package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
    @Shadow @Deprecated public abstract Holder.Reference<BlockEntityType<?>> builtInRegistryHolder();

    @Inject(method = "isValid", at = @At(value = "HEAD"), cancellable = true)
    private void filament$isValid(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        if (blockState.getBlock().isFilamentBlock()) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : blockState.getBlock().getBehaviours()) {
                if (behaviour.getValue() instanceof BlockBehaviourWithEntity<?> blockBehaviourWithEntity && BlockEntityType.class.cast(this) == blockBehaviourWithEntity.blockEntityType()) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}