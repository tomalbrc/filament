package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.class)
public interface ItemInvoker {
    @Invoker("getPlayerPOVHitResult")
    static BlockHitResult invokeGetPlayerPOVHitResult(
            Level level,
            Player player,
            ClipContext.Fluid fluid
    ) {
        throw new AssertionError();
    }
}