package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.registry.DecorationRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        return (DecorationBlockEntity)this.level.getBlockEntity(new BlockPos(this.worldPosition).subtract(this.main));
    }

    public BlockPos mainPosition() {
        return new BlockPos(this.worldPosition).subtract(this.main);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.version = input.getInt(VERSION).orElse(2);
        input.read(MAIN, BlockPos.CODEC).ifPresent(main -> this.main = main);

        if (!this.isMain())
            return;

        this.direction = Direction.from3DDataValue(input.getIntOr(DIRECTION, Direction.UP.get3DDataValue()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (this.main == null) this.main = BlockPos.ZERO;

        output.store(MAIN, BlockPos.CODEC, this.main);
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