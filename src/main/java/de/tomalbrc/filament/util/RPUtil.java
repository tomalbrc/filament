package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.component.DataComponents;

import java.util.Map;

public class RPUtil {
    public static void create(SimpleItem item, Data data) {
        var itemResources = data.itemResource();
        if (itemResources != null && data.itemModel() == null && itemResources.getModels() != null && !data.components().has(DataComponents.ITEM_MODEL)) {
            if (itemResources.models().size() > 1) {
                for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : item.getBehaviours()) {
                    if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider) {
                        modelProvider.generate(data);
                        return;
                    }
                }
            }

            // todo: models for "breaking" stage of item (using dur. component) ..?

            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder ->
                ItemAssetGenerator.createDefault(
                    resourcePackBuilder, data.id(),
                    itemResources, data.vanillaItem().components().has(DataComponents.DYED_COLOR)
                )
            );
        }
    }
}
