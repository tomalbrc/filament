package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
                    var holderSet = setCodec.decode(RegistryOps.create(JsonOps.INSTANCE, Filament.REGISTRY_ACCESS.compositeAccess()), element);
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
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (config.hurt) {
            if (!entity.fireImmune()) {
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
                if (entity.getRemainingFireTicks() == 0) {
                    entity.igniteForSeconds(8.0F);
                }
            }

            entity.hurt(level.damageSources().inFire(), this.config.damage);
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState.getBlock().withPropertiesOf(((FireBlockInvoker)FIRE_BLOCK).invokeUpdateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2));
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

        var customBlock = BuiltInRegistries.BLOCK.get(data.id());
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
        var datax = resourcePackBuilder.getDataOrSource(firepath);
        if (datax == null) {
            Filament.LOGGER.error("Could not load fire block state definition!");
            return;
        }
        var data = new String(datax);

        var dec = BlockStateAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(data));

        dec.ifSuccess(pair -> {
            BlockStateAsset blockStateAsset = pair.getFirst();
            if (blockStateAsset.multipart().isPresent()) {
                List<StateMultiPartDefinition> list = new ObjectArrayList<>();
                for (FireModelEntry model : FIRE_MODELS) {
                    list.addAll(fireSelection(blockStateAsset, model));
                }

                var newAsset = new BlockStateAsset(Optional.empty(), Optional.of(list));
                resourcePackBuilder.addData(firepath, BlockStateAsset.CODEC.encodeStart(JsonOps.INSTANCE, newAsset).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));
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

    public record BlockStateAsset(Optional<Map<String, List<StateModelVariant>>> variants, Optional<List<StateMultiPartDefinition>> multipart) {
        public static final Codec<BlockStateAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StateModelVariant.MAP.optionalFieldOf("variants").forGetter(BlockStateAsset::variants),
                StateMultiPartDefinition.CODEC.listOf().optionalFieldOf("multipart").forGetter(BlockStateAsset::multipart)
        ).apply(instance, BlockStateAsset::new));
    }
    public record StateModelVariant(ResourceLocation model, int x, int y, boolean uvlock, int weigth) {
        private static final Codec<StateModelVariant> BASE = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.fieldOf("model").forGetter(StateModelVariant::model),
                        Codec.INT.optionalFieldOf("x", 0).forGetter(StateModelVariant::x),
                        Codec.INT.optionalFieldOf("y", 0).forGetter(StateModelVariant::y),
                        Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(StateModelVariant::uvlock),
                        Codec.INT.optionalFieldOf("weigth", 1).forGetter(StateModelVariant::weigth)
                ).apply(instance, StateModelVariant::new)
        );

        public static final Codec<List<StateModelVariant>> CODEC = Codec.withAlternative(BASE.listOf(), BASE, List::of);
        public static final Codec<Map<String, List<StateModelVariant>>> MAP = SortedMapCodec.of(Codec.STRING, CODEC);
    }

    public record SortedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec, Comparator<Map.Entry<K, V>> comparator) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {
        public static <K extends Comparable<K>, V> SortedMapCodec<K, V> of(Codec<K> keyCodec, Codec<V> elementCodec) {
            return new SortedMapCodec<>(keyCodec, elementCodec, Map.Entry.comparingByKey());
        }

        public static <K, V> SortedMapCodec<K, V> of(Codec<K> keyCodec, Codec<V> elementCodec, Comparator<K> comparator) {
            return new SortedMapCodec<>(keyCodec, elementCodec, Map.Entry.comparingByKey(comparator));
        }

        @Override
        public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
            return encode(input, ops, ops.mapBuilder()).build(prefix);
        }

        @Override
        public <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            var entries = new ArrayList<>(input.entrySet());
            entries.sort(this.comparator);

            for (final var entry : entries) {
                prefix.add(keyCodec.encodeStart(ops, entry.getKey()), elementCodec.encodeStart(ops, entry.getValue()));
            }
            return prefix;
        }

        @Override
        public String toString() {
            return "SortedMapCodec[" + keyCodec + " -> " + elementCodec + ']';
        }
    }

    public record StateMultiPartDefinition(When when, List<StateModelVariant> apply) {
        public static final Codec<StateMultiPartDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        When.CODEC.optionalFieldOf("when", When.DEFAULT).forGetter(StateMultiPartDefinition::when),
                        StateModelVariant.CODEC.fieldOf("apply").forGetter(StateMultiPartDefinition::apply)
                ).apply(instance, StateMultiPartDefinition::new)
        );

        public record When(Optional<List<Map<String, String>>> or, Optional<List<Map<String, String>>> and,
                           Optional<Map<String, String>> base) {
            public static final When DEFAULT = new When(Optional.empty(), Optional.empty(), Optional.empty());

            private static final Codec<Map<String, String>> STR_MAP = SortedMapCodec.of(Codec.STRING, Codec.withAlternative(Codec.STRING, ExtraCodecs.JAVA, String::valueOf));
            private static final Codec<List<Map<String, String>>> LIST_STR_MAP = STR_MAP.listOf();
            public static final Codec<When> CODEC = Codec.either(
                    LIST_STR_MAP.fieldOf("OR")
                            .xmap(x -> new When(Optional.of(x), Optional.empty(), Optional.empty()), x -> x.or.orElseThrow()).codec(),
                    Codec.either(
                            LIST_STR_MAP.fieldOf("AND")
                                    .xmap(x -> new When(Optional.empty(), Optional.of(x), Optional.empty()), x -> x.and.orElseThrow()).codec(),
                            STR_MAP.xmap(x -> new When(Optional.empty(), Optional.empty(), Optional.of(x)), x -> x.base.orElseThrow()))
            ).xmap(x -> x.left().orElseGet(() -> x.right().orElseThrow().left().orElseGet(x.right().get().right()::get)),

                    x -> x.or.isPresent() ? Either.left(x)
                            : x.and.isPresent() ? Either.right(Either.left(x)) : Either.right(Either.right(x))
            );
        }
    }
}