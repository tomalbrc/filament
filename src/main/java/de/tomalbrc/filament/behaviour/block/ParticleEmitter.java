package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.properties.RangedValue;
import de.tomalbrc.filament.data.properties.RangedVector3f;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class ParticleEmitter implements BlockBehaviour<ParticleEmitter.Config>, AsyncTickingBlockBehaviour {
    private Config config;
    transient private final Map<ParticleEmitterElement, ParticleOptions> decoded = new Reference2ObjectOpenHashMap<>();

    public ParticleEmitter(Config config) {
        this.config = config;
    }

    private ParticleOptions decode(ParticleEmitterElement element) {
        var res = ParticleTypes.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, Filament.SERVER.registryAccess()), element.particle);
        if (res.isSuccess()) {
            return res.getOrThrow().getFirst();
        }

        return ParticleTypes.ANGRY_VILLAGER;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public void tickAsync(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (config.enabled.getValue(blockState) && config.elements != null && !config.elements.isEmpty()) {
            for (ParticleEmitterElement element : config.elements) {
                if (element.enabled.getValue(blockState) && ((serverLevel.getGameTime() % 1_000_000) + blockPos.asLong()) % element.interval.getValue(blockState) == 0 && decoded.computeIfAbsent(element, key -> decode(element)) != null) {
                    emit(blockState, serverLevel, blockPos, randomSource, element);
                }
            }
        }
    }

    private void emit(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, ParticleEmitterElement element) {
        Vec3 pos = blockPos.getCenter();
        Vector3f offset = element.offset.random(randomSource);
        Vector3f delta = element.delta.random(randomSource);
        serverLevel.sendParticles(
                decoded.get(element),
                pos.x + offset.x, pos.y + offset.y, pos.z + offset.z,
                (int)element.count.randomValue(randomSource),
                delta.x, delta.y, delta.z,
                element.speed.randomValue(randomSource));
    }

    public static class Config {
        BlockStateMappedProperty<Boolean> enabled = BlockStateMappedProperty.of(true);
        public List<ParticleEmitterElement> elements;
    }

    public static class ParticleEmitterElement {
        BlockStateMappedProperty<Boolean> enabled = BlockStateMappedProperty.of(true);
        BlockStateMappedProperty<Integer> interval = BlockStateMappedProperty.of(1);
        JsonElement particle;
        RangedVector3f offset = new RangedVector3f();
        RangedValue count = new RangedValue(0);
        RangedVector3f delta = new RangedVector3f();
        RangedValue speed = new RangedValue(0);
    }
}