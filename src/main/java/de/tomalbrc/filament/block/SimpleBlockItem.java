package de.tomalbrc.filament.block;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.FilamentItemDelegate;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleBlockItem extends BlockItem implements PolymerItem, FilamentItem, BehaviourHolder, Equipable {
    private final AbstractBlockData<?> data;

    protected final BehaviourMap behaviours = new BehaviourMap();
    protected final FilamentItemDelegate delegate;

    protected Object2ObjectOpenHashMap<String, PolymerModelData> modelData; // 1.21.1

    public SimpleBlockItem(Properties properties, Block block, AbstractBlockData<?> data) {
        super(block, properties);
        this.data = data;
        this.initBehaviours(data.behaviour());

        this.delegate = new FilamentItemDelegate(this);
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        var dataName = this.data.displayName();
        return dataName != null ? dataName : data.components().getOrDefault(DataComponents.ITEM_NAME, super.getName(itemStack));
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
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!super.placeBlock(context, state)) {
            return false;
        }

        if (context.getPlayer() instanceof ServerPlayer player) {
            BlockUtil.handleBlockPlaceEffects(player, context.getHand(), context.getClickedPos(), state.getSoundType());
        }

        return true;
    }

    @Override
    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void verifyComponentsAfterLoad(ItemStack itemStack) {
        if (this.getData() != null) {
            for (Map.Entry<DataComponentType<?>, JsonElement> entry : this.getData().getAdditionalComponents().entrySet()) {
                var codec = entry.getKey().codec();
                assert codec != null;

                RegistryOps.RegistryInfoLookup registryInfoLookup = Json.DataComponentsDeserializer.createContext(Filament.REGISTRY_ACCESS.compositeAccess());
                var result = codec.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), entry.getValue());
                if (result.hasResultOrPartial()) {
                    DataComponentType type = entry.getKey();
                    itemStack.set(type, (Object) result.getOrThrow().getFirst());
                }
            }
        }
    }

    @Override
    public void requestModels() {
        this.modelData = this.data.requestModels();
    }

    @Override
    public Map<String, PolymerModelData> getModelData() {
        return modelData;
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
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        this.delegate.postHurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        this.delegate.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        this.data.properties().appendHoverText(list::add);
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, ServerPlayer packetContext) {
        return this.getData().vanillaItem();
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
                this.modelData.getOrDefault("default", this.modelData.values().iterator().next()).value() : data.components().has(DataComponents.CUSTOM_DATA) ?
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
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var valid = itemBehaviour.isValidRepairItem(itemStack, itemStack2);
                if (valid)
                    return true;
            }
        }
        return super.isValidRepairItem(itemStack, itemStack2);
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
    public Holder<SoundEvent> getEquipSound() {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var sound = itemBehaviour.getEquipSound();
                if (sound != null)
                    return sound;
            }
        }
        return FilamentItem.super.getEquipSound();
    }

    @Override
    public int getEnchantmentValue() {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var val = itemBehaviour.getEnchantmentValue();
                if (val.isPresent())
                    return val.get();
            }
        }
        return super.getEnchantmentValue();
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        return this.delegate.use(this, level, user, hand, () -> super.use(level, user, hand));
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        return this.delegate.useOn(useOnContext, () -> {
            var res = super.useOn(useOnContext);
            if (res.consumesAction()) {
                return res;
            }

            return InteractionResult.FAIL;
        });
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
}