package de.tomalbrc.filament.behaviours;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.Map;

public class BehaviourMap implements Iterable<Map.Entry<ResourceLocation, Behaviour<?>>> {
    private final Map<ResourceLocation, Behaviour<?>> behaviourMap = new Object2ObjectOpenHashMap<>();
    public <T> void put(ResourceLocation resourceLocation, Behaviour<?> behaviour) {
        behaviourMap.put(resourceLocation, behaviour);
    }

    public <T> T get(ResourceLocation resourceLocation) {
        return (T) behaviourMap.get(resourceLocation);
    }

    public void from(BehaviourConfigMap configMap) {
        configMap.forEach((resourceLocation, behaviour) -> {
            this.put(resourceLocation, BehaviourRegistry.create(resourceLocation, behaviour));
        });
    }

    @Override
    public Iterator<Map.Entry<ResourceLocation, Behaviour<?>>> iterator() {
        return behaviourMap.entrySet().iterator();
    }
}
