package de.tomalbrc.filament.data.properties;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    @NotNull
    private Block blockBase = Blocks.STONE;
    @Nullable private Boolean requiresTool = true;
    private Float explosionResistance = null;
    private Float destroyTime = null;

    private BlockStateMappedProperty<Boolean> isSuffocating = null;
    private BlockStateMappedProperty<Boolean> redstoneConductor = null;
    private BlockStateMappedProperty<Integer> lightEmission = null;

    private Boolean transparent = false;
    private Boolean allowsSpawning = false;
    private Boolean replaceable = false;
    private Boolean collision = true;

    private Boolean solid = true;

    private PushReaction pushReaction = PushReaction.NORMAL;

    private Identifier lootTable = null;

    public Boolean virtual;
    private Boolean showBreakParticles = true;

    private Sounds sounds;

    public @NotNull Block blockBase() {
        return blockBase;
    }

    public boolean requiresTool() {
        return requiresTool == null ? false : requiresTool;
    }

    public float explosionResistance() {
        return explosionResistance == null ? 0 : explosionResistance;
    }

    public float destroyTime() {
        return destroyTime == null ? 0 : destroyTime;
    }

    public BlockStateMappedProperty<Boolean> isSuffocating() {
        return isSuffocating;
    }

    public BlockStateMappedProperty<Boolean> redstoneConductor() {
        return redstoneConductor;
    }

    public BlockStateMappedProperty<Integer> lightEmission() {
        return lightEmission;
    }

    public boolean transparent() {
        return transparent == null ? false : transparent;
    }

    public boolean allowsSpawning() {
        return allowsSpawning == null ? false : allowsSpawning;
    }

    public boolean replaceable() {
        return replaceable == null ? false : replaceable;
    }

    public boolean collision() {
        return collision == null ? false : collision;
    }

    public boolean solid() {
        return solid  == null ? false : solid;
    }

    public PushReaction pushReaction() {
        return pushReaction;
    }

    public Identifier lootTable() {
        return lootTable;
    }

    public boolean virtual() {
        return virtual == null ? false : virtual;
    }

    public boolean showBreakParticles() {
        return showBreakParticles == null ? false : showBreakParticles;
    }

    public Sounds sounds() {
        return sounds;
    }

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        props.sound(this.blockBase.defaultBlockState().getSoundType());
        if (this.sounds != null) {
            props.sound(this.sounds.asSoundType());
        }

        if (this.destroyTime != null) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != null) props.explosionResistance(this.explosionResistance);
        else if (this.destroyTime != null) props.explosionResistance(this.destroyTime);
        if (this.lightEmission != null) props.lightLevel((state) -> this.lightEmission.getOrDefault(state, 0));
        if (this.redstoneConductor != null) props.isRedstoneConductor((blockState, blockGetter, blockPos) -> this.redstoneConductor.getOrDefault(blockState, false));
        if (this.requiresTool == Boolean.TRUE) props.requiresCorrectToolForDrops();
        if (this.replaceable == Boolean.TRUE) props.replaceable();
        if (this.transparent == Boolean.TRUE) props.noOcclusion();
        if (!this.collision == Boolean.TRUE) props.noCollision();

        if (this.lootTable != null)
            props.overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable)));

        if (this.isSuffocating != null)
            props.isSuffocating((blockState, blockGetter, blockPos) -> this.isSuffocating.getValue(blockState));

        props.mapColor(this.blockBase.defaultMapColor());

        if (this.solid == Boolean.TRUE) props.forceSolidOn();
        else props.forceSolidOff();

        props.isValidSpawn((blockState, blockGetter, blockPos, entityType) -> this.allowsSpawning);
        props.pushReaction(this.pushReaction);

        return props;
    }

    public record Sounds(
            float volume,
            float pitch,
            @SerializedName("break") Identifier breakSound,
            @SerializedName("step") Identifier stepSound,
            @SerializedName("place") Identifier placeSound,
            @SerializedName("hit") Identifier hitSound,
            @SerializedName("fall") Identifier fallSound
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
