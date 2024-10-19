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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;

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
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.onUseTick(level, livingEntity, itemStack, i);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.releaseUsing(itemStack, level, livingEntity, useDuration);
            }
        }
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                boolean didUse = itemBehaviour.useOnRelease(itemStack);
                if (didUse)
                    return true;
            }
        }
        return false;
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
        return 0;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                var val = itemBehaviour.getUseDuration(itemStack, livingEntity);
                if (val.isPresent())
                    return val.get();
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
        var stack = PolymerItemUtils.createItemStack(itemStack, tooltipType, packetContext);
        stack.set(DataComponents.ITEM_MODEL, this.getModel());

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
            if (behaviour.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.modifyPolymerItemStack(this.getModelMap(), itemStack, stack, tooltipType, packetContext.getRegistryWrapperLookup(), packetContext.getPlayer());
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
