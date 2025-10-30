package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @param caster         who triggered it
 * @param targets        targets from target selector
 * @param origin         where the skill originated
 * @param trigger        e.g. "on_attack", "on_block_break"
 */
public record SkillContext(
        ServerLevel level,
        LivingEntity caster,
        @Nullable List<Target> targets,
        Vec3 origin,
        Trigger trigger,
        Map<String, Object> vars
) {
    public List<Entity> getNearbyEntities(double radius) {
        return caster.level().getEntities(caster, AABB.ofSize(caster.position().subtract(radius / 2.), radius / 2., radius / 2., radius / 2.));
    }

    public SkillContext withTargets(List<Target> targets) {
        return new SkillContext(this.level, this.caster, targets, this.origin, this.trigger, this.vars);
    }
}