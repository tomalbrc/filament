package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.mixin.accessor.FireBlockInvoker;
import eu.pb4.polymer.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.blocks.impl.DefaultModelData;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateModelVariant;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateMultiPartDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Fire implements BlockBehaviour<Fire.Config> {
    private static final List<FireModelEntry> FIRE_MODELS = ObjectArrayList.of(FireModelEntry.DEFAULT);
    private static final FireBlock FIRE_BLOCK = (FireBlock) Blocks.FIRE;
    private static final Map<Block, JsonArray> holderData = new Reference2ObjectArrayMap<>();

    public static Map<Block, Block> MATERIAL_BLOCKS;

    private final Config config;

    public Fire(Config config) {
        this.config = config;
    }

    public static Block getMaterialFire(BlockState blockState) {
        if (MATERIAL_BLOCKS == null) {
            var setCodec = RegistryCodecs.homogeneousList(Registries.BLOCK);

            var map = new Reference2ReferenceArrayMap<Block, Block>();
            for (Map.Entry<Block, JsonArray> entry : holderData.entrySet()) {
                var arr = entry.getValue();
                for (JsonElement element : arr) {
                    var holderSet = setCodec.decode(RegistryOps.create(JsonOps.INSTANCE, Filament.SERVER.registryAccess()), element);
                    holderSet.ifSuccess(x -> {
                        for (Holder<Block> holder : x.getFirst()) {
                            map.put(holder.value(), entry.getKey());
                        }
                    });
                }
            }

            MATERIAL_BLOCKS = map;
        }

        return MATERIAL_BLOCKS.get(blockState.getBlock());
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        JsonArray arr = new JsonArray();
        if (config.blocks != null) {
            for (ResourceLocation location : config.blocks) {
                arr.add(location.toString());
            }
        }
        if (config.blockTags != null) {
            for (ResourceLocation location : config.blockTags) {
                arr.add("#" + location.toString());
            }
        }

        holderData.put(block, arr);
    }

    @Override
    @NotNull
    public Fire.Config getConfig() {
        return this.config;
    }

    public static class Config {
        public boolean hurt = true;
        public boolean tick = true;
        public boolean lightPortal = true;
        public float damage = 1.f;
        public List<ResourceLocation> blocks;
        public List<ResourceLocation> blockTags;
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(FireBlock.NORTH, false).setValue(FireBlock.EAST, false).setValue(FireBlock.SOUTH, false).setValue(FireBlock.WEST, false).setValue(FireBlock.UP, false).setValue(FireBlock.AGE, 0);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        FIRE_BLOCK.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        if (config.hurt) {
            insideBlockEffectApplier.apply(InsideBlockEffectType.FIRE_IGNITE);
            insideBlockEffectApplier.runAfter(InsideBlockEffectType.FIRE_IGNITE, (e) -> e.hurt(e.level().damageSources().inFire(), config.damage));
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return blockState.getBlock().withPropertiesOf(((FireBlockInvoker)FIRE_BLOCK).invokeUpdateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource));
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockState.getBlock().withPropertiesOf(((FireBlockInvoker)FIRE_BLOCK).invokeGetStateForPlacement(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos()));
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return ((FireBlockInvoker)FIRE_BLOCK).invokeCanSurvive(blockState, levelReader, blockPos);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        ((FireBlockInvoker)FIRE_BLOCK).invokeTick(blockState, serverLevel, blockPos, randomSource);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        ((FireBlockInvoker)FIRE_BLOCK).invokeOnPlace(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        ((FireBlockInvoker)FIRE_BLOCK).invokeCreateBlockStateDefinition(builder);
    }

    @Override
    public BlockState modifyPolymerBlockState(BlockState originalBlockState, BlockState blockState) {
        return blockState.setValue(FireBlock.AGE, 0);
    }

    @Override
    public boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> data) {
        var m = data.blockResource().models();
        var l1 = m.entrySet().stream().map(entry -> entry.getKey().startsWith("up") ? entry.getValue().model() : null).filter(Objects::nonNull).toList();
        var l2 = m.entrySet().stream().map(entry -> entry.getKey().startsWith("side") ? entry.getValue().model() : null).filter(Objects::nonNull).toList();
        var l3 = m.entrySet().stream().map(entry -> entry.getKey().startsWith("floor") ? entry.getValue().model() : null).filter(Objects::nonNull).toList();
        int id = FIRE_MODELS.size();
        FIRE_MODELS.add(new FireModelEntry(data.id(), FIRE_MODELS.size(), l1, l2, l3));

        var customBlock = BuiltInRegistries.BLOCK.getValue(data.id());
        for (BlockState possibleState : customBlock.getStateDefinition().getPossibleStates()) {
            if (possibleState.getValue(FireBlock.AGE) == 0)
                map.put(possibleState, BlockData.BlockStateMeta.of(FIRE_BLOCK.withPropertiesOf(possibleState).setValue(FireBlock.AGE, id), null));
        }

        return true;
    }



    public static void addRemap() {
        for (BlockState possibleState : Blocks.FIRE.getStateDefinition().getPossibleStates()) {
            if (possibleState.getValue(FireBlock.AGE) > 0) {
                DefaultModelData.SPECIAL_REMAPS.put(possibleState, possibleState.setValue(FireBlock.AGE, 0));
                BlockExtBlockMapper.INSTANCE.stateMap.put(possibleState, possibleState.setValue(FireBlock.AGE, 0));
            }
        }
    }

    public static void init(ResourcePackBuilder resourcePackBuilder) {
        if (FIRE_MODELS.size() <= 1)
            return;

        var firepath = "assets/minecraft/blockstates/fire.json";
        var data = resourcePackBuilder.getStringDataOrSource(firepath);
        if (data == null) {
            Filament.LOGGER.error("Could not load fire block state definition!");
            return;
        }

        var dec = BlockStateAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(data));

        dec.ifSuccess(pair -> {
            BlockStateAsset blockStateAsset = pair.getFirst();
            if (blockStateAsset.multipart().isPresent()) {
                List<StateMultiPartDefinition> list = new ObjectArrayList<>();
                for (FireModelEntry model : FIRE_MODELS) {
                    list.addAll(fireSelection(blockStateAsset, model));
                }

                var newAsset = new BlockStateAsset(Optional.empty(), Optional.of(list));
                resourcePackBuilder.addStringData(firepath, BlockStateAsset.CODEC.encodeStart(JsonOps.INSTANCE, newAsset).getOrThrow().toString());
            }
        });
    }

    private static List<StateMultiPartDefinition> fireSelection(BlockStateAsset fireAsset, FireModelEntry modelEntry) {
        List<StateMultiPartDefinition> list = new ObjectArrayList<>();
        if (fireAsset.multipart().isPresent()) {
            for (StateMultiPartDefinition partDefinition : fireAsset.multipart().get()) {
                var currVar = partDefinition.apply().getFirst();
                List<ResourceLocation> models;
                if (currVar.model().getPath().contains("block/fire_floor")) {
                    models = modelEntry.floor;
                } else if (currVar.model().getPath().contains("block/fire_side")) {
                    models = modelEntry.side;
                } else {
                    models = modelEntry.up;
                }

                List<StateModelVariant> newVariants = new ObjectArrayList<>();
                for (ResourceLocation model : models) {
                    newVariants.add(new StateModelVariant(model, currVar.x(), currVar.y(), currVar.uvlock(), currVar.weigth()));
                }

                var when = partDefinition.when();

                Map<String, String> base = null;
                if (when.base().isPresent()) {
                    base = new Object2ObjectArrayMap<>();
                    base.put("age", String.valueOf(modelEntry.age));
                    base.putAll(when.base().get());
                }

                List<Map<String, String>> or = null;
                if (when.or().isPresent()) {
                    or = new ObjectArrayList<>();
                    for (Map<String, String> stringMap : when.or().get()) {
                        var orMap = new Object2ObjectArrayMap<>(stringMap);
                        orMap.put("age", String.valueOf(modelEntry.age));
                        or.add(orMap);
                    }
                }

                var newWhen = new StateMultiPartDefinition.When(Optional.ofNullable(or), Optional.empty(), Optional.ofNullable(base));
                list.add(new StateMultiPartDefinition(newWhen, newVariants));
            }
        }
        return list;
    }

    record FireModelEntry(@Nullable ResourceLocation id, int age, List<ResourceLocation> up, List<ResourceLocation> side, List<ResourceLocation> floor) {
        public static FireModelEntry DEFAULT = new FireModelEntry(
                null,
                0,
                List.of(ResourceLocation.withDefaultNamespace("block/fire_up0"), ResourceLocation.withDefaultNamespace("block/fire_up1"), ResourceLocation.withDefaultNamespace("block/fire_up_alt0"), ResourceLocation.withDefaultNamespace("block/fire_up_alt1")),
                List.of(ResourceLocation.withDefaultNamespace("block/fire_side0"), ResourceLocation.withDefaultNamespace("block/fire_side1"), ResourceLocation.withDefaultNamespace("block/fire_side_alt0"), ResourceLocation.withDefaultNamespace("block/fire_side_alt1")),
                List.of(ResourceLocation.withDefaultNamespace("block/fire_floor0"), ResourceLocation.withDefaultNamespace("block/fire_floor1"))
        );
    }
}