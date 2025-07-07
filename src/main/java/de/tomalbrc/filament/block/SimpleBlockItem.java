package de.tomalbrc.filament.block;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.function.Consumer;

public class SimpleBlockItem extends BlockItem implements PolymerItem, FilamentItem, BehaviourHolder {
    private final AbstractBlockData<?> data;

    protected final BehaviourMap behaviours = new BehaviourMap();
    protected final FilamentItemDelegate delegate;

    public SimpleBlockItem(Properties properties, Block block, AbstractBlockData<?> data) {
        super(block, properties);
        this.data = data;
        this.initBehaviours(data.behaviour());

        this.delegate = new FilamentItemDelegate(this);
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
    @NotNull
    public FeatureFlagSet requiredFeatures() {
        return this.getBlock().requiredFeatures();
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
        super.verifyComponentsAfterLoad(itemStack);

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
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        this.delegate.onUseTick(level, livingEntity, itemStack, i);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        return this.delegate.releaseUsing(itemStack, level, livingEntity, useDuration, () -> super.releaseUsing(itemStack, level, livingEntity, useDuration));
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
    @NotNull
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return this.delegate.getUseAnimation(itemStack, () -> super.getUseAnimation(itemStack));
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        this.delegate.postHurtEnemy(itemStack, livingEntity, livingEntity2);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        this.delegate.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.data.properties().appendHoverText(consumer);
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.getData().vanillaItem();
    }

    @Override
    public final ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext packetContext) {
        return Util.filamentItemStack(itemStack, tooltipType, packetContext, this);
    }

    @Override
    @NotNull
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
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

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity livingEntity) {
        return this.delegate.getDamageSource(livingEntity, () -> super.getDamageSource(livingEntity));
    }
}