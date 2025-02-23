package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FilamentComponents {
    public static final DataComponentType<ItemStack> SKIN_DATA_COMPONENT = new DataComponentType.Builder<ItemStack>().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC).build();
    public static final DataComponentType<HolderSet<Item>> SKIN_COMPONENT = new DataComponentType.Builder<HolderSet<Item>>().persistent(RegistryCodecs.homogeneousList(Registries.ITEM)).networkSynchronized(ByteBufCodecs.holderSet(Registries.ITEM)).build();

    public static void register() {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skin_data"),
                SKIN_DATA_COMPONENT
        );

        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skin"),
                SKIN_COMPONENT
        );

        PolymerComponent.registerDataComponent(SKIN_COMPONENT);
        PolymerComponent.registerDataComponent(SKIN_DATA_COMPONENT);
    }
}
