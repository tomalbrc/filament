package de.tomalbrc.filament.item;

import de.tomalbrc.filament.behaviours.item.Trap;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Util;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrapItem extends SimpleItem {
    public TrapItem(Properties properties, ItemData itemData) {
        super(properties, itemData);
    }

    private Trap.TrapConfig trapData() {
        Trap.TrapConfig trap = this.itemData.behaviourConfig().get(Constants.Behaviours.TRAP);
        return trap;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemStack.get(DataComponents.BUCKET_ENTITY_DATA) != null && itemStack.get(DataComponents.BUCKET_ENTITY_DATA).contains("Type")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(itemStack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag().getString("Type")));
            list.add(Component.literal("Contains ").append(Component.translatable(type.getDescriptionId()))); // todo: make "Contains " translateable?
        }
        else {
            if (this.trapData().requiredEffects != null) {
                list.add(Component.literal("Requires effects: "));
                for (int i = 0; i < this.trapData().requiredEffects.size(); i++) {
                    var e = this.trapData().requiredEffects.get(i);
                    list.add(Component.literal("› ").append(Component.translatable(BuiltInRegistries.MOB_EFFECT.get(e).getDescriptionId()))); // todo: make "Contains " translateable?
                }
            }
            list.add(Component.literal("Chance: " + this.trapData().chance));
        }

        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayer player) {
        return this.modelData != null ? canSpawn(itemStack) ? this.modelData.get("trapped").value() : this.modelData.get("default").value() : -1;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        super.use(level, player, interactionHand);

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

        Trap.TrapConfig trap = this.itemData.behaviourConfig().get(Constants.Behaviours.TRAP);
        player.startUsingItem(hand);

        if (trap.useDuration > 0) player.getCooldowns().addCooldown(this, trap.useDuration);

        player.awardStat(Stats.ITEM_USED.get(this));

        Util.damageAndBreak(1, itemStack, player, Player.getSlotForHand(hand));
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        // TODO: maybe check for lava / safe ground?
        if (useOnContext.getPlayer() != null && canSpawn(useOnContext.getItemInHand()) && useOnContext.getLevel() instanceof ServerLevel serverLevel) {
            this.spawn(serverLevel, useOnContext.getPlayer(), useOnContext.getHand(), useOnContext.getItemInHand(), useOnContext.getClickedPos());
            use(useOnContext.getPlayer(), useOnContext.getHand());
        }

        return InteractionResult.PASS;
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
        Trap.TrapConfig trap = this.trapData();
        ResourceLocation mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return trap.types.contains(mobType);
    }

    public boolean canSave(Mob mob) {
        boolean hasEffects = true;
        if (this.trapData().requiredEffects != null) {
            hasEffects = false;
            for (int i = 0; i < this.trapData().requiredEffects.size(); i++) {
                var effectId = this.trapData().requiredEffects.get(i);
                var optional = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
                if (optional.isPresent() && mob.hasEffect(optional.get())) {
                    hasEffects = true;
                }
            }
        }

        return hasEffects && mob.getRandom().nextInt(100) <= this.trapData().chance;
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
}
