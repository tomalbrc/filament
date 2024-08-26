package de.tomalbrc.filament.data.behaviours.item;

import de.tomalbrc.filament.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

public class Execute implements ItemBehaviour {
    public boolean consumes;

    public String command;

    public ResourceLocation sound;
}
