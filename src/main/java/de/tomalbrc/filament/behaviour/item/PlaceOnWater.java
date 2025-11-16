package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.mixin.accessor.ItemInvoker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Item behaviour to enable place action on water
 */
public class PlaceOnWater implements ItemBehaviour<PlaceOnWater.Config> {
    private final Config config;

    public PlaceOnWater(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public PlaceOnWater.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        BlockHitResult blockHitResult = ItemInvoker.invokeGetPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockHitResult blockHitResult2 = blockHitResult.withPosition(blockHitResult.getBlockPos().above());
        if (item instanceof BlockItem blockItem) {
            var c = blockItem.useOn(new UseOnContext(player, interactionHand, blockHitResult2));
            return new InteractionResultHolder<>(c, player.getItemInHand(interactionHand));
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public static class Config {
    }
}