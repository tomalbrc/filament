package de.tomalbrc.filament.decoration;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.holder.AnimatedDecorationHolder;
import de.tomalbrc.filament.decoration.holder.DecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ModelRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// good enough for an experimental test but at some point we should maybe streamline this implementation
public final class DecorationPreviewManager {
    private DecorationPreviewManager() {}

    private static final Map<UUID, PreviewState> ACTIVE = new ConcurrentHashMap<>();

    public static boolean toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        PreviewState existing = ACTIVE.remove(id);
        if (existing != null) {
            existing.destroy();
            return false;
        }

        if (!(player.getMainHandItem().getItem() instanceof DecorationItem)) return false;

        ACTIVE.put(id, new PreviewState(player));
        return true;
    }

    public static void tick(ServerPlayer player) {
        PreviewState state = ACTIVE.get(player.getUUID());
        if (state != null) state.tick();
    }

    public static void clear(ServerPlayer player) {
        PreviewState state = ACTIVE.remove(player.getUUID());
        if (state != null) state.destroy();
    }

    private record Target(BlockPos pos, Direction direction) {}

    private static final class PreviewState {
        private final ServerPlayer player;

        private FilamentDecorationHolder holder;
        private HolderAttachment attachment;
        private Identifier currentDecorationId;
        private Direction currentDirection;
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

            if (!data.id().equals(currentDecorationId) || target.direction() != currentDirection) {
                rebuild(data, target.direction());
                currentDecorationId = data.id();
                currentDirection = target.direction();
            }

            show();

            if (!target.pos().equals(currentPos) || currentYaw != angle) {
                if (attachment != null) attachment.destroy();
                holder.setYaw(angle);
                attachment = BlockBoundAttachment.ofTicking(holder.asPolymerHolder(), level, target.pos());
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

        private void rebuild(DecorationData data, Direction direction) {
            if (attachment != null) {
                attachment.destroy();
                attachment = null;
            }

            if (data.behaviour().has(Behaviours.ANIMATION)) {
                var anim = data.behaviour().get(Behaviours.ANIMATION);
                this.holder = new AnimatedDecorationHolder(anim, ModelRegistry.getModel(anim.model));
            } else {
                this.holder = new DecorationHolder(() -> player.getMainHandItem().copy());
            }

            DecorationUtil.setupElements(
                    holder,
                    data,
                    direction,
                    player.getYRot(),
                    player.getMainHandItem(),
                    (serverPlayer, hand, pos) -> InteractionResult.PASS
            );



            this.elements = new ArrayList<>(holder.asPolymerHolder().getElements());

            for (VirtualElement element : elements) {
                if (element instanceof ItemDisplayElement itemDisplayElement) {
                    itemDisplayElement.setGlowing(true);
                    itemDisplayElement.setGlowColorOverride(0x00FF00);
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