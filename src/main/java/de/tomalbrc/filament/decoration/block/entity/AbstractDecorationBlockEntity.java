package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.registry.DecorationRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDecorationBlockEntity extends BlockEntity {
    public static final String MAIN = "Main";
    public static final String VERSION = "V";
    public static final String ITEM = "Item";
    public static final String DIRECTION = "Direction";

    protected BlockPos main;
    protected int version = DataFix.VERSION;

    protected Direction direction = Direction.UP;

    public int rotation;

    public AbstractDecorationBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationRegistry.getBlockEntityType(state), pos, state);
    }

    public boolean isMain() {
        return this.main != null && this.main.equals(BlockPos.ZERO);
    }

    public void setMain(BlockPos main) {
        this.main = main;
    }

    public DecorationBlockEntity getMainBlockEntity() {
        assert this.level != null;
        if (this.level.getBlockEntity(this.mainPosition()) instanceof DecorationBlockEntity decorationBlockEntity) {
            return decorationBlockEntity;
        }
        return (DecorationBlockEntity) this;
    }

    public BlockPos mainPosition() {
        return this.worldPosition.subtract(this.main);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider lookup) {
        super.loadAdditional(input, lookup);

        this.version = input.contains(VERSION) ? input.getInt(VERSION) : 2;
        if (input.contains(MAIN)) BlockPos.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, lookup), input.get(MAIN)).ifSuccess(x -> {
            this.main = x.getFirst();
        });

        if (!this.isMain())
            return;

        this.direction = input.contains(DIRECTION) ? Direction.from3DDataValue(input.getInt(DIRECTION)) : Direction.UP;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider lookup) {
        super.saveAdditional(output, lookup);

        if (this.main == null) this.main = BlockPos.ZERO;

        output.put(MAIN, BlockPos.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, lookup), this.main).getOrThrow());
        output.putInt(VERSION, this.version);

        if (this.isMain()) {
            output.putInt(DIRECTION, this.direction.get3DDataValue());
        }
    }

    abstract protected void destroyBlocks(ItemStack particleItem);
    abstract protected void destroyStructure(boolean dropItems);

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public ItemStack getItem() {
        var item = getBlockState().getBlock().asItem().getDefaultInstance();
        item.applyComponents(components());
        return item;
    }

    public float getVisualRotationYInDegrees() {
        return ((DecorationBlock)this.getBlockState().getBlock()).getVisualRotationYInDegrees(this.getBlockState());
    }
}