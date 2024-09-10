package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.block.SimpleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(StateDefinition.Builder.class)
public class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
    @Shadow @Final private O owner;

    @SuppressWarnings("unchecked")
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    void filament$abortCreate(Function<O, S> function, StateDefinition.Factory<O, S> factory, CallbackInfoReturnable<StateDefinition<O, S>> cir) {
        // we have to prevent the code from creating a StateDefinition since it won't contain the properties form the behaviours yet
        if (this.owner instanceof SimpleBlock simpleBlock && !simpleBlock.hasData()) { // only if the block doesn't have data set-up yet
            cir.setReturnValue((StateDefinition<O, S>) Blocks.STONE.getStateDefinition());
        }
    }
}
