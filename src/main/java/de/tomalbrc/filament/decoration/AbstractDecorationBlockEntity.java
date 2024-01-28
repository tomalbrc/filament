package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDecorationBlockEntity extends BlockEntity {

    protected BlockPos main;

    protected int rotation;

    protected Direction direction = Direction.UP;

    protected ItemStack itemStack;

    private boolean passthrough = false;

    public AbstractDecorationBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.DECORATION_BLOCK_ENTITY, pos, state);
    }

    public boolean isMain() {
        return this.main != null && this.main.equals(this.worldPosition);
    }

    public void setMain(BlockPos main) {
        this.main = main;
    }

    public DecorationBlockEntity getMainBlockEntity() {
        assert this.level != null;
        return (DecorationBlockEntity)this.level.getBlockEntity(this.main);
    }

    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        this.main = NbtUtils.readBlockPos(compoundTag.getCompound("Main"));
        this.itemStack = ItemStack.of(compoundTag.getCompound("Item"));

        if (compoundTag.contains("Passthrough")) {
            this.passthrough = compoundTag.getBoolean("Passthrough");
            this.setCollision(!this.passthrough);
        }

        if (!this.isMain())
            return;

        this.rotation = compoundTag.getInt("Rotation");
        this.direction = Direction.from3DDataValue(compoundTag.getInt("Direction"));
    }

    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        compoundTag.put("Item", this.itemStack.save(new CompoundTag()));

        compoundTag.put("Main", NbtUtils.writeBlockPos(this.main));
        if (this.passthrough) compoundTag.putBoolean("Passthrough", this.passthrough);

        if (this.isMain()) {
            compoundTag.putInt("Rotation", this.rotation);
            compoundTag.putInt("Direction", this.direction.get3DDataValue());
        }
    }

    abstract protected void destroyBlocks();
    abstract protected void destroyStructure(boolean dropItems);
    abstract protected void setCollisionStructure(boolean collisionStructure);

    protected void setCollision(boolean collision) {
        if (this.isMain()) {
            this.setCollisionStructure(collision);
        } else {
            this.getMainBlockEntity().setCollisionStructure(collision);
        }
    }

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
        Direction direction = this.getDirection();
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return (float) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + rotation * 45 + i);
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return this.rotation;
    }
}