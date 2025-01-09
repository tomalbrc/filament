package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class FishingRod implements ItemBehaviour<FishingRod.Config>, ItemPredicateModelProvider {
    private final Config config;

    public FishingRod(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public FishingRod.Config getConfig() {
        return config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                int i = player.fishing.retrieve(itemStack);
                itemStack.hurtAndBreak(i, player, LivingEntity.getSlotForHand(interactionHand));
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (level instanceof ServerLevel serverLevel) {
                int lureSpeed = (int) (EnchantmentHelper.getFishingTimeReduction(serverLevel, itemStack, player) * 20.0F);
                int luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, itemStack, player);
                level.addFreshEntity(new FishingHook(player, level, luck, lureSpeed));
            }

            player.awardStat(Stats.ITEM_USED.get(item));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void generate(ResourceLocation id, ItemResource itemResource) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder ->
            ItemAssetGenerator.createFishingRod(
                resourcePackBuilder, id,
                itemResource
            )
        );
    }

    public static class Config {}
}