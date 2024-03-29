package de.tomalbrc.filament.item;

import de.tomalbrc.filament.data.behaviours.item.Trap;
import de.tomalbrc.filament.data.ItemData;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrapItem extends SimpleItem {
    public TrapItem(Properties properties, ItemData itemData) {
        super(properties, itemData);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (itemStack.getOrCreateTag().contains("Type")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(itemStack.getOrCreateTag().getString("Type")));
            tooltip.add(Component.literal("Contains ").append(Component.translatable(type.getDescriptionId()))); // todo: make "Contains " translateable?
        }
        super.appendHoverText(itemStack, level, tooltip, tooltipFlag);
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        return this.modelData != null ? canSpawn(itemStack) ? this.modelData.get("trapped").value() : this.modelData.get("default").value() : -1;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (this.itemData.isTrap()) {
            this.use(player, interactionHand);
            return InteractionResultHolder.consume(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    public void use(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        itemStack.hurtAndBreak(1, player, o -> player.broadcastBreakEvent(hand));

        Trap trap = this.itemData.behaviour().trap;
        player.startUsingItem(hand);

        if (trap.useDuration > 0) player.getCooldowns().addCooldown(this, trap.useDuration);

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        // TODO: maybe check for lava / safe ground?
        if (useOnContext.getPlayer() != null && canSpawn(useOnContext.getItemInHand()) && useOnContext.getLevel() instanceof ServerLevel serverLevel) {
            this.spawn(serverLevel, useOnContext.getItemInHand(), useOnContext.getClickedPos());
            use(useOnContext.getPlayer(), useOnContext.getHand());
        }

        return InteractionResult.PASS;
    }

    private static boolean canSpawn(ItemStack useOnContext) {
        return useOnContext.getOrCreateTag().contains("Type");
    }

    private void spawn(ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(itemStack.getOrCreateTag().getString("Type")));
        Entity entity = entityType.spawn(serverLevel, blockPos.above(1), MobSpawnType.BUCKET);
        if (entity instanceof Mob mob) {
            this.loadFromTag(mob, itemStack.getOrCreateTag());
        }

        int damage = itemStack.getDamageValue();
        itemStack.setTag(null);
        itemStack.setDamageValue(damage);
    }

    public boolean canUseOn(Mob mob) {
        Trap trap = this.itemData.behaviour().trap;
        ResourceLocation mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return trap.types.contains(mobType);
    }

    public boolean canSave(Mob mob) {
        if (mob.hasEffect(MobEffects.POISON) || mob.hasEffect(MobEffects.WEAKNESS) || mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            return mob.getRandom().nextInt(100) > 75;
        }

        return mob.getRandom().nextInt(10_000) == 420;
    }

    public void saveToTag(Mob mob, ItemStack itemStack) {
        // todo: read additional nbt, Bucketable not good enough
        Bucketable.saveDefaultDataToBucketTag(mob, itemStack);

        ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        itemStack.getOrCreateTag().putString("Type", resourceLocation.toString());
    }

    public void loadFromTag(Mob mob, CompoundTag compoundTag) {
        // todo: write additional nbt, Bucketable not good enough
        Bucketable.loadDefaultDataFromBucketTag(mob, compoundTag);
    }
}
