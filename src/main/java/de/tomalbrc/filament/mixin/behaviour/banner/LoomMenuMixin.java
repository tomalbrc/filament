package de.tomalbrc.filament.mixin.behaviour.banner;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.behaviour.Behaviours;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private HolderGetter<BannerPattern> patternGetter;

    protected LoomMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Shadow public abstract Slot getDyeSlot();

    @Shadow public abstract Slot getBannerSlot();

    @Shadow public abstract Slot getPatternSlot();

    @Inject(method = "getSelectablePatterns(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void filament$onGetSelectablePatterns(ItemStack itemStack, CallbackInfoReturnable<List<Holder<BannerPattern>>> cir) {
        if (itemStack.getItem().isFilamentItem() && itemStack.getItem().getBehaviours().has(Behaviours.BANNER_PATTERN)) {
            ResourceLocation resourceLocation = itemStack.getItem().getBehaviours().get(Behaviours.BANNER_PATTERN).getConfig().id;
            Holder<BannerPattern> res = this.patternGetter.get(ResourceKey.create(Registries.BANNER_PATTERN, resourceLocation)).orElseThrow();
            cir.setReturnValue(ImmutableList.of(res));
        }
    }

    @Inject(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    void filament$onQuickMoveStack(Player player, int i, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack, Slot slot, ItemStack itemStack2) {
        boolean canMove = !(i == this.getDyeSlot().index || i == this.getBannerSlot().index || i == this.getPatternSlot().index);
        if (canMove && itemStack2.getItem().isFilamentItem() && itemStack2.getItem().getBehaviours().has(Behaviours.BANNER_PATTERN)) {
            if (!this.moveItemStackTo(itemStack2, this.getPatternSlot().index, this.getPatternSlot().index + 1, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            cir.setReturnValue(itemStack2.copy());
        }
    }
}
