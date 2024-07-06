package de.tomalbrc.filament.mixin.polymer;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.impl.DefaultModelData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultModelData.class)
public class DefaultModelDataMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void filament$additionalBlockModelTypes(CallbackInfo ci) {
        addTripwire(false, BlockModelType.valueOf("TRIPWIRE_BLOCK"));
        addTripwire(true, BlockModelType.valueOf("FLAT_TRIPWIRE_BLOCK"));
        addPetriSlab(BlockModelType.valueOf("SLAB_BLOCK"));
    }

    @Unique
    private static void addPetriSlab(BlockModelType modelType) {
        ObjectArrayList<BlockState> list = new ObjectArrayList<>();

        // Generate all permutations
        var b = new SlabType[]{SlabType.TOP, SlabType.BOTTOM, SlabType.DOUBLE};
        for (var s : b) {
            BlockState state = Blocks.PETRIFIED_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, s);
            list.add(state);
            DefaultModelData.SPECIAL_REMAPS.put(state, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, s));

            {
                BlockState state2 = Blocks.PETRIFIED_OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true).setValue(SlabBlock.TYPE, s);
                list.add(state2);
                DefaultModelData.SPECIAL_REMAPS.put(state2, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true).setValue(SlabBlock.TYPE, s));
            }
        }

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }

    @Unique
    private static void addTripwire(boolean attached, BlockModelType modelType) {
        var tripwire_normal = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ns"), 0, 0)};

        var tripwire_west = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_n"), 0, 270)};
        var tripwire_south = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_n"), 0, 180)};
        var tripwire_east = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_n"), 0, 90)};
        var tripwire_north = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_n"), 0, 0)};

        var tripwire_sw = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ne"), 0, 180)};
        var tripwire_nw = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ne"), 0, 270)};
        var tripwire_ew = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ns"), 0, 90)};
        var tripwire_es = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ne"), 0, 90)};
        var tripwire_en = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_ne"), 0, 0)};
        var tripwire_nsw = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_nse"), 0, 180)};
        var tripwire_esw = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_nse"), 0, 90)};
        var tripwire_enw = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_nse"), 0, 270)};
        var tripwire_ens = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_nse"), 0, 0)};

        var tripwire_all = new PolymerBlockModel[]{PolymerBlockModel.of(ResourceLocation.parse("minecraft:block/tripwire"+(attached?"_attached":"")+"_nsew"), 0, 0)};

        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.WEST, true), tripwire_west);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.SOUTH, true), tripwire_south);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true), tripwire_east);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true), tripwire_north);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.SOUTH, true).setValue(TripWireBlock.WEST, true), tripwire_sw);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.WEST, true), tripwire_nw);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.WEST, true), tripwire_ew);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.SOUTH, true), tripwire_es);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.SOUTH, true).setValue(TripWireBlock.WEST, true), tripwire_nsw);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.SOUTH, true).setValue(TripWireBlock.WEST, true), tripwire_esw);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.SOUTH, true), tripwire_normal);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.SOUTH, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.WEST, true), tripwire_all);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.WEST, true), tripwire_enw);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.EAST, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.SOUTH, true), tripwire_ens);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true).setValue(TripWireBlock.NORTH, true).setValue(TripWireBlock.EAST, true), tripwire_en);
        DefaultModelData.MODELS.put(Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, attached).setValue(TripWireBlock.DISARMED, true), tripwire_normal);

        ObjectArrayList<BlockState> list = new ObjectArrayList<>();

        // Generate all permutations of north, south, east, west being true or false
        var b = new boolean[]{true, false};
        for (boolean north : b) {
            for (boolean south : b) {
                for (boolean east : b) {
                    for (boolean west : b) {
                        BlockState state = Blocks.TRIPWIRE.defaultBlockState()
                                .setValue(TripWireBlock.ATTACHED, attached)
                                .setValue(TripWireBlock.DISARMED, true)
                                .setValue(TripWireBlock.NORTH, north)
                                .setValue(TripWireBlock.SOUTH, south)
                                .setValue(TripWireBlock.EAST, east)
                                .setValue(TripWireBlock.WEST, west);
                        list.add(state);
                        DefaultModelData.SPECIAL_REMAPS.put(state, state.setValue(TripWireBlock.DISARMED, false));
                    }
                }
            }
        }

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }
}
