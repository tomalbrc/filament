package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class RPUtil {
    public static void create(SimpleItem item, ResourceLocation id, ItemResource itemResource) {
        if (itemResource != null && itemResource.models() != null && itemResource.models().size() > 1 && !item.components().has(DataComponents.CUSTOM_MODEL_DATA)) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : item.getBehaviours()) {
                if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider) {
                    modelProvider.generate(id, itemResource);
                    return;
                }
            }
        }
    }

    public static boolean useGeneratedModel(BehaviourMap behaviourMap) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : behaviourMap) {
            if (entry.getValue() instanceof ItemPredicateModelProvider) {
                return true;
            }
        }
        return false;
    }
}
