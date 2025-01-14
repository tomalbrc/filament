package de.tomalbrc.filament.mixin.behaviour.falling_block;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.FallingBlock;
import de.tomalbrc.filament.block.SimpleBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    @Shadow private BlockState blockState;

    @Shadow private boolean cancelDrop;

    public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"), cancellable = true)
    private void filament$damageChangeBlockState(float f, float g, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (this.blockState.getBlock() instanceof SimpleBlock simpleBlock && simpleBlock.has(Behaviours.FALLING_BLOCK)) {
            FallingBlock behaviour = simpleBlock.get(Behaviours.FALLING_BLOCK);
            assert behaviour != null;

            var conf = behaviour.getConfig();
            if (conf.canBeDamaged) {
                int value = Mth.ceil(f - 1.0F);
                float h = (float)Math.min(Mth.floor((float)value * conf.damagePerDistance), conf.maxDamage);

                if (h > 0.0F && this.random.nextFloat() < conf.baseBreakChance + (float)value * conf.breakChancePerDistance) {
                    if (conf.damagedBlock == null) {
                        this.cancelDrop = true;
                    } else {
                        this.blockState = BuiltInRegistries.BLOCK.get(conf.damagedBlock).orElseThrow().value().withPropertiesOf(this.blockState);
                    }
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
