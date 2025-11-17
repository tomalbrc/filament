package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

/**
 * Elytra behaviour
 */
public class Elytra implements ItemBehaviour<Elytra.Config> {
    private final Config config;

    public Elytra(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Elytra.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        DispenserBlock.registerBehavior(item, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(config.repairItem);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        return Cosmetic.swapWithEquipmentSlot(item, level, player, interactionHand);
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return BuiltInRegistries.SOUND_EVENT.getHolder(config.sound).orElseThrow();
    }

    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    public static class Config {
        public ResourceLocation sound = ResourceLocation.withDefaultNamespace("item.armor.equip_elytra");
        public Item repairItem = Items.PHANTOM_MEMBRANE;
    }
}