package de.tomalbrc.filament.util;

import de.tomalbrc.filament.decoration.block.DecorationBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.nio.charset.StandardCharsets;

public class VirtualDestroyStage extends ElementHolder {
    public static final ItemStack[] MODELS = new ItemStack[10];
    private final ItemDisplayElement main = new ItemDisplayElement();
    private int state;


    public VirtualDestroyStage() {
        this.main.setItem(MODELS[0]);
        this.main.setScale(new Vector3f(1.01f));
        this.addElement(this.main);
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean updateState(ServerPlayer player, BlockPos pos, BlockState state, int i) {
        VirtualDestroyStage self = ((ServerGamePacketListenerExtF) player.connection).filament$getVirtualDestroyStage();

        if (i == -1 || (!(state.getBlock() instanceof Marker && (state.getBlock() instanceof DecorationBlock decorationBlock && decorationBlock.getDecorationData().hasBlocks())))) {
            self.setState(-1);
            if (self.getAttachment() != null) {
                self.destroy();
            }
            return true;
        }

        var vecPos = Vec3.atCenterOf(pos);

        if (self.getAttachment() == null || !self.getAttachment().getPos().equals(vecPos)) {
            ChunkAttachment.of(self, player.serverLevel(), vecPos);
        }

        self.setState(i);

        return true;
    }

    public static void destroy(@Nullable ServerPlayer player) {
        if (player != null && player.connection != null) {
            ((ServerGamePacketListenerExtF) player.connection).filament$getVirtualDestroyStage().destroy();
        }
    }

    public void setState(int i) {
        if (this.state == i) {
            return;
        }

        this.state = i;
        this.main.setItem(i < 0 ? ItemStack.EMPTY : MODELS[Math.min(i, MODELS.length - 1)]);
        this.tick();
    }


    static {
        for (int i = 0; i < MODELS.length; i++) {
            MODELS[i] = PolymerResourcePackUtils.requestModel(Items.STICK, ResourceLocation.fromNamespaceAndPath("filament", "block/special/destroy_stage_" + i)).asStack();
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
            for (var i = 0; i < MODELS.length; i++) {
                x.addData("assets/filament/models/block/special/destroy_stage_" + i + ".json", model.replace("|ID|", "" + i).getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    public interface Marker {}

    public interface ServerGamePacketListenerExtF {
        VirtualDestroyStage filament$getVirtualDestroyStage();
    }
}