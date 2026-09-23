package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.mixin.accessor.PolymerEntityAttachmentAccessor;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShulkerCollisionElement extends GenericEntityElement {

    private InteractionHandler handler = InteractionHandler.EMPTY;

    @Nullable private AABB cachedBox;
    @Nullable private Vec3 cachedOrigin;

    public ShulkerCollisionElement() {
        this.syncedData.set(EntityData.SILENT, true);
        this.syncedData.set(EntityData.NO_GRAVITY, true);
        this.syncedData.set(EntityData.FLAGS, (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX));
    }

    public void setHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    @Override
    public InteractionHandler getInteractionHandler(ServerPlayer player) {
        return this.handler;
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityTypes.SHULKER;
    }

    @Nullable
    public Vec3 worldPosition() {
        var holder = this.getHolder();
        if (holder == null) return null;
        if (!(holder.getAttachment() instanceof EntityAttachment attachment)) return null;
        return ((PolymerEntityAttachmentAccessor)attachment).getEntity().position().add(this.getOffset());
    }

    @Nullable
    public AABB worldBox() {
        Vec3 origin = this.worldPosition();
        if (origin == null) return null;

        if (this.cachedBox != null && cachedOrigin != null && origin.equals(this.cachedOrigin)) {
            return this.cachedBox;
        }

        this.cachedOrigin = origin;
        this.cachedBox = new AABB(
                origin.x - 0.5, origin.y,       origin.z - 0.5,
                origin.x + 0.5, origin.y + 1.0, origin.z + 0.5
        );
        return this.cachedBox;
    }

    @Override
    public void tick() {
        super.tick();
        this.cachedOrigin = null;
    }
}