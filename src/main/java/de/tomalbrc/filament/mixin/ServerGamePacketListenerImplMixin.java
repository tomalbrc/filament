package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.util.VirtualDestroyStage;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements VirtualDestroyStage.ServerGamePacketListenerExtF {
    @Unique
    private final VirtualDestroyStage filament$virtualDestroyStageF = new VirtualDestroyStage();

    @Override
    public VirtualDestroyStage filament$getVirtualDestroyStage() {
        return this.filament$virtualDestroyStageF;
    }
}
