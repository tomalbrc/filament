package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class Wax implements ItemBehaviour<Wax.Config> {
    private final Config config;

    public Wax(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Wax.Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        return HoneycombItem.getWaxed(blockState).map((blockStatex) -> {
            Player player = useOnContext.getPlayer();
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack);
            }

            if (config.reduceDurability && player != null)
                itemStack.hurtAndBreak(1, player, useOnContext.getHand());
            else itemStack.shrink(1);

            level.setBlock(blockPos, blockStatex,  Block.UPDATE_ALL_IMMEDIATE);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockStatex));
            level.levelEvent(player,  LevelEvent.PARTICLES_AND_SOUND_WAX_ON, blockPos, 0);
            return (InteractionResult) InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    public static class Config {
        public boolean reduceDurability = false;
    }
}
