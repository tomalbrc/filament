package de.tomalbrc.filament.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple item, base for filament items without block (+ decorations), with behaviour support
 */
public class SimpleItem extends Item implements PolymerItem, FilamentItem, BehaviourHolder {
    protected final Data<?> data;
    protected final ItemProperties properties;
    protected final Item vanillaItem;

    protected final BehaviourMap behaviours = new BehaviourMap();
    protected final FilamentItemDelegate delegate;

    public SimpleItem(Properties properties, Data<?> data, Item vanillaItem) {
        super(properties);
        this.initBehaviours(data.behaviour());

        this.vanillaItem = vanillaItem;
        this.data = data;
        this.properties = data.properties();
        this.delegate = new FilamentItemDelegate(this);
    }

    @Override
    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    @Override
    public Data<?> getData() {
        return this.data;
    }

    @Override
    public FilamentItemDelegate getDelegate() {
        return this.delegate;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void verifyComponentsAfterLoad(ItemStack itemStack) {
        if (this.data != null) {
            for (Map.Entry<DataComponentType<?>, JsonElement> entry : this.data.getAdditionalComponents().entrySet()) {
                var codec = entry.getKey().codec();
                assert codec != null;

                RegistryOps.RegistryInfoLookup registryInfoLookup = Json.DataComponentsDeserializer.createContext(Filament.REGISTRY_ACCESS.compositeAccess());
                var result = codec.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), entry.getValue());
                if (result.hasResultOrPartial()) {
                    DataComponentType type = entry.getKey();
                    itemStack.set(type, result.getOrThrow().getFirst());
                }
            }
        }
    }

    @Override
    public @NotNull Component getName(@NonNull ItemStack itemStack) {
        var dataName = this.data.displayName();
        return dataName != null ? dataName : data.components().has(DataComponents.ITEM_NAME) ? data.components().get(DataComponents.ITEM_NAME) : super.getName(itemStack);
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack itemStack, int i) {
        this.delegate.onUseTick(level, livingEntity, itemStack, i);
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity livingEntity, int useDuration) {
        return this.delegate.releaseUsing(itemStack, level, livingEntity, useDuration, () -> super.releaseUsing(itemStack, level, livingEntity, useDuration));
    }

    @Override
    public boolean useOnRelease(@NonNull ItemStack itemStack) {
        return this.delegate.useOnRelease(itemStack, () -> super.useOnRelease(itemStack));
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack, @NonNull LivingEntity livingEntity) {
        return this.delegate.getUseDuration(itemStack, livingEntity, () -> super.getUseDuration(itemStack, livingEntity));
    }

    @Override
    @NotNull
    public ItemUseAnimation getUseAnimation(@NonNull ItemStack itemStack) {
        return this.delegate.getUseAnimation(itemStack, () -> super.getUseAnimation(itemStack));
    }

    @Override
    public void hurtEnemy(@NonNull ItemStack itemStack, @NonNull LivingEntity livingEntity, @NonNull LivingEntity livingEntity2) {
        this.delegate.hurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void postHurtEnemy(@NonNull ItemStack itemStack, @NonNull LivingEntity livingEntity, @NonNull LivingEntity livingEntity2) {
        this.delegate.postHurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void appendHoverText(@NonNull ItemStack itemStack, Item.@NonNull TooltipContext tooltipContext, @NonNull TooltipDisplay tooltipDisplay, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag tooltipFlag) {
        this.delegate.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.properties.appendHoverText(consumer);
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.vanillaItem != null ? this.vanillaItem : Items.PAPER;
    }

    @Override
    public final ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext packetContext, HolderLookup.Provider provider) {
        return Util.filamentItemStack(itemStack, tooltipType, packetContext, this);
    }

    @Override
    @NotNull
    public InteractionResult use(@NonNull Level level, @NonNull Player user, @NonNull InteractionHand hand) {
        return this.delegate.use(this, level, user, hand, () -> super.use(level, user, hand));
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NonNull UseOnContext useOnContext) {
        return this.delegate.useOn(useOnContext, () -> useOnContext.getItemInHand().has(DataComponents.CONSUMABLE) ? super.use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand()) : InteractionResult.FAIL);
    }

    @Override
    public boolean mineBlock(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull BlockState blockState, @NonNull BlockPos blockPos, @NonNull LivingEntity livingEntity) {
        return this.delegate.mineBlock(itemStack, level, blockState, blockPos, livingEntity, () -> super.mineBlock(itemStack, level, blockState, blockPos, livingEntity));
    }

    /// Entity Damaging behaviours
    @Override
    public float getAttackDamageBonus(@NonNull Entity entity, float f, @NonNull DamageSource damageSource) {
        return this.delegate.getAttackDamageBonus(entity, f, damageSource, () -> super.getAttackDamageBonus(entity, f, damageSource));
    }

    @Override
    @Nullable
    public DamageSource getItemDamageSource(@NotNull LivingEntity livingEntity) {
        return this.delegate.getDamageSource(livingEntity, () -> super.getItemDamageSource(livingEntity));
    }

    @Override
    public boolean canFitInsideContainerItems() {
        var c = DecorationData.getFirstContainer(this);
        if (c != null && c.canPickUp())
            return false;

        return super.canFitInsideContainerItems();
    }
}
