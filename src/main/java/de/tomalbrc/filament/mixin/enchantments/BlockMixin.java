package de.tomalbrc.filament.mixin.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import de.tomalbrc.filament.registry.EnchantmentRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

// For enchantments
@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("RETURN"),
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            cancellable = true)
    private static void dropLoot(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> ci) {
        if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentRegistry.INFERNAL_TOUCH, tool) != 0) {
            if (entity instanceof Player) {
                ArrayList<ItemStack> newDropList = new ArrayList<>();
                ci.getReturnValue().forEach(x ->
                        newDropList.add(level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(x), level)
                                .map(smeltingRecipe -> smeltingRecipe.value().getResultItem(level.registryAccess()))
                                .filter(itemStack -> !itemStack.isEmpty())
                                .map(itemStack -> {
                                    ItemStack copy = itemStack.copy();
                                    copy.setCount(x.getCount() * itemStack.getCount());
                                    return copy;
                                })
                                .orElse(x))
                );
                ci.setReturnValue(newDropList);
            }
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentRegistry.MAGNETIZED, tool) != 0) {
            if (entity instanceof Player playerEntity) {
                ArrayList<ItemStack> newDropList = new ArrayList<>(ci.getReturnValue());
                newDropList.removeIf(playerEntity::addItem);
                ci.setReturnValue(newDropList);
            }
        }
    }
}