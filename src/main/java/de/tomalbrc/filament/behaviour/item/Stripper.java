package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.mixin.behaviour.strippable.AxeItemAccessor;
import de.tomalbrc.filament.registry.StrippableRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Stripper implements ItemBehaviour<Stripper.Config> {
    private final Config config;

    public Stripper(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Stripper.Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();

        BlockState optional = this.getNewBlockState(level, blockPos, level.getBlockState(blockPos));
        if (optional == null) {
            return InteractionResult.PASS;
        } else {
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }

            level.setBlock(blockPos, optional, Block.UPDATE_IMMEDIATE | Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, optional));
            if (player != null) {
                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
            }

            return InteractionResult.SUCCESS;
        }
    }

    private BlockState getNewBlockState(Level level, BlockPos blockPos, BlockState blockState) {
        var replacementBlock = AxeItemAccessor.getSTRIPPABLES().get(blockState.getBlock());
        if (replacementBlock == null && StrippableRegistry.has(blockState.getBlock())) {
            replacementBlock = StrippableRegistry.get(blockState.getBlock());
        }

        if (replacementBlock != null) {
            level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.sound), SoundSource.BLOCKS, 1.0F, 1.0F);
            return replacementBlock.withPropertiesOf(blockState);
        } else {
            Optional<BlockState> opt = WeatheringCopper.getPrevious(blockState);
            if (opt.isPresent()) {
                level.playSound(null, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.levelEvent(null, LevelEvent.PARTICLES_SCRAPE, blockPos, 0);
                return opt.get();
            } else {
                opt = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(blockState.getBlock())).map((block) -> block.withPropertiesOf(blockState));
                if (opt.isPresent()) {
                    level.playSound(null, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(null, LevelEvent.PARTICLES_WAX_OFF, blockPos, 0);
                    return opt.get();
                } else {
                    return null;
                }
            }
        }
    }

    public static class Config {
        public ResourceLocation sound = SoundEvents.AXE_STRIP.location();
    }
}
