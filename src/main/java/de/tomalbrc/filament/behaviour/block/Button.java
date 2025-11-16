package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

import static de.tomalbrc.filament.behaviour.block.Lever.getConnectedDirection;

public class Button implements BlockBehaviour<Button.Config> {
    private final Config config;

    public Button(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Button.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(ButtonBlock.POWERED)) {
            return InteractionResult.CONSUME;
        } else {
            this.press(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks() && !(Boolean)state.getValue(ButtonBlock.POWERED)) {
            this.press(state, level, pos, null);
        }
    }

    public void press(BlockState state, Level level, BlockPos pos, @Nullable Player player) {
        level.setBlock(pos, state.setValue(ButtonBlock.POWERED, true), Block.UPDATE_ALL);
        this.updateNeighbours(state, level, pos);
        level.scheduleTick(pos, state.getBlock(), this.config.ticksToStayPressed.getValue(state));
        this.playSound(level, pos, true);
        level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
    }

    protected void playSound(LevelAccessor level, BlockPos pos, boolean on) {
        level.playSound(null, pos, this.getSound(on), SoundSource.BLOCKS);
    }

    protected SoundEvent getSound(boolean on) {
        return on ? SoundEvent.createVariableRangeEvent(this.config.clickOnSound) : SoundEvent.createVariableRangeEvent(this.config.clickOffSound);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, Level level, BlockPos pos, BlockState blockState2, boolean movedByPiston) {
        if (!movedByPiston && state.getValue(ButtonBlock.POWERED)) {
            this.updateNeighbours(state, level, pos);
        }
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ButtonBlock.POWERED) ? this.config.powerlevel.getValue(state) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ButtonBlock.POWERED) && getConnectedDirection(state) == direction ? this.config.powerlevel.getValue(state) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ButtonBlock.POWERED)) {
            this.pressCheck(state, level, pos);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && this.config.canBeActivatedByArrows.getValue(state) && !(Boolean)state.getValue(ButtonBlock.POWERED)) {
            this.pressCheck(state, level, pos);
        }
    }

    protected void pressCheck(BlockState state, Level level, BlockPos pos) {
        AbstractArrow abstractArrow = this.config.canBeActivatedByArrows.getValue(state) ? level.getEntitiesOfClass(AbstractArrow.class, state.getShape(level, pos).bounds().move(pos)).stream().findFirst().orElse(null) : null;
        boolean hasArrowInside = abstractArrow != null;
        boolean isPowered = state.getValue(ButtonBlock.POWERED);
        if (hasArrowInside != isPowered) {
            level.setBlock(pos, state.setValue(ButtonBlock.POWERED, hasArrowInside), Block.UPDATE_ALL);
            this.updateNeighbours(state, level, pos);
            this.playSound(level, pos, hasArrowInside);
            level.gameEvent(abstractArrow, hasArrowInside ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        }

        if (hasArrowInside) {
            level.scheduleTick(pos, state.getBlock(), this.config.ticksToStayPressed.getValue(state));
        }
    }

    private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, blockState.getBlock());
        level.updateNeighborsAt(blockPos.relative(getConnectedDirection(blockState).getOpposite()), blockState.getBlock());
    }

    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ButtonBlock.FACING, ButtonBlock.POWERED, ButtonBlock.FACE);
    }

    public static class Config {
        public BlockStateMappedProperty<Integer> powerlevel = BlockStateMappedProperty.of(15);
        public BlockStateMappedProperty<Integer> ticksToStayPressed = BlockStateMappedProperty.of(100);
        public BlockStateMappedProperty<Boolean> canBeActivatedByArrows = BlockStateMappedProperty.of(true);
        public ResourceLocation clickOnSound = SoundEvents.WOODEN_BUTTON_CLICK_ON.getLocation();
        public ResourceLocation clickOffSound = SoundEvents.WOODEN_BUTTON_CLICK_OFF.getLocation();
    }
}