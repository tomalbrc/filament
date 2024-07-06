package de.tomalbrc.filament.mixin.enchantments;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

// For enchantments
@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("RETURN"),
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            cancellable = true)
    private static void dropLoot(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> ci) {
        if (!FilamentConfig.getInstance().enchantments) {
            return;
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentRegistry.INFERNAL_TOUCH, tool) != 0) {
            if (entity instanceof Player) {
                List<ItemStack> newDropList = new ObjectArrayList<>();
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
                List<ItemStack> newDropList = new ObjectArrayList<>(ci.getReturnValue());
                newDropList.removeIf(playerEntity::addItem);
                ci.setReturnValue(newDropList);
            }
        }
    }
}