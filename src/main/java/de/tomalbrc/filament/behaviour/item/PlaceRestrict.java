package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceRestrict implements ItemBehaviour<PlaceRestrict.Config> {
    private final Config config;

    public PlaceRestrict(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public PlaceRestrict.Config getConfig() {
        return this.config;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        var pos = useOnContext.getClickedPos();

        var state = useOnContext.getLevel().getBlockState(pos.below());
        if (config.blocks != null && config.blocks.contains(state.getBlock()))
            return InteractionResult.PASS;
        if (config.tags != null) {
            for (ResourceLocation tag : config.tags) {
                var tagKey = TagKey.create(Registries.BLOCK, tag);
                if (state.is(tagKey))
                    return InteractionResult.PASS;
            }
        }

        return InteractionResult.CONSUME;
    }

    public static class Config {
        public List<Block> blocks;
        public List<ResourceLocation> tags;
    }
}
