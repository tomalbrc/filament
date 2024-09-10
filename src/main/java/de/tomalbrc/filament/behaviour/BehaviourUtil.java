package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BehaviourUtil {
    public static void postInitItem(Item item, BehaviourHolder behaviourHolder, BehaviourConfigMap configMap) {
        if (configMap == null || item == null)
            return;

        for (var e : behaviourHolder.getBehaviours()) {
            if (e.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.init(item, behaviourHolder);
            }
        }
    }

    public static void postInitBlock(Block block, BehaviourHolder behaviourHolder, BehaviourConfigMap configMap) {
        if (configMap == null)
            return;

        for (var e : behaviourHolder.getBehaviours()) {
            if (e.getValue() instanceof BlockBehaviour<?> blockBehaviour) {
                blockBehaviour.init(block, behaviourHolder);
            }
        }
    }
}
