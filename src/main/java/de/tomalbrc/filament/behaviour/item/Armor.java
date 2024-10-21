package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.trim.FilamentTrimPatterns;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Armor item behaviour, using fancypants shader via polymer or armor trims
 */
public class Armor implements ItemBehaviour<Armor.Config> {
    private final Config config;
    private PolymerArmorModel armorModel;
    private FilamentTrimPatterns.FilamentTrimHolder trimHolder;

    public Armor(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Armor.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        if (!config.trim && config.texture != null)
            this.armorModel = PolymerResourcePackUtils.requestArmor(config.texture);
        else if (config.texture != null) {
            this.trimHolder = FilamentTrimPatterns.addConfig(config);
        }

        DispenserBlock.registerBehavior(item, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public void modifyPolymerItemStack(ItemStack original, ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        if (this.trimHolder != null)
            itemStack.set(DataComponents.TRIM, new ArmorTrim(lookup.lookup(Registries.TRIM_MATERIAL).orElseThrow().get(TrimMaterials.QUARTZ).orElseThrow(), this.trimHolder.trimPattern, false));
    }

    public int modifyPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player, int color) {
        return this.armorModel != null ? this.armorModel.color() : color;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        if (item instanceof Equipable equipable) {
            return equipable.swapWithEquipmentSlot(item, level, player, interactionHand);
        }
        return ItemBehaviour.super.use(item, level, player, interactionHand);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.config.slot;
    }

    public static class Config {
        /**
         * The equipment slot for the armor piece (e.g., head, chest, legs, or feet).
         */
        public EquipmentSlot slot;

        /**
         * The resource location of the texture associated with the armor.
         */
        public ResourceLocation texture;

        /**
         * Flag whether to use armor trims instead of shader based armor
         */
        public boolean trim = false;
    }
}
