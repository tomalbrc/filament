package de.tomalbrc.filament.data.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    @NotNull
    public Block blockBase = Blocks.STONE;
    public boolean requiresTool = false;
    public float explosionResistance = Float.MIN_VALUE;
    public float destroyTime = Float.MIN_VALUE;
    public boolean redstoneConductor = true;

    public int lightEmission = Integer.MIN_VALUE;

    public boolean transparent = false;
    public boolean allowsSpawning = false;
    public boolean replaceable = false;

    public PushReaction pushReaction = PushReaction.NORMAL;

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(this.blockBase);

        if (this.destroyTime != Float.MIN_VALUE) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != Float.MIN_VALUE) props.explosionResistance(this.explosionResistance);
        if (this.isLightSource()) props.lightLevel((state) -> this.lightEmission);
        if (this.isLightSource()) props.lightLevel((state) -> this.lightEmission);
        props.isRedstoneConductor((a,b,c) -> this.redstoneConductor);
        if (this.requiresTool) props.requiresCorrectToolForDrops();
        if (this.transparent) props.noOcclusion();
        if (this.replaceable) props.replaceable();

        props.isValidSpawn((blockState,blockGetter,blockPos,entityType) -> this.allowsSpawning);
        props.pushReaction(this.pushReaction);

        return props.dynamicShape();
    }

    public boolean isLightSource() {
        return this.lightEmission != Integer.MIN_VALUE;
    }
}
