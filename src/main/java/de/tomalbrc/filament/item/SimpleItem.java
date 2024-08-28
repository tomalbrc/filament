package de.tomalbrc.filament.item;

import de.tomalbrc.filament.FilamentTrimPatterns;
import de.tomalbrc.filament.behaviours.item.Armor;
import de.tomalbrc.filament.behaviours.item.Cosmetic;
import de.tomalbrc.filament.behaviours.item.Execute;
import de.tomalbrc.filament.behaviours.item.Fuel;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.registry.FuelRegistry;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleItem extends Item implements PolymerItem, Equipable {
    final protected ItemData itemData;
    final protected Object2ObjectOpenHashMap<String, PolymerModelData> modelData;

    @Nullable
    PolymerArmorModel armorModel = null;

    @Nullable
    FilamentTrimPatterns.FilamentTrimHolder trimHolder = null;

    public SimpleItem(Properties properties, ItemData itemData) {
        super(properties);

        this.itemData = itemData;
        this.modelData = this.itemData.requestModels();

        // For armor
        if (this.itemData.isArmor()) {
            Armor.ArmorConfig armor = this.itemData.behaviourConfig().get(Constants.Behaviours.ARMOR);
            if (!armor.trim && armor.texture != null)
                this.armorModel = PolymerResourcePackUtils.requestArmor(armor.texture);
            else if (armor.texture != null) {
                this.trimHolder = FilamentTrimPatterns.addConfig(armor);
            }
        }

        if (this.itemData.isCosmetic() || this.itemData.isArmor()) {
            DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        }

        if (this.itemData.isFuel()) {
            Fuel.FuelConfig fuel = this.itemData.behaviourConfig().get(Constants.Behaviours.FUEL);
            FuelRegistry.add(this, fuel.value);
        }
    }

    private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess) {
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        registryAccess.registries().forEach((registryEntry) -> map.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));
        return new RegistryOps.RegistryInfoLookup() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(resourceKey));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
        return new RegistryOps.RegistryInfo(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return this.itemData.components().has(DataComponents.TOOL);
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (this.itemData.components().has(DataComponents.TOOL))
            itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemData.properties() != null)
            itemData.properties().appendHoverText(list);
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        ItemStack itemStack1 = PolymerItemUtils.createItemStack(itemStack, tooltipType, lookup, player);
        if (this.trimHolder != null) itemStack1.set(DataComponents.TRIM, new ArmorTrim(lookup.lookup(Registries.TRIM_MATERIAL).get().get(TrimMaterials.QUARTZ).get(), this.trimHolder.trimPattern, false));
        return itemStack1;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayer player) {
        return itemData.vanillaItem() != null ? itemData.vanillaItem() : Items.PAPER;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        return modelData != null ? this.modelData.get("default").value() : -1;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player) {
        return armorModel != null ? this.armorModel.color() : -1;
    }

    @Override
    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        boolean isArmor = itemData.isArmor();
        boolean isCosmetic = itemData.isCosmetic();
        if (isArmor) {
            Armor.ArmorConfig armor = itemData.behaviourConfig().get(Constants.Behaviours.ARMOR);
            if (armor.slot != null) return armor.slot;
        } else if (isCosmetic) {
            Cosmetic.CosmeticConfig cosmetic = itemData.behaviourConfig().get(Constants.Behaviours.COSMETIC);
            if (cosmetic.slot != null) return cosmetic.slot;
        }

        return EquipmentSlot.MAINHAND;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        var res = super.use(level, user, hand);

        if (this.itemData.canExecute()) {
            Execute.ExecuteConfig execute = this.itemData.behaviourConfig().get(Constants.Behaviours.EXECUTE);
            if (execute.command != null) {
                user.getServer().getCommands().performPrefixedCommand(user.createCommandSourceStack(), execute.command);

                user.awardStat(Stats.ITEM_USED.get(this));

                if (execute.sound != null) {
                    var sound = execute.sound;
                    level.playSound(null, user, BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                if (execute.consumes) {
                    user.getItemInHand(hand).shrink(1);
                }
                res = InteractionResultHolder.consume(user.getItemInHand(hand));
            }
        }

        if (this.itemData.isArmor() || this.itemData.isCosmetic()) {
            res = this.swapWithEquipmentSlot(this, level, user, hand);
        }

        return res;
    }

    public ItemData getItemData() {
        return this.itemData;
    }
}
