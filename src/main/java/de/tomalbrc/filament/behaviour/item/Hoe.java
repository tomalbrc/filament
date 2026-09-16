package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.util.annotation.RegistryRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Hoe behaviour
 */
@Deprecated
public class Hoe implements ItemBehaviour<Hoe.Config> {
    private final Config config;

    public Hoe(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Hoe.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        var t = Filament.REGISTRY_ACCESS.compositeAccess().get(BlockTransformers.HOE).orElseThrow().value().transforms();
        for (BlockTransformer.BlockTransformData transformData : t) {
            var tr = transformData.blockStateProvider().value().getOptionalState(level, level.getRandom(),  blockPos);
            if (tr != null && !transformData.disallowedFaces().contains(useOnContext.getClickedFace())) {
                Player player = useOnContext.getPlayer();
                level.playSound(player, blockPos, SoundEvent.createVariableRangeEvent(config.sound), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide()) {
                    if (player != null) {
                        useOnContext.getItemInHand().hurtAndBreak(1, player, useOnContext.getHand());
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    public static class Config {
        @RegistryRef("sound_event")
        public Identifier sound = SoundEvents.HOE_TILL.key().identifier();
    }
}