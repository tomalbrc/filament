package de.tomalbrc.filament.mixin.trim;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"))
    public void filament$trimTagCheck(TagKey<Item> tagKey, CallbackInfoReturnable<Boolean> cir) {
        ItemStack _this = ItemStack.class.cast(this);
        if (tagKey == ItemTags.TRIMMABLE_ARMOR && !(_this.getItem() instanceof PolymerItem) && _this.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.CHAIN) {
            cir.setReturnValue(false);
        }

    }
}
