package de.tomalbrc.filament.mixin.injection;

import de.tomalbrc.filament.injection.FilamentBlockExtension;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements FilamentBlockExtension {

}
