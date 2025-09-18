package de.tomalbrc.filament.decoration.util;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DecorationItemDisplayElement extends ItemDisplayElement {
    public DecorationItemDisplayElement(ItemStack stack) {
        super(stack);
    }

    public DecorationItemDisplayElement() {super();}

    public DecorationItemDisplayElement(Item item) {
        super(item);
    }
}
