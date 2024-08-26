package de.tomalbrc.filament.data.behaviours.item;

import de.tomalbrc.filament.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

public class Instrument implements ItemBehaviour {
    public ResourceLocation sound = null;

    public int range = 0;

    public int useDuration = 0;
}
