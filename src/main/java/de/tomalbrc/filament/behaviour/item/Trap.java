package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> consumer, TooltipFlag tooltipFlag) {
        var bucketData = itemStack.get(DataComponents.BUCKET_ENTITY_DATA);
        if (bucketData != null && bucketData.copyTag().contains("Type")) {
            var tag = bucketData.copyTag();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.contains("Type") ? tag.getString("Type") : "minecraft:pig"));
            consumer.add(Component.literal("Contains ").append(Component.translatable(type.getDescriptionId()))); // todo: make "Contains " translateable?
        } else {
            if (this.config.requiredEffects != null) {
                consumer.add(Component.literal("Requires effects: "));
                for (int i = 0; i < this.config.requiredEffects.size(); i++) {
                    var e = this.config.requiredEffects.get(i);
                    consumer.add(Component.literal("› ").append(Component.translatable(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.get(e)).getDescriptionId()))); // todo: make "Contains " translateable?
                }
            }
            consumer.add(Component.literal("Chance: " + this.config.chance + "%"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player user, InteractionHand hand) {
        this.useTrapAndBreak(item, user, hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    public void useTrapAndBreak(Item item, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        player.startUsingItem(hand);

        if (this.config.useDuration > 0) player.getCooldowns().addCooldown(itemStack.getItem(), this.config.useDuration);

        player.awardStat(Stats.ITEM_USED.get(item));

        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
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
    public void modifyPolymerItemStack(Map<String, ResourceLocation> modelData, ItemStack original, ItemStack itemStack, TooltipFlag tooltipType, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        if (modelData != null && original.getItem() instanceof FilamentItem filamentItem) {
            // TODO: 1.21.1
            var cmd = filamentItem.getModelData().get(canSpawn(original) ? "trapped" : "default");
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, cmd.asComponent());
        }
    }

    private static boolean canSpawn(ItemStack useOnContext) {
        return useOnContext.get(DataComponents.BUCKET_ENTITY_DATA) != null && Objects.requireNonNull(useOnContext.get(DataComponents.BUCKET_ENTITY_DATA)).copyTag().contains("Type");
    }

    private void spawn(ServerLevel serverLevel, Player player, InteractionHand hand, ItemStack itemStack, BlockPos blockPos) {
        var bucketData = itemStack.get(DataComponents.BUCKET_ENTITY_DATA);
        if (bucketData != null) {
            var compoundTag = bucketData.copyTag();

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(compoundTag.getString("Type"))).orElse(EntityType.PIG);
            Entity entity = entityType.spawn(serverLevel, blockPos.above(1), MobSpawnType.BUCKET);
            if (entity instanceof Mob mob) {
                this.loadFromTag(mob, compoundTag);
            }

            itemStack.remove(DataComponents.BUCKET_ENTITY_DATA);
        }
    }

    public boolean canUseOn(Mob mob) {
        ResourceLocation mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (this.config.types != null) {
            var isType = this.config.types.contains(mobType);
            if (isType)
                return true;
        }
        if (this.config.tags != null) {
            for (ResourceLocation tag : this.config.tags) {
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
                var optional = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
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

        ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());

        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, (tag) -> {
            tag.putString("Type", resourceLocation.toString());
        });
    }

    public void loadFromTag(Mob mob, CompoundTag compoundTag) {
        // todo: write additional nbt, Bucketable not good enough
        // especially cobblemon (but then just use pokeballs or something)
        Bucketable.loadDefaultDataFromBucketTag(mob, compoundTag);
    }

    public static class Config {
        // allowed util types to trap
        public List<ResourceLocation> types = null;
        public List<ResourceLocation> tags = null;

        public List<ResourceLocation> requiredEffects = null;

        public int chance = 50;

        public int useDuration = 0;
    }
}
