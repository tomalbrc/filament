package de.tomalbrc.filament.item;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
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
        this.initBehaviours(itemData.behaviourConfig());

        this.vanillaItem = vanillaItem;
        this.itemData = itemData;
        this.properties = itemData.properties();
    }

    public SimpleItem(Block block, Item.Properties itemProperties, ItemProperties props, Item vanillaItem) {
        super(block, itemProperties);

        this.vanillaItem = vanillaItem;
        this.properties = props;
    }

    @Override
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
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayer player) {
        return this.vanillaItem != null ? this.vanillaItem : Items.PAPER;
    }

    @Override
    public final ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        var stack = PolymerItemUtils.createItemStack(itemStack, tooltipType, lookup, player);
        stack.set(DataComponents.ITEM_MODEL, this.getModel());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.modifyPolymerItemStack(this.getModelMap(), itemStack, stack, tooltipType, lookup, player);
            }
        }

        return stack;
    }

    protected Map<String, ResourceLocation> getModelMap() {
        return this.itemData.itemResource() == null ? Map.of() : this.itemData.itemResource().models();
    }

    protected ResourceLocation getModel() {
        if (this.itemData.itemResource() != null)
            return this.itemData.itemResource().models().get("default");

        return vanillaItem.components().get(DataComponents.ITEM_MODEL);
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
