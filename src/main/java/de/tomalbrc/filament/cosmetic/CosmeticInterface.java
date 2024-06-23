package de.tomalbrc.filament.cosmetic;

import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.registry.filament.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

public interface CosmeticInterface {
    void filament$addHolder(ServerPlayer serverPlayer, SimpleItem simpleItem, ItemStack itemStack);

    void filament$destroyHolder();
}
