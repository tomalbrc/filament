package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.VirtualDestroyStage;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements VirtualDestroyStage.ServerGamePacketListenerExtF {
    @Shadow protected abstract void tryPickItem(ItemStack itemStack);

    @Unique
    private final VirtualDestroyStage filament$virtualDestroyStageF = new VirtualDestroyStage();

    @Override
    public VirtualDestroyStage filament$getVirtualDestroyStage() {
        return this.filament$virtualDestroyStageF;
    }

    @Inject(method = "handlePickItemFromEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryPickItem(Lnet/minecraft/world/item/ItemStack;)V"), cancellable = true)
    private void filament$handleEntityPick(ServerboundPickItemFromEntityPacket serverboundPickItemFromEntityPacket, CallbackInfo ci) {
        var v = DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.get(serverboundPickItemFromEntityPacket.id());
        if (v != null) {
            this.tryPickItem(v);
            ci.cancel();
        }
    }
}
