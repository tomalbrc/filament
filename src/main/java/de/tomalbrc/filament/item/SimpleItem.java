package de.tomalbrc.filament.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simple item, base for filament items without block (+ decorations), with behaviour support
 */
public class SimpleItem extends Item implements PolymerItem, FilamentItem, BehaviourHolder, Equipable {
    protected final Data<?> data;
    protected final ItemProperties properties;
    protected final Item vanillaItem;

    protected final BehaviourMap behaviours = new BehaviourMap();
    protected final FilamentItemDelegate delegate;

    public Object2ObjectOpenHashMap<String, PolymerModelData> modelData; // 1.21.1

    public SimpleItem(Properties properties, Data<?> data, Item vanillaItem) {
        super(properties);
        this.initBehaviours(data.behaviour());

        this.vanillaItem = vanillaItem;
        this.data = data;
        this.properties = data.properties();
        this.delegate = new FilamentItemDelegate(this);

    }

    public void requestModels() {
        this.modelData = this.data.requestModels();
    }

    @Override
    public Map<String, PolymerModelData> getModelData() {
        return modelData;
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
    public @NotNull Component getName(ItemStack itemStack) {
        var dataName = this.data.displayName();
        return dataName != null ? dataName : data.components().has(DataComponents.ITEM_NAME) ? data.components().get(DataComponents.ITEM_NAME) : super.getName(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        this.delegate.onUseTick(level, livingEntity, itemStack, i);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        this.delegate.releaseUsing(itemStack, level, livingEntity, useDuration);
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        return this.delegate.useOnRelease(itemStack, () -> super.useOnRelease(itemStack));
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return this.delegate.getUseDuration(itemStack, livingEntity, () -> super.getUseDuration(itemStack, livingEntity));
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return this.delegate.hurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        this.delegate.postHurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> consumer, TooltipFlag tooltipFlag) {
        this.delegate.appendHoverText(itemStack, tooltipContext, consumer, tooltipFlag);
        this.properties.appendHoverText(consumer::add);
        super.appendHoverText(itemStack, tooltipContext, consumer, tooltipFlag);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, ServerPlayer packetContext) {
        return this.vanillaItem != null ? this.vanillaItem : Items.PAPER;
    }

    @Override
    public final ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, ServerPlayer packetContext) {
        return Util.filamentItemStack(itemStack, tooltipType, lookup, packetContext, this);
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var data = itemBehaviour.modifyPolymerCustomModelData(this.modelData, itemStack, player);
                if (data != -1) {
                    return data;
                }
            }
        }

        return this.modelData != null && !this.modelData.isEmpty() ?
                this.modelData.get("default").value() : data.components().has(DataComponents.CUSTOM_DATA) ?
                Objects.requireNonNull(data.components().get(DataComponents.CUSTOM_MODEL_DATA)).value() : -1;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayer player) {
        int color = -1;
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                color = itemBehaviour.modifyPolymerArmorColor(itemStack, player, color);
            }
        }
        return color;
    }

    @Override
    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var slot = itemBehaviour.getEquipmentSlot();
                if (slot != EquipmentSlot.MAINHAND) {
                    return slot;
                }
            }
        }
        return EquipmentSlot.MAINHAND;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        return this.delegate.use(this, level, user, hand, () -> super.use(level, user, hand));
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        return this.delegate.useOn(useOnContext, () -> useOnContext.getItemInHand().has(DataComponents.FOOD) ? super.use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand()).getResult() : InteractionResult.FAIL);
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        return this.delegate.mineBlock(itemStack, level, blockState, blockPos, livingEntity, () -> super.mineBlock(itemStack, level, blockState, blockPos, livingEntity));
    }

    /// Entity Damaging behaviours
    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        return this.delegate.getAttackDamageBonus(entity, f, damageSource, () -> super.getAttackDamageBonus(entity, f, damageSource));
    }

    // TODO: 1.21.1
//    @Override
//    @Nullable
//    public DamageSource getDamageSource(LivingEntity livingEntity) {
//        return this.delegate.getDamageSource(livingEntity, () -> super.getDamageSource(livingEntity));
//    }

    @Override
    public boolean canFitInsideContainerItems() {
        var c = DecorationData.getFirstContainer(this);
        if (c != null && c.canPickUp())
            return false;

        return super.canFitInsideContainerItems();
    }
}
