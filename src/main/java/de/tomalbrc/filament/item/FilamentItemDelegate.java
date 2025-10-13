package de.tomalbrc.filament.item;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

// shared behaviour delegate for SimpleItem and SimpleBlockItem - DecorationItem just inherits SimpleItem
public class FilamentItemDelegate {
    private final BehaviourHolder holder;

    public FilamentItemDelegate(BehaviourHolder holder) {
        this.holder = holder;
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.onUseTick(level, livingEntity, itemStack, i);
            }
        }
    }

    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useDuration, Supplier<Boolean> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                if (itemBehaviour.releaseUsing(itemStack, level, livingEntity, useDuration)) {
                    return true;
                }
            }
        }
        return fallback.get();
    }

    public boolean useOnRelease(ItemStack itemStack, Supplier<Boolean> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                if (itemBehaviour.useOnRelease(itemStack)) {
                    return true;
                }
            }
        }
        return fallback.get();
    }

    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity, Supplier<Integer> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                Optional<Integer> val = itemBehaviour.getUseDuration(itemStack, livingEntity);
                if (val.isPresent()) return val.get();
            }
        }
        return fallback.get();
    }

    public ItemUseAnimation getUseAnimation(ItemStack itemStack, Supplier<ItemUseAnimation> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                ItemUseAnimation anim = itemBehaviour.getUseAnimation(itemStack);
                if (anim != ItemUseAnimation.NONE) return anim;
            }
        }
        return fallback.get();
    }

    public void hurtEnemy(ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.hurtEnemy(itemStack, attacker, target);
            }
        }
    }

    public void postHurtEnemy(ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.postHurtEnemy(itemStack, attacker, target);
            }
        }
        if (itemStack.has(DataComponents.TOOL))
            itemStack.hurtAndBreak(1, target, attacker.getEquipmentSlotForItem(itemStack));
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
            }
        }
    }

    public void modifyPolymerItemStack(Map<String, ResourceLocation> modelMap, ItemStack original, ItemStack polymer, TooltipFlag tooltipFlag, HolderLookup.Provider lookup, ServerPlayer player) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                itemBehaviour.modifyPolymerItemStack(modelMap, original, polymer, tooltipFlag, lookup, player);
            }
        }
    }

    public InteractionResult use(Item item, Level level, Player player, InteractionHand hand, Supplier<InteractionResult> fallback) {
        InteractionResult result = fallback.get();
        if (result.consumesAction()) {
            return result;
        }

        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                result = itemBehaviour.use(item, level, player, hand);
                if (result.consumesAction()) {
                    return result;
                }
            }
        }
        return result;
    }

    public InteractionResult useOn(UseOnContext context, Supplier<InteractionResult> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                InteractionResult result = itemBehaviour.useOn(context);
                if (result.consumesAction()) {
                    return result;
                }
            }
        }
        return fallback.get();
    }

    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos pos, LivingEntity entity, Supplier<Boolean> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                if (itemBehaviour.mineBlock(itemStack, level, state, pos, entity)) {
                    return true;
                }
            }
        }
        return fallback.get();
    }

    public float getAttackDamageBonus(Entity entity, float base, DamageSource source, Supplier<Float> fallback) {
        float bonus = fallback.get();
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                bonus += itemBehaviour.getAttackDamageBonus(entity, base, source);
            }
        }
        return bonus;
    }

    public DamageSource getDamageSource(LivingEntity livingEntity, Supplier<DamageSource> fallback) {
        for (Map.Entry<BehaviourType<?, ?>, Behaviour<?>> entry : holder.getBehaviours()) {
            if (entry.getValue() instanceof ItemBehaviour<?> itemBehaviour) {
                DamageSource ds = itemBehaviour.getDamageSource(livingEntity);
                if (ds != null) return ds;
            }
        }
        return fallback.get();
    }
}