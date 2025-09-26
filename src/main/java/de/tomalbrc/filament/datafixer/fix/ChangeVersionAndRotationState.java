package de.tomalbrc.filament.datafixer.fix;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.datafixer.DataFix;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;

import java.util.*;

public class ChangeVersionAndRotationState extends com.mojang.datafixers.DataFix {
    public ChangeVersionAndRotationState(Schema outputSchema) {
        super(outputSchema, true);
    }

    public static com.mojang.datafixers.DataFix create(Schema outputSchema) {
        return new ChangeVersionAndRotationState(outputSchema);
    }


    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = this.getOutputSchema().getType(References.CHUNK);

        return writeFixAndRead("UpdateFilamentDecorations", type, type2, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        try {
            List<Dynamic<?>> blockEntities = dynamic.get("block_entities").asList(dyn -> dyn.castTyped(NbtOps.INSTANCE));
            if (blockEntities == null || blockEntities.isEmpty())
                return dynamic;

            List<Dynamic<?>> sections = dynamic.get("sections").asList(dyn -> dyn.castTyped(NbtOps.INSTANCE));

            var blendingData = dynamic.get("blending_data");
            var min = blendingData.get("min_section").asInt(-4);

            Map<Integer, List<Dynamic<?>>> map = new HashMap<>();
            for (Dynamic<?> blockEntity : blockEntities) {
                var id = blockEntity.get("id").read(ResourceLocation.CODEC).getOrThrow();
                var v = blockEntity.get("V");
                if (v == null || v.get().isError())
                    continue;

                var version = v.asInt(0);
                if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) && version < DataFix.VERSION) {
                    var yData = blockEntity.get("y").asInt(0);
                    var idx = (yData - min * 16) / 16;
                    map.computeIfAbsent(idx, (x) -> new ArrayList<>()).add(blockEntity);
                }
            }

            if (!map.isEmpty()) {
                for (Integer idx : map.keySet()) {
                    var entries = map.get(idx);
                    var section = sections.get(idx);

                    var states = section.get("block_states");

                    PalettedContainer<BlockState> palettedContainer = states.read(CompoundTag.CODEC).mapOrElse(tag -> BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, tag).promotePartial(string -> {}).getOrThrow(SerializableChunkData.ChunkReadException::new), (tagError) -> new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)));

                    for (Dynamic<?> blockEntityData : entries) {
                        var yData = blockEntityData.get("y").asInt(0);
                        var x = blockEntityData.get("x").asInt(0) & 15;
                        var y = yData & 15;
                        var z = blockEntityData.get("z").asInt(0) & 15;

                        var dir = Direction.from3DDataValue(blockEntityData.get(DecorationBlockEntity.DIRECTION).asInt(Direction.UP.get3DDataValue()));
                        var rot = blockEntityData.get("Rotation");

                        var state = palettedContainer.get(x,y,z);
                        if (state.getBlock() instanceof DecorationBlock && state.hasProperty(BlockUtil.ROTATION)) {
                            state = state.setValue(BlockUtil.ROTATION, Util.SEGMENTED_ANGLE8.fromDegrees(DataFix.getVisualRotationYInDegrees(dir, rot.asInt(0))));
                            palettedContainer.getAndSet(x, y, z, state);
                        }

                        var upgradedBlockEntityData = blockEntityData.set("V", dynamic.createInt(2)).remove("Rotation");
                        blockEntities.set(blockEntities.indexOf(blockEntityData), upgradedBlockEntityData);
                    }

                    var result = BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, palettedContainer);
                    Tag tag = result.getOrThrow();
                    section = section.replaceField("block_states", "block_states", Optional.of(new Dynamic<>(NbtOps.INSTANCE, tag)));
                    sections.set(idx, section);
                }

                dynamic = dynamic.set("block_entities", dynamic.createList(blockEntities.stream())).set("sections", dynamic.createList(sections.stream()));
            }
        } catch (Exception e) {
            Filament.LOGGER.info("Error during data fix");
        }

        return dynamic;
    }

    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
            BlockState.CODEC, Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY), Blocks.AIR.defaultBlockState()
    );
}