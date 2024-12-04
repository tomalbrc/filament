package de.tomalbrc.filament.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA;
import static net.minecraft.core.component.DataComponents.ITEM_MODEL;

/**
 * Universal item, base for all filament items, with behaviour support
 * I wish BlockItem was an interface...
 */
public class SimpleItem extends BlockItem implements PolymerItem, BehaviourHolder {
    private ItemData itemData;
    protected ItemProperties properties;

    protected final Item vanillaItem;

    protected final BehaviourMap behaviours = new BehaviourMap();

    public BehaviourMap getBehaviours() {
        return this.behaviours;
    }

    public SimpleItem(Block block, Properties properties, ItemData itemData, Item vanillaItem) {
        super(block, properties);
        this.initBehaviours(itemData.behaviour());

        this.vanillaItem = vanillaItem;
        this.itemData = itemData;
        this.properties = itemData.properties();
    }

    public SimpleItem(Block block, Properties itemProperties, ItemProperties props, Item vanillaItem) {
        super(block, itemProperties);

        this.vanillaItem = vanillaItem;
        this.properties = props;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void verifyComponentsAfterLoad(ItemStack itemStack) {
        super.verifyComponentsAfterLoad(itemStack);

        if (this.itemData != null) {
            for (Map.Entry<DataComponentType<?>, JsonObject> entry : this.itemData.getAdditionalComponents().entrySet()) {
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
    @SuppressWarnings("NullableProblems")
    @Nullable // yes there is no block item for simple items
    public Block getBlock() {
        return super.getBlock();
    }

    @Override
    @NotNull
    public FeatureFlagSet requiredFeatures() {
        return this.getBlock() != null ? this.getBlock().requiredFeatures() : this.vanillaItem.requiredFeatures();
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.onUseTick(level, livingEntity, itemStack, i);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var res = itemBehaviour.releaseUsing(itemStack, level, livingEntity, useDuration);
                if (res) return true;
            }
        }
        return false;
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                boolean didUse = itemBehaviour.useOnRelease(itemStack);
                if (didUse) return true;
            }
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var val = itemBehaviour.getUseDuration(itemStack, livingEntity);
                if (val.isPresent()) return val.get();
            }
        }
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return this.components().has(DataComponents.TOOL);
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (this.components().has(DataComponents.TOOL))
            itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
            }
        }
        this.properties.appendHoverText(list);
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.vanillaItem != null ? this.vanillaItem : Items.PAPER;
    }

    @Override
    public final ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext packetContext) {
        ItemStack stack = PolymerItemUtils.createItemStack(itemStack, tooltipType, packetContext);

        ResourceLocation dataComponentModel = null;
        if (this.itemData != null && this.itemData.components().has(ITEM_MODEL)) {
            dataComponentModel = this.itemData.components().get(ITEM_MODEL);
        } else if (this.itemData != null) {
            if (this.itemData.itemModel() != null) {
                dataComponentModel = this.itemData.itemModel();
            } else {
                dataComponentModel = this.itemData.itemResource() == null ? itemData.vanillaItem().components().get(ITEM_MODEL) : null;
            }
        }
        if (dataComponentModel != null) stack.set(ITEM_MODEL, dataComponentModel);

        if (!itemStack.has(CUSTOM_MODEL_DATA)) {
            CustomModelData customModelData = this.itemData != null && this.itemData.components().has(CUSTOM_MODEL_DATA) ? this.itemData.components().get(CUSTOM_MODEL_DATA) : getModel();
            if (customModelData != null) stack.set(CUSTOM_MODEL_DATA, customModelData);
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.modifyPolymerItemStack(this.getModelMap(), itemStack, stack, tooltipType, packetContext.getRegistryWrapperLookup(), packetContext.getPlayer());
            }
        }

        return stack;
    }

    protected Map<String, ResourceLocation> getModelMap() {
        return this.itemData.itemResource() == null ? Map.of() : Objects.requireNonNull(this.itemData.itemResource()).models();
    }

    @Nullable
    protected CustomModelData getModel() {
        return null;
    }

    @Override
    @NotNull
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        var res = super.use(level, user, hand);
        if (res.consumesAction()) {
            return res;
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                res = itemBehaviour.use(this, level, user, hand);
                if (res.consumesAction()) {
                    return res;
                }
            }
        }

        return res;
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var res = itemBehaviour.useOn(useOnContext);
                if (res.consumesAction()) {
                    return res;
                }
            }
        }

        if (this.getBlock() instanceof SimpleBlock) {
            var res = super.useOn(useOnContext);
            if (res.consumesAction()) {
                return res;
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!super.placeBlock(context, state)) {
            return false;
        }

        if (context.getPlayer() instanceof ServerPlayer player) {
            Util.handleBlockPlaceEffects(player, context.getHand(), context.getClickedPos(), state.getSoundType());
        }

        return true;
    }
}
