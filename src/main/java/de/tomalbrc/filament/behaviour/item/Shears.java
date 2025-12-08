package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Shears behaviour
 */
public class Shears implements ItemBehaviour<Shears.Config> {
    private final Config config;

    public Shears(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Shears.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        ItemBehaviour.super.init(item, behaviourHolder);
        DispenserBlock.registerBehavior(item, new ShearsDispenseItemBehavior());
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        Tool tool = itemStack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        } else {
            if (!level.isClientSide() && !blockState.is(BlockTags.FIRE) && tool.damagePerBlock() > 0) {
                itemStack.hurtAndBreak(tool.damagePerBlock(), livingEntity, EquipmentSlot.MAINHAND);
            }

            return true;
        }
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof GrowingPlantHeadBlock growingPlantHeadBlock) {
            if (!growingPlantHeadBlock.isMaxAge(blockState)) {
                Player player = useOnContext.getPlayer();
                ItemStack itemStack = useOnContext.getItemInHand();
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
                }

                level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.sound), SoundSource.BLOCKS, 1.0F, 1.0F);
                BlockState blockState2 = growingPlantHeadBlock.getMaxAgeState(blockState);
                level.setBlockAndUpdate(blockPos, blockState2);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), blockState2));
                if (player != null) {
                    itemStack.hurtAndBreak(1, player, useOnContext.getHand());
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }

    public static boolean is(ItemStack itemStack) {
        return itemStack.getItem().isFilamentItem() && itemStack.getItem().has(Behaviours.SHEARS);
    }

    public static class Config {
        public ResourceLocation sound = SoundEvents.GROWING_PLANT_CROP.location();
    }
}