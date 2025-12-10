package de.tomalbrc.filament.mixin.behaviour.container;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.animal.golem.CopperGolemAi;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CopperGolemAi.class)
public class CopperGolemAiMixin {
    @Inject(method = "method_72486", at = @At("RETURN"), cancellable = true)
    private static void filament$customContainerSupportWooden(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && blockState.getBlock() instanceof DecorationBlock decorationBlock && DecorationData.getFirstContainer(decorationBlock) != null)
            cir.setReturnValue(blockState.is(ConventionalBlockTags.WOODEN_CHESTS));
    }

    @Inject(method = "method_72482", at = @At("RETURN"), cancellable = true)
    private static void filament$shouldQueue(TransportItemsBetweenContainers.TransportItemTarget target, CallbackInfoReturnable<Boolean> cir) {
        if (target.blockEntity() instanceof DecorationBlockEntity decorationBlockEntity) {
            var containerLike = DecorationData.getFirstContainer(decorationBlockEntity.getMainBlockEntity());
            Container container;
            if (containerLike != null && (container = containerLike.container()) != null) {
                cir.setReturnValue(!container.getEntitiesWithContainerOpen().isEmpty());
            }
        }
    }
}
