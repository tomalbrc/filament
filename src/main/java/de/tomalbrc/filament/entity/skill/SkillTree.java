package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SkillTree {
    private final MobSkills mobSkills;
    private final Level level;
    public final FilamentMob caster;
    private final Target trigger;
    private final Vec3 origin;
    private final Map<String, Variable> vars = new HashMap<>(); // skill-scoped vars
    private final Deque<Skill> stack = new ArrayDeque<>();
    private List<Target> currentTargets; // inherited targets
    private int delayRemaining = 0;

    private boolean cancelled = false;

    public SkillTree(MobSkills mobSkills, List<Skill> skills, FilamentMob caster, Target trigger, Vec3 origin, List<Target> inheritedTargets) {
        this.mobSkills = mobSkills;
        this.level = caster.level();
        this.caster = caster;
        this.trigger = trigger;
        this.origin = origin;
        this.currentTargets = inheritedTargets == null ? List.of() : new ArrayList<>(inheritedTargets);

        skills.forEach(stack::push);
    }

    public SkillTree copyWith(Skill skill) {
        return new SkillTree(mobSkills, List.of(skill), caster, trigger, origin, currentTargets);
    }

    public SkillTree copyWith(List<Skill> skills) {
        return new SkillTree(mobSkills, skills, caster, trigger, origin, currentTargets);
    }

    public boolean isFinished() {
        return stack.isEmpty() || cancelled;
    }

    public void tick() {
        if (cancelled) return;
        if (delayRemaining > 0) { delayRemaining--; return; }

        while (!stack.isEmpty()) {
            Skill top = stack.pop();
            Mechanic mechanic = top.mechanic();
            // todo: target conditions
            int delay = mechanic.execute(this);
            if (delay > 0) {
                this.delayRemaining = delay;
                return;
            }
        }
    }

    public void setCurrentTargets(List<Target> newTargets) {
        this.currentTargets = newTargets;
    }

    public List<Target> getCurrentTargets() { return currentTargets; }

    public void cancel() { cancelled = true; }

    public boolean isCancelled() {
        return cancelled;
    }

    public List<Entity> getNearbyEntities(double radius) {
        Vec3 pos = caster.position();
        AABB box = new AABB(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );
        return caster.level().getEntities(caster, box);
    }

    public FilamentMob caster() {
        return caster;
    }

    public Map<String, Variable> vars() {
        return this.vars;
    }

    public Level level() {
        return level;
    }

    public Vec3 origin() {
        return origin;
    }

    public Target trigger() {
        return trigger;
    }

    public MobSkills mobSkills() {
        return mobSkills;
    }
}