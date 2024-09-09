package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Mob trap
 */
public class Trap implements ItemBehaviour<Trap.TrapConfig> {
    private final TrapConfig config;

    public Trap(TrapConfig config) {
        this.config = config;
    }

    @Override
    public TrapConfig getConfig() {
        return config;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemStack.get(DataComponents.BUCKET_ENTITY_DATA) != null && itemStack.get(DataComponents.BUCKET_ENTITY_DATA).contains("Type")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(itemStack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag().getString("Type")));
            list.add(Component.literal("Contains ").append(Component.translatable(type.getDescriptionId()))); // todo: make "Contains " translateable?
        } else {
            if (this.config.requiredEffects != null) {
                list.add(Component.literal("Requires effects: "));
                for (int i = 0; i < this.config.requiredEffects.size(); i++) {
                    var e = this.config.requiredEffects.get(i);
                    list.add(Component.literal("› ").append(Component.translatable(BuiltInRegistries.MOB_EFFECT.get(e).getDescriptionId()))); // todo: make "Contains " translateable?
                }
            }
            list.add(Component.literal("Chance: " + this.config.chance + "%"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        this.useTrapAndBreak(item, user, hand);
        return InteractionResultHolder.consume(itemStack);
    }

    public void useTrapAndBreak(Item item, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        player.startUsingItem(hand);

        if (this.config.useDuration > 0) player.getCooldowns().addCooldown(item, this.config.useDuration);

        player.awardStat(Stats.ITEM_USED.get(item));

        Util.damageAndBreak(1, itemStack, player, Player.getSlotForHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        // TODO: maybe check for lava / safe ground?
        if (useOnContext.getPlayer() != null && canSpawn(useOnContext.getItemInHand()) && useOnContext.getLevel() instanceof ServerLevel serverLevel) {
            this.spawn(serverLevel, useOnContext.getPlayer(), useOnContext.getHand(), useOnContext.getItemInHand(), useOnContext.getClickedPos());
            this.useTrapAndBreak(useOnContext.getItemInHand().getItem(), useOnContext.getPlayer(), useOnContext.getHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public int modifyPolymerCustomModelData(Map<String, PolymerModelData> modelData, ItemStack itemStack, @Nullable ServerPlayer player) {
        return modelData != null ? canSpawn(itemStack) ? modelData.get("trapped").value() : modelData.get("default").value() : -1;
    }

    private static boolean canSpawn(ItemStack useOnContext) {
        return useOnContext.get(DataComponents.BUCKET_ENTITY_DATA) == null ? false : useOnContext.get(DataComponents.BUCKET_ENTITY_DATA).contains("Type");
    }

    private void spawn(ServerLevel serverLevel, Player player, InteractionHand hand, ItemStack itemStack, BlockPos blockPos) {
        var compoundTag = itemStack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag();

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(compoundTag.getString("Type")));
        Entity entity = entityType.spawn(serverLevel, blockPos.above(1), MobSpawnType.BUCKET);
        if (entity instanceof Mob mob) {
            this.loadFromTag(mob, compoundTag);
        }

        itemStack.remove(DataComponents.BUCKET_ENTITY_DATA);
    }

    public boolean canUseOn(Mob mob) {
        ResourceLocation mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return this.config.types.contains(mobType);
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
        Bucketable.loadDefaultDataFromBucketTag(mob, compoundTag);
    }

    public static class TrapConfig {
        // allowed util types to trap
        public List<ResourceLocation> types = null;

        public List<ResourceLocation> requiredEffects = null;

        public int chance = 50;

        public int useDuration = 0;
    }
}
