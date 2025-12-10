package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mob trap
 */
public class Trap implements ItemBehaviour<Trap.Config> {
    private final Config config;

    public Trap(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Trap.Config getConfig() {
        return config;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        var bucketData = itemStack.get(DataComponents.BUCKET_ENTITY_DATA);
        if (bucketData != null && bucketData.copyTag().contains("Type")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(bucketData.copyTag().getString("Type").orElse("minecraft:pig")));
            consumer.accept(Component.literal("Contains ").append(Component.translatable(type.getDescriptionId()))); // todo: make "Contains " translateable?
        } else {
            if (this.config.requiredEffects != null) {
                consumer.accept(Component.literal("Requires effects: "));
                for (int i = 0; i < this.config.requiredEffects.size(); i++) {
                    var e = this.config.requiredEffects.get(i);
                    consumer.accept(Component.literal("› ").append(Component.translatable(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getValue(e)).getDescriptionId()))); // todo: make "Contains " translateable?
                }
            }
            consumer.accept(Component.literal("Chance: " + this.config.chance + "%"));
        }
    }

    @Override
    public InteractionResult use(Item item, Level level, Player user, InteractionHand hand) {
        this.useTrapAndBreak(item, user, hand);
        return InteractionResult.CONSUME;
    }

    public void useTrapAndBreak(Item item, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        player.startUsingItem(hand);

        if (this.config.useDuration > 0) player.getCooldowns().addCooldown(itemStack, this.config.useDuration);

        player.awardStat(Stats.ITEM_USED.get(item));

        itemStack.hurtAndBreak(1, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (useOnContext.getPlayer() != null && canSpawn(useOnContext.getItemInHand()) && useOnContext.getLevel() instanceof ServerLevel serverLevel) {
            this.spawn(serverLevel, useOnContext.getPlayer(), useOnContext.getHand(), useOnContext.getItemInHand(), useOnContext.getClickedPos());
            this.useTrapAndBreak(useOnContext.getItemInHand().getItem(), useOnContext.getPlayer(), useOnContext.getHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void modifyPolymerItemStack(Map<String, Identifier> modelData, ItemStack original, ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        if (modelData != null) {
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(canSpawn(original) ? "trapped" : "default"), List.of()));
        }
    }

    private static boolean canSpawn(ItemStack useOnContext) {
        return useOnContext.get(DataComponents.BUCKET_ENTITY_DATA) != null && Objects.requireNonNull(useOnContext.get(DataComponents.BUCKET_ENTITY_DATA)).copyTag().contains("Type");
    }

    private void spawn(ServerLevel serverLevel, Player player, InteractionHand hand, ItemStack itemStack, BlockPos blockPos) {
        var bucketData = itemStack.get(DataComponents.BUCKET_ENTITY_DATA);
        if (bucketData != null) {
            var compoundTag = bucketData.copyTag();

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(compoundTag.getString("Type").orElse("minecraft:pig")));
            Entity entity = entityType.spawn(serverLevel, blockPos.above(1), EntitySpawnReason.BUCKET);
            if (entity instanceof Mob mob) {
                this.loadFromTag(mob, compoundTag);
            }

            itemStack.remove(DataComponents.BUCKET_ENTITY_DATA);
        }
    }

    public boolean canUseOn(Mob mob) {
        Identifier mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (this.config.types != null) {
            var isType = this.config.types.contains(mobType);
            if (isType)
                return true;
        }
        if (this.config.tags != null) {
            for (Identifier tag : this.config.tags) {
                if (mob.getType().is(TagKey.create(Registries.ENTITY_TYPE, tag))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canSave(Mob mob) {
        boolean hasEffects = true;
        if (this.config.requiredEffects != null) {
            hasEffects = false;
            for (int i = 0; i < this.config.requiredEffects.size(); i++) {
                var effectId = this.config.requiredEffects.get(i);
                var optional = BuiltInRegistries.MOB_EFFECT.get(effectId);
                if (optional.isPresent() && mob.hasEffect(optional.get())) {
                    hasEffects = true;
                }
            }
        }

        return hasEffects && mob.getRandom().nextInt(100) <= this.config.chance;
    }

    public void saveToTag(Mob mob, ItemStack itemStack) {
        // todo: read additional nbt, Bucketable not good enough
        Bucketable.saveDefaultDataToBucketTag(mob, itemStack);

        Identifier Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());

        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, (tag) -> {
            tag.putString("Type", Identifier.toString());
        });
    }

    public void loadFromTag(Mob mob, CompoundTag compoundTag) {
        // todo: write additional nbt, Bucketable not good enough
        // especially cobblemon (but then just use pokeballs or something)
        Bucketable.loadDefaultDataFromBucketTag(mob, compoundTag);
    }

    public static class Config {
        // allowed util types to trap
        public List<Identifier> types = null;
        public List<Identifier> tags = null;

        public List<Identifier> requiredEffects = null;

        public int chance = 50;

        public int useDuration = 0;
    }
}
