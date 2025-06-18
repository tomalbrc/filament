package de.tomalbrc.filament.decoration.block;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.VirtualDestroyStage;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class DecorationBlock extends SimpleBlock implements PolymerBlock, BlockWithElementHolder, SimpleWaterloggedBlock, VirtualDestroyStage.Marker {
    final protected ResourceLocation decorationId;
    DecorationData data;

    public DecorationBlock(Properties properties, DecorationData data) {
        super(properties, data);
        this.decorationId = data.id();
        this.data = data;

        this.stateDefinitionEx.getPossibleStates().forEach(BlockState::initCache);
        this.stateDefinition.getPossibleStates().forEach(BlockState::initCache);
    }

    @Override
    protected void initCaches() {

    }

    public void postRegister() {
        this.stateDefinitionEx.getPossibleStates().forEach(BlockState::initCache);
    }

    public DecorationData getDecorationData() {
        return this.data;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
        if (getDecorationData() != null) {
            DecorationData decorationData = getDecorationData();
            boolean passthrough = !decorationData.hasBlocks();

            var newState = state;
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : this.getBehaviours()) {
                if (behaviour.getValue() instanceof BlockBehaviour<?> blockBehaviour) {
                    newState = blockBehaviour.modifyPolymerBlockState(state, newState);
                }
            }

            BlockState blockState = passthrough ? Blocks.AIR.defaultBlockState() : decorationData.block();
            boolean waterlogged = newState.hasProperty(BlockStateProperties.WATERLOGGED) && newState.getValue(BlockStateProperties.WATERLOGGED);
            if (passthrough && waterlogged) {
                return Blocks.WATER.defaultBlockState();
            } else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
            }

            return blockState;
        }

        return super.getPolymerBlockState(state, packetContext);
    }

    @Override
    public void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (!blockState.isAir()) {
            this.removeDecoration(level, blockPos, null);
        }
    }

    @Override
    @NotNull
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockState returnVal = super.playerWillDestroy(level, blockPos, blockState, player);
        this.removeDecoration(level, blockPos, player);
        return returnVal;
    }

    protected void removeDecoration(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            decorationBlockEntity.destroyStructure(player == null || !player.isCreative());
        } else {
            level.destroyBlock(blockPos, false);
        }
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!getDecorationData().hasBlocks()) {
            return Shapes.empty();
        } else {
            return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
        }
    }

    public float getVisualRotationYInDegrees(BlockState blockState) {
        float rotation = 0;
        if (blockState.getBlock() instanceof DecorationBlock decorationBlock) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : decorationBlock.getBehaviours()) {
                if (behaviour.getValue() instanceof DecorationRotationProvider rotationProvider) {
                    return rotationProvider.getVisualRotationYInDegrees(blockState);
                }
            }
        }

        return 0;
    }

    abstract public ItemStack visualItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState);
}
