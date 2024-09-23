package de.tomalbrc.filament.data;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public record BlockData(
        @NotNull ResourceLocation id,
        @Nullable Item vanillaItem,
        @NotNull BlockResource blockResource,
        @Nullable ItemResource itemResource,
        @Nullable BlockModelType blockModelType,
        @Nullable BlockProperties properties,
        @SerializedName("behaviour")
        @Nullable BehaviourConfigMap behaviourConfig,
        @Nullable DataComponentMap components,
        @SerializedName("group")
        @Nullable ResourceLocation itemGroup
        ) {
    @Override
    @NotNull
    public BlockProperties properties() {
        if (properties == null) {
            return BlockProperties.EMPTY;
        }
        return properties;
    }

    @Override
    @NotNull
    public DataComponentMap components() {
        if (components == null) {
            return DataComponentMap.EMPTY;
        }
        return components;
    }

    @Override
    @NotNull
    public Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    public Map<BlockState, BlockStateMeta> createStandardStateMap() {
        Reference2ReferenceArrayMap<BlockState, BlockStateMeta> val = new Reference2ReferenceArrayMap<>();

        if (blockResource.couldGenerate()) {
            //val = BlockModelGenerator.generate(blockResource);
            throw new UnsupportedOperationException("Not implemented");
        } else if (blockResource.models() != null && this.blockModelType != null) {
            for (Map.Entry<String, ResourceLocation> entry : this.blockResource.models().entrySet()) {
                PolymerBlockModel blockModel = PolymerBlockModel.of(entry.getValue());

                BlockState requestedState = PolymerBlockResourceUtils.requestBlock(this.blockModelType, blockModel);

                if (entry.getKey().equals("default")) {
                    val.put(BuiltInRegistries.BLOCK.get(id).defaultBlockState(), BlockStateMeta.of(requestedState, blockModel));
                }
                else {
                    BlockStateParser.BlockResult parsed;
                    String str = String.format("%s[%s]", id, entry.getKey());
                    try {
                        parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), str, false);
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        throw new JsonParseException("Invalid BlockState value: " + str);
                    }

                    val.put(parsed.blockState(), BlockStateMeta.of(requestedState, blockModel));
                }

                if (requestedState == null) {
                    throw new RuntimeException("Ran out of block states to use for " + this.blockModelType.name() + "!");
                }
            }
        }

        return val;
    }

    public record BlockStateMeta(BlockState blockState, PolymerBlockModel polymerBlockModel) {
        public static BlockStateMeta of(BlockState blockState, PolymerBlockModel blockModel) {
            return new BlockStateMeta(blockState, blockModel);
        }
    }
}
