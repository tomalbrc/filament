package de.tomalbrc.filament.data.properties;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    @NotNull
    public Block blockBase = Blocks.STONE;
    public boolean requiresTool = true;
    public float explosionResistance = Float.MIN_VALUE;
    public float destroyTime = Float.MIN_VALUE;

    public BlockStateMappedProperty<Boolean> isSuffocating = null;
    public BlockStateMappedProperty<Boolean> redstoneConductor = null;
    public BlockStateMappedProperty<Integer> lightEmission = null;

    public boolean transparent = false;
    public boolean allowsSpawning = false;
    public boolean replaceable = false;
    public boolean collision = true;

    public boolean solid = true;

    public PushReaction pushReaction = PushReaction.NORMAL;

    public ResourceLocation lootTable = null;

    public boolean virtual;
    public boolean showBreakParticles = true;

    public Sounds sounds;

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        props.sound(this.blockBase.defaultBlockState().getSoundType());
        if (this.sounds != null) {
            props.sound(this.sounds.asSoundType());
        }

        if (this.destroyTime != Float.MIN_VALUE) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != Float.MIN_VALUE) props.explosionResistance(this.explosionResistance);
        else if (this.destroyTime != Float.MIN_VALUE) props.explosionResistance(this.destroyTime);
        if (this.lightEmission != null) props.lightLevel((state) -> this.lightEmission.getOrDefault(state, 0));
        if (this.redstoneConductor != null) props.isRedstoneConductor((blockState, blockGetter, blockPos) -> this.redstoneConductor.getOrDefault(blockState, false));
        if (this.requiresTool) props.requiresCorrectToolForDrops();
        if (this.replaceable) props.replaceable();
        if (this.transparent) props.noOcclusion();
        if (!this.collision) props.noCollision();

        if (this.lootTable != null)
            props.overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable)));

        if (this.isSuffocating != null)
            props.isSuffocating((blockState, blockGetter, blockPos) -> this.isSuffocating.getValue(blockState));

        props.mapColor(this.blockBase.defaultMapColor());

        if (this.solid) props.forceSolidOn();
        else props.forceSolidOff();

        props.isValidSpawn((blockState, blockGetter, blockPos, entityType) -> this.allowsSpawning);
        props.pushReaction(this.pushReaction);

        return props;
    }

    public record Sounds(
            float volume,
            float pitch,
            @SerializedName("break") ResourceLocation breakSound,
            @SerializedName("step") ResourceLocation stepSound,
            @SerializedName("place") ResourceLocation placeSound,
            @SerializedName("hit") ResourceLocation hitSound,
            @SerializedName("fall") ResourceLocation fallSound
    ) {
        SoundType asSoundType() {
            return new SoundType(
                    volume,
                    pitch,
                    SoundEvent.createVariableRangeEvent(breakSound),
                    SoundEvent.createVariableRangeEvent(stepSound),
                    SoundEvent.createVariableRangeEvent(placeSound),
                    SoundEvent.createVariableRangeEvent(hitSound),
                    SoundEvent.createVariableRangeEvent(fallSound)
            );
        }
    }
}
