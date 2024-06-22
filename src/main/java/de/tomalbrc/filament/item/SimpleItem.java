package de.tomalbrc.filament.item;

import de.tomalbrc.filament.registry.filament.EntityRegistry;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.tomalbrc.filament.data.ItemData;
import org.joml.Vector3f;

import java.util.List;

public class SimpleItem extends Item implements PolymerItem, Equipable {
    final protected ItemData itemData;
    final protected Object2ObjectOpenHashMap<String, PolymerModelData> modelData;

    @Nullable
    PolymerArmorModel armorModel = null;

    public SimpleItem(Properties properties, ItemData itemData) {
        super(properties);
        this.itemData = itemData;
        this.modelData = this.itemData.requestModels();

        // For armor
        if (this.itemData.isArmor() && this.itemData.behaviour().armor.texture != null) {
            this.armorModel = PolymerResourcePackUtils.requestArmor(this.itemData.behaviour().armor.texture);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemData.properties() != null)
            itemData.properties().appendHoverText(list);
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

    public EquipmentSlot getEquipmentSlot() {
        if (itemData.behaviour() != null && itemData.behaviour().armor != null && itemData.behaviour().armor.slot != null) {
            return itemData.behaviour().armor.slot;
        }
        return EquipmentSlot.MAINHAND;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        if (this.itemData.canExecute() && this.itemData.behaviour().execute.command != null) {
            user.getServer().getCommands().performPrefixedCommand(user.createCommandSourceStack(), this.itemData.behaviour().execute.command);

            user.awardStat(Stats.ITEM_USED.get(this));

            if (this.itemData.behaviour().execute.sound != null) {
                var sound = this.itemData.behaviour().execute.sound;
                level.playSound(null, user, BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            if (this.itemData.behaviour().execute.consumes) {
                user.getItemInHand(hand).shrink(1);
                return InteractionResultHolder.consume(user.getItemInHand(hand));
            }

            return InteractionResultHolder.consume(user.getItemInHand(hand));
        }

        return InteractionResultHolder.fail(user.getItemInHand(hand));
    }
}
