package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;

public class HorizontalFacing extends AbstractHorizontalFacing<HorizontalFacing.Config> implements BlockBehaviour<HorizontalFacing.Config> {
    public HorizontalFacing(Config config) {
        super(config);
    }

    public static class Config {}
}