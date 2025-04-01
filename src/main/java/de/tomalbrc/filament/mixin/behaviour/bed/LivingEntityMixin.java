package de.tomalbrc.filament.mixin.behaviour.bed;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract Optional<BlockPos> getSleepingPos();

    @Inject(method = "checkBedExists", at = @At(value = "HEAD"), cancellable = true)
    private void filament$checkBedExists(CallbackInfoReturnable<Boolean> cir) {
        var value = this.getSleepingPos().map((blockPos) -> this.level().getBlockState(blockPos).getBlock() instanceof DecorationBlock decorationBlock && decorationBlock.getDecorationData().behaviour().has(Behaviours.BED)).orElse(false);
        if (value == Boolean.TRUE) {
            cir.setReturnValue(true);
        }
    }
}
