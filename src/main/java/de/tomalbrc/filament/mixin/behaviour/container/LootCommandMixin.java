package de.tomalbrc.filament.mixin.behaviour.container;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootCommand.class)
public class LootCommandMixin {
    @Inject(method = "getContainer", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/exceptions/Dynamic3CommandExceptionType;create(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/brigadier/exceptions/CommandSyntaxException;", shift = At.Shift.BEFORE), cancellable = true)
    private static void filament$getFilamentContainer(CommandSourceStack commandSourceStack, BlockPos blockPos, CallbackInfoReturnable<Container> cir, @Local BlockEntity blockEntity) {
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            var containerLike = decorationBlockEntity.getDecorationData().getFirstContainer(decorationBlockEntity);
            if (containerLike != null) {
                var container = containerLike.container();
                if (container != null)
                    cir.setReturnValue(container);
            }
        }
    }
}
