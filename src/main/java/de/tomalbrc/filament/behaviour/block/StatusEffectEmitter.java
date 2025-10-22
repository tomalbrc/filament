package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StatusEffectEmitter implements BlockBehaviour<StatusEffectEmitter.Config>, AsyncTickingBlockBehaviour {
    private Config config;

    public StatusEffectEmitter(Config config) {
        this.config = config;
    }

    private MobEffectInstance decode(EffectElement element) {
        return new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.get(element.effect).orElseThrow(), element.duration, element.amplifier, element.ambient, element.visible, element.showIcon);
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public void tickAsync(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (config.enabled.getValue(blockState) && config.elements != null && !config.elements.isEmpty()) {
            for (EffectElement element : config.elements) {
                if (element.enabled.getValue(blockState) && ((serverLevel.getGameTime() % 1_000_000) + blockPos.asLong()) % element.interval.getValue(blockState) == 0) {
                    emit(blockState, serverLevel, blockPos, randomSource, element);
                }
            }
        }
    }

    private void emit(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, EffectElement element) {
        AABB aABB = (new AABB(blockPos)).inflate(element.radius.getValue(blockState)).expandTowards(0.0, element.ignoreHeight ? serverLevel.getHeight() : 0, 0.0);
        List<? extends LivingEntity> list;
        if (element.onlyPlayer) {
            list = serverLevel.getEntitiesOfClass(Player.class, aABB);
        }
        else {
            list = serverLevel.getEntitiesOfClass(LivingEntity.class, aABB);
        }

        for (LivingEntity entity : list) {
            entity.addEffect(decode(element));
        }
    }

    public static class Config {
        BlockStateMappedProperty<Boolean> enabled = BlockStateMappedProperty.of(true);
        public List<EffectElement> elements;
    }

    public static class EffectElement {
        public boolean onlyPlayer = true;
        public boolean ignoreHeight = false;
        BlockStateMappedProperty<Boolean> enabled = BlockStateMappedProperty.of(true);
        BlockStateMappedProperty<Integer> interval = BlockStateMappedProperty.of(1);
        BlockStateMappedProperty<Integer> radius = BlockStateMappedProperty.of(16);

        ResourceLocation effect = ResourceLocation.withDefaultNamespace("speed");
        int duration = 20;
        int amplifier = 0;
        boolean ambient = true;
        boolean visible = true;
        boolean showIcon = true;
    }
}