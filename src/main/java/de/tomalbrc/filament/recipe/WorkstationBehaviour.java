package de.tomalbrc.filament.recipe;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.EntityRegistry;
import de.tomalbrc.filament.util.TextUtil;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class WorkstationBehaviour implements BlockBehaviourWithEntity<WorkstationBehaviour.Config> {
    final Config config;
    private final Long2ObjectArrayMap<List<SimpleGui>> map = new Long2ObjectArrayMap<>();

    public static Map<Block, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new Object2ReferenceOpenHashMap<>();
    BlockEntityType<StationBlockEntity> blockEntityType;

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        if (!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(block.asFilamentBlock().data().id())) {
            var blockEntityType = FabricBlockEntityTypeBuilder.create((pos, state) -> new StationBlockEntity(pos, state, config.id), block).build();
            EntityRegistry.registerBlockEntity(EntityRegistry.key(block.asFilamentBlock().data().id()), blockEntityType);
            BLOCK_ENTITY_TYPES.put(block, blockEntityType);
        }
    }

    public WorkstationBehaviour(Config config) {
        this.config = config;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new StationBlockEntity(blockPos, blockState, config.id);
    }

    @Override
    public BlockEntityType<?> blockEntityType() {
        return blockEntityType;
    }

    @Override
    @Nullable
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level level, BlockState blockState1, BlockEntityType<A> blockEntityType) {
        BlockEntityTicker<A> ticker;
        if (level instanceof ServerLevel) {
            ticker = BlockBehaviourWithEntity.createTickerHelper(blockEntityType, blockEntityType, (_, _, _, abstractFurnaceBlockEntity) -> ((StationBlockEntity) abstractFurnaceBlockEntity).tick());
        } else {
            ticker = null;
        }
        return ticker;
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        var key = blockPos.asLong();
        if (map.containsKey(key)) {
            for (var gui : map.get(key)) {
                gui.close();
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof StationBlockEntity be) {
            StationGui gui = new StationGui((ServerPlayer) player, Workstations.get(config.id), be);

            gui.setTitle(TextUtil.formatText(config.titlePrefix).copy().append(be.getName()));

            final var key = blockPos.asLong();
            gui.open(() -> {
                map.get(key).remove(gui);
 
                if (map.get(key).isEmpty())
                    map.remove(key);
            });

            map.computeIfAbsent(key, _ -> new ObjectArrayList<>()).add(gui);

            return InteractionResult.SUCCESS;

        }

        return InteractionResult.FAIL;

    }

    @Override
    public @NonNull Config getConfig() {
        return config;
    }

    public static class Config {
        Identifier id;
        String titlePrefix;
    }
}
