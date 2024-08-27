package de.tomalbrc.filament.behaviours;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import net.minecraft.resources.ResourceLocation;

public interface BehaviourHolder {
    <T extends Behaviour> T getBehaviour(ResourceLocation resourceLocation);
}