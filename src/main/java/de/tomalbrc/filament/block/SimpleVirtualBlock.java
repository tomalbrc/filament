package de.tomalbrc.filament.block;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.http.annotation.Obsolete;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class SimpleVirtualBlock extends SimpleBlock implements BlockWithElementHolder {
    Map<BlockState, String> cmdMap = new Reference2ObjectOpenHashMap<>();
    protected Map<BlockState, BlockState> reverseStateMap = new Reference2ReferenceOpenHashMap<>();

    public SimpleVirtualBlock(Properties properties, BlockData data) {
        super(properties, data);
    }

    @Obsolete
    public void postRegister() {
        super.postRegister();

        for (Map.Entry<BlockState, BlockData.BlockStateMeta> entry : this.stateMap.entrySet()) {
            for (Map.Entry<String, ResourceLocation> set2 : this.blockData.blockResource().getModels().entrySet()) {
                boolean same = set2.getValue().equals(entry.getValue().polymerBlockModel().model());
                if (same) {
                    this.cmdMap.put(entry.getKey(), set2.getKey());
                }
            }
        }
        for (Map.Entry<BlockState, BlockData.BlockStateMeta> entry : this.stateMap.entrySet()) {
            this.reverseStateMap.put(entry.getValue().blockState(), entry.getKey());
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        if (this.blockData.block() != null) {
            return this.blockData.block();
        }

        return super.getPolymerBlockState(blockState, packetContext);
    }

    @Nullable
    public ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new VirtualBlockHolder(initialBlockState);
    }

    public static class VirtualBlockHolder extends ElementHolder {
        @NotNull private final SimpleVirtualBlock virtualBlock;
        @NotNull private final ItemStack displayStack;
        @NotNull private final ItemDisplayElement displayElement;

        public VirtualBlockHolder(BlockState blockState) {
            this.virtualBlock = (SimpleVirtualBlock) blockState.getBlock();
            this.displayStack = this.virtualBlock.asItem().getDefaultInstance();
            this.displayStack.set(DataComponents.ITEM_MODEL, virtualBlock.blockData.id().withPrefix("block/"));

            this.displayElement = new ItemDisplayElement(this.displayStack);
            this.displayElement.setItemDisplayContext(ItemDisplayContext.NONE);
            this.displayElement.setScale(new Vector3f(1.0001f));
            this.displayElement.setDisplaySize(1.f, 1.f);
            this.displayElement.setInvisible(true);
            update(blockState);
            this.addElement(this.displayElement);
        }

        private BlockAwareAttachment attachment() {
            return (BlockAwareAttachment) this.getAttachment();
        }

        public void update(BlockState blockState) {
            this.displayStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                    ImmutableList.of(),
                    ImmutableList.of(),
                    ImmutableList.of(this.customModelDataSelector(blockState)),
                    ImmutableList.of()
            ));

            var polymerBlockModel = this.virtualBlock.stateMap.get(blockState).polymerBlockModel();
            this.displayElement.setLeftRotation(new Quaternionf().rotateX(polymerBlockModel.x() * Mth.DEG_TO_RAD).rotateY((polymerBlockModel.y() + 180.f) * Mth.DEG_TO_RAD));
        }

        private String customModelDataSelector(BlockState blockState) {
            var newState = this.virtualBlock.getPolymerBlockState(blockState, PacketContext.create());
            return this.virtualBlock.cmdMap.get(this.virtualBlock.reverseStateMap.get(newState));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            super.notifyUpdate(updateType);

            var attachment = this.attachment();
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && attachment != null) {
                this.update(attachment.getBlockState());
            }
        }

        @Override
        protected void onAttachmentRemoved(HolderAttachment oldAttachment) {
            if (oldAttachment instanceof BlockAwareAttachment blockAwareAttachment && blockAwareAttachment.isPartOfTheWorld()) {
                var pos = oldAttachment.getPos();
                DecorationUtil.showBreakParticle(oldAttachment.getWorld(), this.displayStack, (float)pos.x(), (float)pos.y(), (float)pos.z());
            }
            super.onAttachmentRemoved(oldAttachment);
        }
    }
}