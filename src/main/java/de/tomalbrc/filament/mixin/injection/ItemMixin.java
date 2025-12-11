package de.tomalbrc.filament.mixin.injection;

import de.tomalbrc.filament.injection.FilamentItemExtension;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements FilamentItemExtension {

}
