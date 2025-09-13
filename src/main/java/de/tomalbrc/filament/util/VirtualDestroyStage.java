package de.tomalbrc.filament.util;

import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDestroyStage extends ElementHolder {
    public static final ItemStack[] DESTROY_STAGE_MODELS = new ItemStack[10];
    private final List<ItemDisplayElement> destroyElements = new ObjectArrayList<>();
    private int state;

    public VirtualDestroyStage() {
        for (int i = 0; i < 32; i++) {
            var element = new ItemDisplayElement();
            element.setItem(DESTROY_STAGE_MODELS[0]);
            element.setScale(new Vector3f(1.01f));
            this.destroyElements.add(element);
        }
    }

    private List<ItemDisplayElement> destroyElements() {
        return this.destroyElements;
    }


    @Override
    public void destroy() {
        this.destroyElements.forEach(this::removeElement);
        super.destroy();
    }

    public void setState(int i) {
        if (this.state == i) {
            return;
        }

        this.state = i;
        this.destroyElements.forEach(x -> x.setItem(i < 0 ? ItemStack.EMPTY : DESTROY_STAGE_MODELS[Math.min(i, DESTROY_STAGE_MODELS.length - 1)]));
        this.tick();
    }



    @SuppressWarnings("SameReturnValue")
    public static boolean updateState(ServerPlayer player, BlockPos pos, BlockState state, int i) {
        final VirtualDestroyStage self = ((ServerGamePacketListenerExtF) player.connection).filament$getVirtualDestroyStage();

        if (i == -1 || (!(state.getBlock() instanceof Marker && (state.getBlock() instanceof DecorationBlock decorationBlock && decorationBlock.getDecorationData().hasBlocks())))) {
            self.setState(-1);
            if (self.getAttachment() != null) {
                self.destroy();
            }
            return true;
        }

        var vecPos = Vec3.atCenterOf(pos);

        if (self.getAttachment() == null || !self.getAttachment().getPos().equals(vecPos)) {
            // init display list
            ChunkAttachment.of(self, player.serverLevel(), vecPos);
            self.destroyElements().get(0).setTranslation(new Vector3f());

            if (state.getBlock() instanceof DecorationBlock decorationBlock1 && decorationBlock1.getDecorationData().hasBlocks()) {
                for (int i1 = 0; i1 < decorationBlock1.getDecorationData().countBlocks(); i1++) {
                    self.addElement(self.destroyElements().get(i1));
                }

                BlockEntity blockEntity = player.serverLevel().getBlockEntity(pos);
                if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
                    final AtomicInteger index = new AtomicInteger();
                    final DecorationBlockEntity finalDecorationBlockEntity = decorationBlockEntity.isMain() ? decorationBlockEntity : decorationBlockEntity.getMainBlockEntity();
                    if (finalDecorationBlockEntity != null) {
                        Util.forEachRotated(finalDecorationBlockEntity.getDecorationData().blocks(), finalDecorationBlockEntity.getBlockPos(), finalDecorationBlockEntity.getVisualRotationYInDegrees(), rotPos -> {
                            BlockPos op = rotPos.subtract(pos);
                            self.destroyElements.get(index.getAndIncrement()).setTranslation(new Vector3f(op.getX(), op.getY(), op.getZ()));
                        });
                    }
                }
            }
        }

        self.setState(i);

        return true;
    }

    public static void destroy(@Nullable ServerPlayer player) {
        if (player != null && player.connection != null) {
            ((ServerGamePacketListenerExtF) player.connection).filament$getVirtualDestroyStage().destroy();
        }
    }

    static {
        for (int i = 0; i < DESTROY_STAGE_MODELS.length; i++) {
            DESTROY_STAGE_MODELS[i] = PolymerResourcePackUtils.requestModel(Items.STICK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/special/destroy_stage_" + i)).asStack();
        }

        var model =  """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "minecraft:block/destroy_stage_|ID|"
                  }
                }
                """;

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(x -> {
            for (var i = 0; i < DESTROY_STAGE_MODELS.length; i++) {
                x.addData("assets/filament/models/item/special/destroy_stage_" + i + ".json", model.replace("|ID|", "" + i).getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    public interface Marker {}

    public interface ServerGamePacketListenerExtF {
        VirtualDestroyStage filament$getVirtualDestroyStage();
    }
}