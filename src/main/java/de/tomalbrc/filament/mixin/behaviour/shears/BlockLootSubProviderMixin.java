package de.tomalbrc.filament.mixin.behaviour.shears;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.tomalbrc.filament.behaviour.item.Shears;
import de.tomalbrc.filament.util.mixin.ItemPredicateCustomCheck;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin implements LootTableSubProvider {
    @ModifyReturnValue(method = "hasShears", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ItemPredicate$Builder;of(Lnet/minecraft/core/HolderGetter;[Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/advancements/critereon/ItemPredicate$Builder;"))
    private ItemPredicate.Builder filament$customShears(ItemPredicate.Builder builder) {
        if (builder instanceof ItemPredicateCustomCheck customCheck) {
            customCheck.setCustomCheck(Shears::is);
        }
        return builder;
    }
}
