package de.tomalbrc.filament.decoration.block.entity;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.datafixer.DataFix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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

    protected ItemStack itemStack;

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

        this.itemStack = input.read(ITEM, ItemStack.CODEC).orElse(null);
        if (this.itemStack == null || this.itemStack.isEmpty()) {
            this.itemStack = BuiltInRegistries.ITEM.getValue(((DecorationBlock)this.getBlockState().getBlock()).getDecorationData().id()).getDefaultInstance();
        }

        if (!this.isMain())
            return;

        this.direction = Direction.from3DDataValue(input.getIntOr(DIRECTION, Direction.UP.get3DDataValue()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (this.itemStack == null) {
            var optionalKey = this.getBlockState().getBlockHolder().unwrapKey();
            var optional = BuiltInRegistries.ITEM.get(optionalKey.orElseThrow().location());
            optional.ifPresent(item -> this.itemStack = item.value().getDefaultInstance());
        }

        if (this.itemStack == null && this.level != null) {
            Filament.LOGGER.error("No item for decoration! Removing decoration block entity at {}", this.getBlockPos().toShortString());
            this.level.destroyBlock(this.getBlockPos(), false);
            this.setRemoved();
            return;
        }

        if (this.itemStack != null)
            output.store(ITEM, ItemStack.CODEC, this.itemStack);

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
        return this.itemStack;
    }

    public void setItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public float getVisualRotationYInDegrees() {
        return ((DecorationBlock)this.getBlockState().getBlock()).getVisualRotationYInDegrees(this.getBlockState());
    }
}