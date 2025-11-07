package de.tomalbrc.filament.entity.skill;

import net.minecraft.world.InteractionResult;

public record ExecutionResult(InteractionResult result, int delay) {
    public static ExecutionResult NULL = new ExecutionResult(InteractionResult.PASS, 0);
    public static ExecutionResult CONSUME = new ExecutionResult(InteractionResult.CONSUME, 0);

    public static ExecutionResult delayed(int delay) {
        return new ExecutionResult(InteractionResult.PASS, delay);
    }
}
