package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.mixin.accessor.ShovelItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Shovel behaviour
 */
public class Shovel implements ItemBehaviour<Shovel.Config> {
    private final Config config;

    public Shovel(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Shovel.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (useOnContext.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player player = useOnContext.getPlayer();
            BlockState blockState2 = ShovelItemAccessor.getFLATTENABLES().get(blockState.getBlock());
            BlockState blockState3 = null;
            if (blockState2 != null && level.getBlockState(blockPos.above()).isAir()) {
                level.playSound(player, blockPos, SoundEvent.createVariableRangeEvent(config.sound), SoundSource.BLOCKS, 1.0F, 1.0F);
                blockState3 = blockState2;
            } else if (blockState.getBlock() instanceof CampfireBlock && blockState.getValue(CampfireBlock.LIT)) {
                if (!level.isClientSide()) {
                    level.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockPos, 0);
                }

                CampfireBlock.dowse(useOnContext.getPlayer(), level, blockPos, blockState);
                blockState3 = blockState.setValue(CampfireBlock.LIT, false);
            }

            if (blockState3 != null) {
                if (!level.isClientSide()) {
                    level.setBlock(blockPos, blockState3, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState3));
                    if (player != null) {
                        useOnContext.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
                    }
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static class Config {
        public ResourceLocation sound = SoundEvents.SHOVEL_FLATTEN.location();
    }
}