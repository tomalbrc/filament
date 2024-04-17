package de.tomalbrc.filament.data.properties;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BlockProperties extends ItemProperties {
    @NotNull
    public Block blockBase = Blocks.STONE;
    @NotNull
    public Item itemBase = Items.PAPER;

    private boolean requiresTool = false;
    private float explosionResistance = Integer.MIN_VALUE;
    private float destroyTime = Integer.MIN_VALUE;
    private boolean redstoneConductor = true;

    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(this.blockBase);

        if (this.destroyTime != Integer.MIN_VALUE) props.destroyTime(this.destroyTime);
        if (this.explosionResistance != Integer.MIN_VALUE) props.explosionResistance(this.explosionResistance);
        if (this.isLightSource()) props.lightLevel((state) -> this.lightEmission);
        if (this.isLightSource()) props.lightLevel((state) -> this.lightEmission);
        props.isRedstoneConductor((a,b,c) -> this.redstoneConductor);
        if (this.requiresTool) props.requiresCorrectToolForDrops();

        return props;
    }
}
