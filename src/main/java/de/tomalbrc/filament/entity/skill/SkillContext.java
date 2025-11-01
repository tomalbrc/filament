package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.FilamentMob;
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
        FilamentMob caster,
        @Nullable List<Target> targets,
        Vec3 origin,
        Trigger trigger,
        Map<String, Variable> vars
) {
    public List<Entity> getNearbyEntities(double radius) {
        Vec3 pos = caster.position();
        AABB box = new AABB(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );
        return caster.level().getEntities(caster, box);
    }

    public SkillContext withTargets(List<Target> targets) {
        return new SkillContext(this.level, this.caster, targets, this.origin, this.trigger, this.vars);
    }
}