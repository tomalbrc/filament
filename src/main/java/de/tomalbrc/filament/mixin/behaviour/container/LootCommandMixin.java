package de.tomalbrc.filament.mixin.behaviour.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootCommand.class)
public class LootCommandMixin {
    @WrapOperation(method = "blockDistribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/item/BlockItemAccessor;getContainer(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/core/BlockPos;Lcom/mojang/brigadier/exceptions/Dynamic3CommandExceptionType;)Lnet/minecraft/world/Container;"))
    private static Container filament$getFilamentContainer(CommandSourceStack source, BlockPos pos, Dynamic3CommandExceptionType exceptionType, Operation<Container> original) {
        var blockEntity = source.getLevel().getBlockEntity(pos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            var containerLike = DecorationData.getFirstContainer(decorationBlockEntity);
            if (containerLike != null) {
                var container = containerLike.container();
                if (container != null)
                    return container;
            }
        }

        return original.call(source, pos, exceptionType);
    }
}
