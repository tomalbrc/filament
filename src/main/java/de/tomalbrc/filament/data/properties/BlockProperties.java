package de.tomalbrc.filament.data.properties;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    public static final BlockProperties EMPTY = new BlockProperties();

    @NotNull
    public Block blockBase = Blocks.STONE;
    public boolean requiresTool = true;
    public float explosionResistance = Float.MIN_VALUE;
    public float destroyTime = Float.MIN_VALUE;
    public BlockStateMappedProperty<Boolean> redstoneConductor = null;

    public BlockStateMappedProperty<Integer> lightEmission = null;

    public boolean transparent = false;
    public boolean allowsSpawning = false;
    public boolean replaceable = false;
    public boolean collision = true;

    public boolean solid = true;

    public PushReaction pushReaction = PushReaction.NORMAL;

    public ResourceLocation lootTable = null;

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        props.sound(this.blockBase.defaultBlockState().getSoundType());

        if (this.destroyTime != Float.MIN_VALUE) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != Float.MIN_VALUE) props.explosionResistance(this.explosionResistance);
        else if (this.destroyTime != Float.MIN_VALUE) props.explosionResistance(this.destroyTime);
        if (this.mayBeLightSource()) props.lightLevel((state) -> this.lightEmission.getOrDefault(state, 0));
        if (this.mayBeRedstoneConductor()) props.isRedstoneConductor((blockState, blockGetter, blockPos) -> this.redstoneConductor.getOrDefault(blockState, false));
        if (this.requiresTool) props.requiresCorrectToolForDrops();
        if (this.replaceable) props.replaceable();
        if (this.transparent) props.noOcclusion();
        if (!this.collision) props.noCollission();

        if (this.lootTable != null) props.overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable)));

        props.mapColor(blockBase.defaultMapColor());

        if (this.solid) props.forceSolidOn();
        else props.forceSolidOff();

        props.isValidSpawn((blockState,blockGetter,blockPos,entityType) -> this.allowsSpawning);
        props.pushReaction(this.pushReaction);

        return props.dynamicShape();
    }

    public boolean mayBeRedstoneConductor() {
        return this.redstoneConductor != null && (this.redstoneConductor.isMap() || (this.redstoneConductor.getRawValue() != null && this.redstoneConductor.getRawValue()));
    }

    public boolean mayBeLightSource() {
        return this.lightEmission != null && (this.lightEmission.isMap() || (this.lightEmission.getRawValue() != null && this.lightEmission.getRawValue() > 0));
    }
}
