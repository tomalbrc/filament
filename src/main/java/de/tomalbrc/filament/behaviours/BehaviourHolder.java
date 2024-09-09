package de.tomalbrc.filament.behaviours;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import net.minecraft.resources.ResourceLocation;

public interface BehaviourHolder {

    BehaviourMap getBehaviours();

    default <T extends Behaviour> T getBehaviour(ResourceLocation resourceLocation) {
        return this.getBehaviours().get(resourceLocation);
    }

    default void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        this.getBehaviours().from(behaviourConfigMap);
    }
}