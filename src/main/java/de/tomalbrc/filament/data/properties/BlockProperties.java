package de.tomalbrc.filament.data.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    public static final BlockProperties EMPTY = new BlockProperties();

    @NotNull
    public Block blockBase = Blocks.STONE;
    public boolean requiresTool = true;
    public float explosionResistance = Float.MIN_VALUE;
    public float destroyTime = Float.MIN_VALUE;
    public boolean redstoneConductor = true;

    public int lightEmission = Integer.MIN_VALUE;

    public boolean transparent = false;
    public boolean allowsSpawning = false;
    public boolean replaceable = false;
    public boolean collision = true;

    public boolean solid = true;

    public PushReaction pushReaction = PushReaction.NORMAL;

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        props.sound(this.blockBase.defaultBlockState().getSoundType());

        if (this.destroyTime != Float.MIN_VALUE) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != Float.MIN_VALUE) props.explosionResistance(this.explosionResistance);
        else if (this.destroyTime != Float.MIN_VALUE) props.explosionResistance(this.destroyTime);
        if (this.isLightSource()) props.lightLevel((state) -> this.lightEmission);
        props.isRedstoneConductor((a,b,c) -> this.redstoneConductor);
        if (this.requiresTool) props.requiresCorrectToolForDrops();
        if (this.replaceable) props.replaceable();
        if (this.transparent) props.noOcclusion();
        if (!this.collision) props.noCollission();

        if (this.solid) props.forceSolidOn();
        else props.forceSolidOff();

        props.isValidSpawn((blockState,blockGetter,blockPos,entityType) -> this.allowsSpawning);
        props.pushReaction(this.pushReaction);

        return props.dynamicShape();
    }

    public boolean isLightSource() {
        return this.lightEmission != Integer.MIN_VALUE;
    }
}
