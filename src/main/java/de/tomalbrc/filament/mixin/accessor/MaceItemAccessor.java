package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MaceItem.class)
public interface MaceItemAccessor {
    @Invoker
    static void invokeKnockback(Level level, Player entity, Entity entity2) {
        throw new AssertionError();
    }
}
