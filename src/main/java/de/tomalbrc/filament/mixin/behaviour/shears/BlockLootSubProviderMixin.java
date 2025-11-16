package de.tomalbrc.filament.mixin.behaviour.shears;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.tomalbrc.filament.behaviour.item.Shears;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin implements LootTableSubProvider {
    @ModifyReturnValue(method = "hasShearsOrSilkTouch", at = @At(value = "RETURN"))
    private LootItemCondition.Builder filament$customShears(LootItemCondition.Builder original) {
        if (original instanceof LootItemCondition.Builder) {
            return original.or(MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(new ItemSubPredicate.Type<>(null), Shears::is)));
        }
        return original;
    }
}
