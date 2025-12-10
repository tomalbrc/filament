package de.tomalbrc.filament.mixin.behaviour.shears;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.tomalbrc.filament.behaviour.item.Shears;
import de.tomalbrc.filament.util.mixin.ItemPredicateCustomCheck;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin implements LootTableSubProvider {
    @ModifyReturnValue(method = "hasShears", at = @At(value = "RETURN", target = "Lnet/minecraft/advancements/criterion/ItemPredicate$Builder;of(Lnet/minecraft/core/HolderGetter;[Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/advancements/criterion/ItemPredicate$Builder;"))
    private LootItemCondition.Builder filament$customShears(LootItemCondition.Builder original) {
        if (original instanceof ItemPredicateCustomCheck customCheck) {
            customCheck.setCustomCheck(Shears::is);
        }
        return original;
    }
}
