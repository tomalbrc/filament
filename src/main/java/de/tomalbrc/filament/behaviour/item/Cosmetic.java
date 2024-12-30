package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic implements ItemBehaviour<Cosmetic.Config> {
    private final Config config;

    public Cosmetic(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Cosmetic.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        if (item instanceof Equipable) {
            return swapWithEquipmentSlot(item, level, player, interactionHand);
        }

        return ItemBehaviour.super.use(item, level, player, interactionHand);
    }

    public static InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
        if (!player.canUseSlot(equipmentSlot)) {
            return InteractionResultHolder.pass(itemStack);
        } else {
            ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
            if ((!EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative()) && !ItemStack.matches(itemStack, itemStack2)) {
                if (!level.isClientSide()) {
                    player.awardStat(Stats.ITEM_USED.get(item));
                }

                ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : itemStack2.copyAndClear();
                ItemStack itemStack4 = itemStack.copy();
                itemStack.consume(1, player);
                player.setItemSlot(equipmentSlot, itemStack4.copyWithCount(1));
                return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return this.config.slot;
    }

    public static class Config {
        /**
         * The equipment slot for the cosmetic (head, chest).
         */
        public EquipmentSlot slot;

        /**
         * The resource location of the animated model for the cosmetic.
         */
        public ResourceLocation model;

        /**
         * The name of the animation to autoplay. The animation should be loopable
         */
        public String autoplay;

        /**
         * Scale of the chest cosmetic
         */
        public Vector3f scale = new Vector3f(1);

        /**
         * Translation of the chest cosmetic
         */
        public Vector3f translation = new Vector3f();
    }
}
