package de.tomalbrc.filament.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public interface FakeItem {

    InteractionResult useOn(UseOnContext useOnContext);
}
