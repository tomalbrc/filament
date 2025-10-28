package de.tomalbrc.filament.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.TextUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class FilamentComponents {
    public static final DataComponentType<ItemStack> SKIN_DATA_COMPONENT = new DataComponentType.Builder<ItemStack>().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC).build();
    public static final DataComponentType<HolderSet<Item>> SKIN_COMPONENT = new DataComponentType.Builder<HolderSet<Item>>().persistent(RegistryCodecs.homogeneousList(Registries.ITEM)).networkSynchronized(ByteBufCodecs.holderSet(Registries.ITEM)).build();

    public static final DataComponentType<BackpackOptions> BACKPACK = new DataComponentType.Builder<BackpackOptions>().persistent(BackpackOptions.CODEC).build();

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skin_data"), SKIN_DATA_COMPONENT);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skin"), SKIN_COMPONENT);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack"), BACKPACK);

        PolymerComponent.registerDataComponent(SKIN_COMPONENT);
        PolymerComponent.registerDataComponent(SKIN_DATA_COMPONENT);
        PolymerComponent.registerDataComponent(BACKPACK);
    }

    public record BackpackOptions(int size, boolean preventPlacement, String titlePrefix) {
        public static final Codec<BackpackOptions> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("size", 27).forGetter(BackpackOptions::size),
                        Codec.BOOL.optionalFieldOf("prevent_placement", false).forGetter(BackpackOptions::preventPlacement),
                        Codec.STRING.optionalFieldOf("title_prefix", "").forGetter(BackpackOptions::titlePrefix)
                ).apply(instance, BackpackOptions::new)
        );

        public void open(ItemStack itemStack, ServerPlayer player) {
            ItemContainerContents container = itemStack.get(DataComponents.CONTAINER);
            if (container != null) {
                final int selectedSlot = player.getInventory().getSelectedSlot();
                SimpleContainer container1 = new SimpleContainer(size);
                container.copyInto(container1.items);
                container1.addListener(x -> itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container1.items)));

                MenuProvider provider = new SimpleMenuProvider((id, inventory, p) -> Util.createMenu(container1, id, player, selectedSlot), Component.empty().append(TextUtil.formatText(this.titlePrefix())).append(itemStack.getOrDefault(DataComponents.CUSTOM_NAME, itemStack.get(DataComponents.ITEM_NAME))));
                player.openMenu(provider);
            }
        }
    }
}
