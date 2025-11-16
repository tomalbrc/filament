package de.tomalbrc.filament.block;

import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.mixin.accessors.ItemDisplayEntityAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class SimpleVirtualBlock extends SimpleBlock implements BlockWithElementHolder {
    private final Map<BlockData.BlockStateMeta, PolymerModelData> cmdMap = new Reference2ObjectOpenHashMap<>();

    public SimpleVirtualBlock(Properties properties, BlockData data) {
        super(properties, data);
    }

    @Override
    public void postRegister() {
        super.postRegister();

        for (Map.Entry<BlockState, BlockData.BlockStateMeta> stateMapEntry : this.stateMap.entrySet()) {
            for (Map.Entry<String, ResourceLocation> blockResourceModelsEntry : this.blockData.blockResource().getModels().entrySet()) {
                boolean same = blockResourceModelsEntry.getValue().equals(stateMapEntry.getValue().polymerBlockModel().model());
                if (same) {
                    this.cmdMap.put(stateMapEntry.getValue(), PolymerResourcePackUtils.requestModel(data().vanillaItem(), stateMapEntry.getValue().polymerBlockModel().model()));
                }
            }
        }
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

            this.displayElement = new ItemDisplayElement(this.displayStack);
            this.displayElement.setModelTransformation(ItemDisplayContext.NONE);
            this.displayElement.setScale(new Vector3f(1.0001f));
            this.displayElement.setDisplaySize(1.f, 1.f);
            this.displayElement.setInvisible(true);
            update(blockState, false);
            this.addElement(this.displayElement);
        }

        private BlockAwareAttachment attachment() {
            return (BlockAwareAttachment) this.getAttachment();
        }

        public void update(BlockState blockState, boolean update) {
            var state = this.virtualBlock.behaviourModifiedBlockState(blockState, blockState);
            var meta = this.virtualBlock.stateMap.get(state);
            this.displayStack.set(DataComponents.CUSTOM_MODEL_DATA, this.virtualBlock.cmdMap.get(meta).asComponent());

            var polymerBlockModel = meta.polymerBlockModel();
            this.displayElement.setLeftRotation(new Quaternionf().rotateX(polymerBlockModel.x() * Mth.DEG_TO_RAD).rotateY((-polymerBlockModel.y()+180) * Mth.DEG_TO_RAD));
            if (update) {
                this.displayElement.getDataTracker().setDirty(ItemDisplayEntityAccessor.getITEM(), true);
                this.displayElement.tick();
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            super.notifyUpdate(updateType);

            var attachment = this.attachment();
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && attachment != null) {
                this.update(attachment.getBlockState(), true);
            }
        }
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        if (blockData.properties().showBreakParticles) {
            var attachment = BlockBoundAttachment.get(player.level(), blockPos);
            if (attachment != null && player.level() instanceof ServerLevel serverLevel) {
                var holder = (VirtualBlockHolder)attachment.holder();
                DecorationUtil.showBreakParticleShaped(serverLevel, blockPos, blockState, holder.displayStack);
                BlockUtil.playBreakSound(serverLevel, blockPos, blockState);
            }
        }
    }
}