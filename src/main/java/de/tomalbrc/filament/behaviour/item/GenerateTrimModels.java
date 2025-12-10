package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Trident behaviour
 */
public class GenerateTrimModels implements ItemBehaviour<GenerateTrimModels.Config>, ItemPredicateModelProvider {
    private final Config config;

    public GenerateTrimModels(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public GenerateTrimModels.Config getConfig() {
        return this.config;
    }

    private EquipmentSlot slot(Data<?> data) {
        if (data.components().has(DataComponents.EQUIPPABLE)) {
            return Objects.requireNonNull(data.components().get(DataComponents.EQUIPPABLE)).slot();
        }

        return null;
    }

    @Override
    public void generate(Data<?> data) {
        var slot = slot(data);
        String armorType = null;
        String namespace = null;
        if (slot != null && config.typePrefix == null) {
            for (ArmorType type : ArmorType.values()) {
                if (type.getSlot() == slot) {
                    namespace = Identifier.DEFAULT_NAMESPACE;
                    armorType = "trims/items/" + type.getName() + "_trim_";
                    break;
                }
            }
        } else if (config.typePrefix != null) {
            // support non wearable items like axes
            if (config.typePrefix.contains(":")) {
                var r = Identifier.parse(config.typePrefix);
                armorType = r.getPath();
                namespace = r.getNamespace();
            }
            else {
                armorType = config.typePrefix;
            }
        }

        var itemResource = data.itemResource();
        if (armorType != null && itemResource != null) {
            ItemAssetGenerator.createItemModels(data.id(), itemResource);

            final Object2ObjectArrayMap<Identifier, byte[]> map = new Object2ObjectArrayMap<>();
            var list = new ArrayList<Identifier>();
            list.addAll(config.customMaterials);
            list.addAll(config.materials);

            // TODO: trim_material registry not available at the point the rp is generated?
            // Filament.SERVER.registries().compositeAccess().getOrThrow(Registries.TRIM_MATERIAL).value().entrySet().forEach((resourceKeyTrimMaterialEntry -> {
            //                    var trimMaterialReference = resourceKeyTrimMaterialEntry.getValue();
            //                    var assetInfo = trimMaterialReference.assets().assetId(Objects.requireNonNull(eq).assetId().orElseThrow());

            for (Identifier suffix : list) {
                var layer1 = Identifier.fromNamespaceAndPath(namespace == null ? suffix.getNamespace() : namespace, String.format("%s%s", armorType, suffix.getPath()));
                var name = data.id().getPath() + MaterialAssetGroup.SEPARATOR + suffix.getPath();
                ModelAsset.Builder modelAsset = ModelAsset.builder();

                modelAsset.parent(itemResource.parent());

                var layer0 = itemResource.textures().get("default");
                modelAsset.texture("layer0", layer0.get("layer0").toString());
                modelAsset.texture("layer1", layer1.toString());

                var model = Identifier.fromNamespaceAndPath(data.id().getNamespace(), name);
                var models = itemResource.getModels();
                if (models != null)
                    models.put(suffix.getPath(), model);

                map.put(model, modelAsset.build().toBytes());
            }

            PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
                map.forEach((model, modelAsset) -> resourcePackBuilder.addData(AssetPaths.itemModel(model), modelAsset));

                ItemAssetGenerator.createTrimModels(
                        resourcePackBuilder, data.id(),
                        Objects.requireNonNull(data.itemResource()),
                        data.components().has(DataComponents.DYED_COLOR) || data.vanillaItem().components().has(DataComponents.DYED_COLOR)
                );
            });
        }
    }

    @Override
    public List<String> requiredModels() {
        return List.of("default");
    }

    @Override
    public boolean canCreateItemModels() {
        return true;
    }

    public static class Config {
        public String typePrefix = null;
        public List<Identifier> customMaterials = List.of();
        public List<Identifier> materials = List.of(
                Identifier.withDefaultNamespace("quartz"),
                Identifier.withDefaultNamespace("iron"),
                Identifier.withDefaultNamespace("netherite"),
                Identifier.withDefaultNamespace("redstone"),
                Identifier.withDefaultNamespace("copper"),
                Identifier.withDefaultNamespace("gold"),
                Identifier.withDefaultNamespace("emerald"),
                Identifier.withDefaultNamespace("diamond"),
                Identifier.withDefaultNamespace("lapis"),
                Identifier.withDefaultNamespace("amethyst"),
                Identifier.withDefaultNamespace("resin")
        );
    }
}