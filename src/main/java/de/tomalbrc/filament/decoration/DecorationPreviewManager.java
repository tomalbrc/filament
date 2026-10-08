package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.AnimatedDecorationHolder;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.generator.PreviewModelGenerator;
import de.tomalbrc.filament.injection.DecorationPreviewHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ModelRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.FilamentConfig;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DecorationPreviewManager {
    private DecorationPreviewManager() {}

    public static boolean toggle(ServerPlayer player) {
        DecorationPreviewHolder holder = (DecorationPreviewHolder) player;
        PreviewState existing = holder.filament$getPreviewState();
        if (existing != null) {
            existing.destroy();
            holder.filament$setPreviewState(null);
            return false;
        }

        if (!(player.getMainHandItem().getItem() instanceof DecorationItem)) return false;

        holder.filament$setPreviewState(new PreviewState(player));
        return true;
    }

    public static void tick(ServerPlayer player) {
        PreviewState state = ((DecorationPreviewHolder) player).filament$getPreviewState();
        if (state != null) state.tick();
    }

    public static void clear(ServerPlayer player) {
        DecorationPreviewHolder holder = (DecorationPreviewHolder) player;
        PreviewState state = holder.filament$getPreviewState();
        if (state != null) {
            state.destroy();
            holder.filament$setPreviewState(null);
        }
    }

    private static void applyPreviewSuffix(ItemStack stack) {
        var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);

        List<String> strings;
        if (cmd != null && !cmd.strings().isEmpty()) {
            strings = new ArrayList<>(cmd.strings());
            strings.set(0, strings.getFirst() + PreviewModelGenerator.PREVIEW_SUFFIX);
        } else {
            strings = List.of("default" + PreviewModelGenerator.PREVIEW_SUFFIX);
        }

        List<Float> floats = cmd != null ? cmd.floats() : List.of();
        List<Boolean> flags = cmd != null ? cmd.flags() : List.of();
        List<Integer> colors = cmd != null ? cmd.colors() : List.of();

        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
    }

    private record Target(BlockPos pos, Direction direction) {}

    public static final class PreviewState {
        private final ServerPlayer player;

        private FilamentDecorationHolder holder;
        private HolderAttachment attachment;
        private Identifier currentDecorationId;
        private Direction currentDirection;
        private BlockState currentBlockState;
        private List<VirtualElement> elements = new ArrayList<>();
        private boolean elementsAdded;

        private BlockPos currentPos;
        private float currentYaw = Float.NaN;

        private PreviewState(ServerPlayer player) {
            this.player = player;
        }

        private void tick() {
            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof DecorationItem item)) {
                hide();
                return;
            }

            DecorationData data = item.getDecorationData();

            HitResult hit = player.pick(player.blockInteractionRange(), 0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                hide();
                return;
            }

            ServerLevel level = player.level();
            BlockPos clickedPos = ((BlockHitResult)hit).getBlockPos();
            Direction clickedFace = ((BlockHitResult)hit).getDirection();
            BlockState clickedState = level.getBlockState(clickedPos);

            DecorationBlock block = DecorationRegistry.getDecorationBlock(data.id());
            if (block == null) {
                hide();
                return;
            }

            UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, (BlockHitResult)hit);
            BlockState blockState = block.getStateForPlacement(new BlockPlaceContext(useOnContext));
            if (blockState == null) {
                hide();
                return;
            }

            float angle = block.getVisualRotationYInDegrees(blockState);
            Target target = resolveTarget(level, data, clickedPos, clickedFace, clickedState, angle);
            if (target == null) {
                hide();
                return;
            }

            if (data.id() != currentDecorationId || target.direction() != currentDirection || !blockState.equals(currentBlockState)) {
                rebuild(item, data, target.direction(), blockState);
                currentDecorationId = data.id();
                currentDirection = target.direction();
                currentBlockState = blockState;
            }

            show();

            if (!target.pos().equals(currentPos) || currentYaw != angle) {
                if (attachment != null) attachment.destroy();
                holder.setYaw(angle);
                if (holder.isAnimated()) {
                    attachment = BlockBoundAttachment.ofTicking(holder.asPolymerHolder(), level, target.pos());
                } else {
                    attachment = BlockBoundAttachment.of(holder.asPolymerHolder(), level, target.pos());
                }
                currentPos = target.pos().immutable();
                currentYaw = angle;
            }
        }

        private Target resolveTarget(ServerLevel level, DecorationData data, BlockPos clickedPos, Direction clickedFace, BlockState clickedState, float angle) {
            DecorationProperties properties = data.properties();

            if (clickedState.canBeReplaced()) {
                if (properties.placement.canPlace(Direction.UP) && canPlaceAt(level, data, clickedPos, angle)) {
                    return new Target(clickedPos, Direction.UP);
                }
                return null;
            }

            BlockPos adjacent = clickedPos.relative(clickedFace);

            if (properties.placement.canPlace(clickedFace) && canPlaceAt(level, data, adjacent, angle)) {
                return new Target(adjacent, clickedFace);
            }

            if (clickedFace.getAxis().isHorizontal()
                    && !level.getBlockState(adjacent.below()).canBeReplaced()
                    && properties.placement.canPlace(Direction.UP)
                    && canPlaceAt(level, data, adjacent, angle)) {
                return new Target(adjacent, Direction.UP);
            }

            return null;
        }

        private boolean canPlaceAt(ServerLevel level, DecorationData data, BlockPos pos, float angle) {
            if (!level.getBlockState(pos).canBeReplaced()) return false;

            if (!data.hasBlocks()) return true;

            boolean[] ok = {true};
            DecorationUtil.forEachRotated(data.blocks(), pos, angle, p -> {
                if (!level.getBlockState(p).canBeReplaced()) ok[0] = false;
            });
            return ok[0];
        }

        private void rebuild(DecorationItem item, DecorationData data, Direction direction, BlockState blockState) {
            if (attachment != null) {
                attachment.destroy();
                attachment = null;
            }

            if (data.behaviour().has(Behaviours.ANIMATION)) {
                var anim = data.behaviour().get(Behaviours.ANIMATION);
                this.holder = new AnimatedDecorationHolder(anim, ModelRegistry.getModel(anim.model)) {
                    @Override
                    public boolean startWatching(ServerGamePacketListenerImpl player) {
                        return player.player == PreviewState.this.player && super.startWatching(player);
                    }
                };
            } else {
                this.holder = new DecorationHolder(() -> ItemStack.EMPTY) {
                    @Override
                    public boolean startWatching(ServerGamePacketListenerImpl player) {
                        return player.player == PreviewState.this.player && super.startWatching(player);
                    }
                };
            }

            ItemStack previewStack = DecorationUtil.placementAdjustedItem(
                    player.getMainHandItem(),
                    data.itemResource(),
                    direction
            );

            if (item.getBehaviours() != null) {
                for (Map.Entry<?, ?> entry : item.getBehaviours()) {
                    if (entry.getValue() instanceof DecorationBehaviour<?> behaviour) {
                        previewStack = behaviour.visualItemStack(null, previewStack, blockState);
                    }
                }
            }

            applyPreviewSuffix(previewStack);

            DecorationUtil.setupElements(
                    holder,
                    data,
                    direction,
                    player.getYRot(),
                    previewStack,
                    (_, _, _) -> InteractionResult.PASS
            );

            this.elements = new ArrayList<>(holder.asPolymerHolder().getElements());

            for (VirtualElement element : elements) {
                if (element instanceof ItemDisplayElement itemDisplayElement) {
                    itemDisplayElement.setGlowing(FilamentConfig.getInstance().previewGlow);
                    itemDisplayElement.setGlowColorOverride(FilamentConfig.getInstance().previewGlowColor);
                }
                holder.removeElement(element);
            }
            this.elements.removeIf(x -> x instanceof InteractionElement); // TODO: cleanup and fix for item-frame elements

            elementsAdded = false;

            this.currentPos = null;
            this.currentYaw = Float.NaN;
        }

        private void show() {
            if (elementsAdded) return;
            for (VirtualElement element : elements) {
                holder.addElement(element);
            }
            elementsAdded = true;
            holder.tick();
        }

        private void hide() {
            if (!elementsAdded) return;
            for (VirtualElement element : elements) {
                holder.removeElement(element);
            }
            elementsAdded = false;
            holder.tick();
        }

        private void destroy() {
            if (attachment != null) {
                attachment.destroy();
                attachment = null;
            }
            elements.clear();
            elementsAdded = false;
        }
    }
}