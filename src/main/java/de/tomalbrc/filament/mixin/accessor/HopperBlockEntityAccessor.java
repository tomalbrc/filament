package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityAccessor {
    @Invoker(value = "isOnCooldown")
    boolean invokeIsOnCooldown();

    @Invoker(value = "inventoryFull")
    boolean invokeInventoryFull();

    @Accessor(value = "cooldownTime")
    int cooldownTime();

    @Accessor(value = "cooldownTime")
    void setCooldownTime(int time);

    @Accessor(value = "tickedGameTime")
    void setTickedGameTime(long time);

    @Invoker(value = "ejectItems")
    static boolean invokeEjectItems(Level level, BlockPos blockPos, HopperBlockEntity hopperBlockEntity) {
        throw new UnsupportedOperationException();
    }

    @Invoker(value = "getSlots")
    static int[] invokeGetSlots(Container container, Direction direction) {
        throw new UnsupportedOperationException();
    }
}
