package de.tomalbrc.filament.mixin.behaviour.crop;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HarvestFarmland.class)
public abstract class HarvestFarmlandMixin {
    @Shadow private @Nullable BlockPos aboveFarmlandPos;

    @Shadow @Final private List<BlockPos> validFarmlandAroundVillager;

    @Shadow @Nullable protected abstract BlockPos getValidFarmland(ServerLevel serverLevel);

    @Shadow private long nextOkStartTime;

    @Inject(method = "validPos", at = @At("RETURN"), cancellable = true)
    private void filament$validPos(BlockPos blockPos, ServerLevel serverLevel, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.getBlock().isFilamentBlock() && blockState.getBlock().has(Behaviours.CROP) && blockState.getBlock().getOrThrow(Behaviours.CROP).isMaxAge(blockState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.BY, by = 3, ordinal = 1))
    private void filament$onTick(ServerLevel serverLevel, Villager villager, long l, CallbackInfo ci, @Local BlockState blockState, @Local(ordinal = 0) Block block, @Local(ordinal = 1) Block block2) {
        if (block.isFilamentBlock()) {
            if (filament$isCustomCrop(block) && block.getOrThrow(Behaviours.CROP).isMaxAge(blockState) && aboveFarmlandPos != null) {
                serverLevel.destroyBlock(this.aboveFarmlandPos, true, villager);

                if (blockState.isAir() && filament$isCustomCrop(block2) && villager.hasFarmSeeds()) {
                    SimpleContainer simpleContainer = villager.getInventory();
                    for (int i = 0; i < simpleContainer.getContainerSize(); ++i) {
                        ItemStack itemStack = simpleContainer.getItem(i);
                        if (!itemStack.isEmpty() && itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && itemStack.getItem() instanceof BlockItem blockItem && filament$isCustomCrop(blockItem.getBlock())) {
                            BlockState blockState2 = blockItem.getBlock().defaultBlockState();
                            serverLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockState2);
                            serverLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockState2));
                        } else {
                            continue;
                        }
                        serverLevel.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0f, 1.0f);
                        itemStack.shrink(1);
                        if (!itemStack.isEmpty()) break;
                        simpleContainer.setItem(i, ItemStack.EMPTY);
                        break;
                    }
                }

                if (!block.getOrThrow(Behaviours.CROP).isMaxAge(blockState)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(serverLevel);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = l + 20;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5f, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
                    }
                }
            }
        }
    }

    @Unique boolean filament$isCustomCrop(Block block) {
        return block.isFilamentBlock() && block.has(Behaviours.CROP) && block.getOrThrow(Behaviours.CROP).getConfig().villagerInteraction;
    }
}
