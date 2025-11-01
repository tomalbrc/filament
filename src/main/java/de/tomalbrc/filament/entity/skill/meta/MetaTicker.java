package de.tomalbrc.filament.entity.skill.meta;

import net.minecraft.server.level.ServerLevel;

public interface MetaTicker {
    void add(MetaSkillInstance instance);

    void tick(ServerLevel level);
}
