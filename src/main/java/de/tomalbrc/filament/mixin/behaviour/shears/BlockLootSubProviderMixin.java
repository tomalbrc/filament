package de.tomalbrc.filament.mixin.behaviour.shears;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.ItemPredicateCustomCheck;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin implements LootTableSubProvider {
    @Shadow @Final protected HolderLookup.Provider registries;

    @ModifyReturnValue(method = "hasShears", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ItemPredicate$Builder;of(Lnet/minecraft/core/HolderGetter;[Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/advancements/critereon/ItemPredicate$Builder;"))
    private ItemPredicate.Builder filament$customShears(ItemPredicate.Builder builder) {
        if (builder instanceof ItemPredicateCustomCheck customCheck) {
            customCheck.setCustomCheck(itemStack -> itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.SHEARS));
        }
        return builder;
    }
}
