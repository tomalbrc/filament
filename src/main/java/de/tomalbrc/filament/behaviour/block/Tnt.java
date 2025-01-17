package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tnt implements BlockBehaviour<Tnt.Config> {
    private final Config config;

    public Tnt(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Tnt.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        DispenserBlock.registerBehavior(block, new DefaultDispenseItemBehavior(){
            @Override
            protected @NotNull ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                ServerLevel serverLevel = blockSource.level();
                BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
                explode(serverLevel, blockPos, null, block.defaultBlockState());
                serverLevel.gameEvent(null, GameEvent.ENTITY_PLACE, blockPos);

                itemStack.shrink(1);
                return itemStack;
            }
        });
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState2.is(blockState.getBlock())) {
            if (level.hasNeighborSignal(blockPos)) {
                explode(level, blockPos, null, blockState);
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.hasNeighborSignal(blockPos)) {
            explode(level, blockPos, null, blockState);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide() && !player.isCreative() && config.unstable.getValue(blockState)) {
            explode(level, blockPos, null, blockState);
        }
    }

    @Override
    public void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
        var bs = serverLevel.getBlockState(blockPos);
        var i = config.fuseTime.getValue(bs);
        explode(serverLevel, blockPos, null, serverLevel.getBlockState(blockPos), serverLevel.random.nextInt(i / 4) + i / 8);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
            return null;
        } else {
            explode(level, blockPos, player, blockState);
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            Item item = itemStack.getItem();
            if (itemStack.is(Items.FLINT_AND_STEEL)) {
                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
            } else {
                itemStack.consume(1, player);
            }

            player.awardStat(Stats.ITEM_USED.get(item));
            return ItemInteractionResult.SUCCESS;
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos)) {
                explode(level, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null, blockState);
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    public void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity, BlockState bs, int fuse) {
        if (!level.isClientSide) {
            bs = bs == null ? level.getBlockState(blockPos) : bs;

            PrimedTnt tntEntity = new PrimedTnt(level, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, livingEntity);
            tntEntity.setFuse(fuse);
            tntEntity.setBlockState(bs);
            level.addFreshEntity(tntEntity);

            level.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvent.createVariableRangeEvent(config.primeSound.getValue(bs)), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(livingEntity, GameEvent.PRIME_FUSE, blockPos);
        }
    }

    public void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity, BlockState bs) {
        explode(level, blockPos, livingEntity, bs, config.fuseTime.getValue(bs == null ? level.getBlockState(blockPos) : bs));
    }

    public static class Config {
        public BlockStateMappedProperty<Boolean> unstable = BlockStateMappedProperty.of(false);
        public BlockStateMappedProperty<Float> explosionPower = BlockStateMappedProperty.of(4f);
        public BlockStateMappedProperty<Integer> fuseTime = BlockStateMappedProperty.of(80);
        public BlockStateMappedProperty<ResourceLocation> primeSound = BlockStateMappedProperty.of(SoundEvents.TNT_PRIMED.getLocation());
    }
}