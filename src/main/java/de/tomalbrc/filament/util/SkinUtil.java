package de.tomalbrc.filament.util;

import de.tomalbrc.filament.registry.FilamentComponents;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class SkinUtil {
    private static final List<DataComponentType<?>> COMPONENTS_TO_COPY = List.of(DataComponents.ITEM_NAME, DataComponents.CUSTOM_NAME, DataComponents.ATTRIBUTE_MODIFIERS, DataComponents.LORE, DataComponents.DAMAGE, DataComponents.MAX_DAMAGE, DataComponents.MAX_STACK_SIZE, DataComponents.UNBREAKABLE, DataComponents.CHARGED_PROJECTILES, DataComponents.TOOL, DataComponents.ENCHANTMENTS, DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

    public static void registerEventHandler() {
        PolymerItemUtils.ITEM_CHECK.register(x -> x.has(FilamentComponents.SKIN_DATA_COMPONENT));
        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((orig,mod,player) -> {
            var newStack = SkinUtil.wrap(orig, mod, player);
            if (mod == newStack) return mod;
            newStack.set(DataComponents.CUSTOM_DATA, mod.get(DataComponents.CUSTOM_DATA));
            return newStack;
        });
    }

    public static ItemStack wrap(ItemStack itemStack, ItemStack mod, ServerPlayer player) {
        if (itemStack.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var wrappedItemStack = itemStack.get(FilamentComponents.SKIN_DATA_COMPONENT).copy();
            if (wrappedItemStack != null && !wrappedItemStack.isEmpty()) {
                // important
                var oldWrapped = wrappedItemStack;
                if (wrappedItemStack.getItem() instanceof PolymerItem polymerItem) {
                    wrappedItemStack = polymerItem.getPolymerItemStack(wrappedItemStack, TooltipFlag.NORMAL, player.registryAccess(), player);
                }
                else wrappedItemStack = wrappedItemStack.copy();

                for (DataComponentType type : COMPONENTS_TO_COPY) {
                    wrappedItemStack.set(type, itemStack.get(type));
                }

                if (itemStack.getItem() instanceof ArmorItem armorItem) {
                    // todo: merge with existing attributes..?!
                    wrappedItemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, armorItem.getDefaultAttributeModifiers());
                }

                wrappedItemStack.set(DataComponents.ITEM_NAME, mod.getHoverName());

                // modify lore
                var lore = itemStack.get(DataComponents.LORE);
                if (lore == null || lore.lines().isEmpty()) {
                    lore = new ItemLore(ObjectArrayList.of());
                }
                var component = Component.literal("Skin: ").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)).append(oldWrapped.getHoverName());
                lore.lines().addFirst(component);

                wrappedItemStack.set(DataComponents.LORE, lore);
                wrappedItemStack.setCount(itemStack.getCount());

                return wrappedItemStack;
            }
        }
        return mod;
    }

    public static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
