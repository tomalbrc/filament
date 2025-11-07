package de.tomalbrc.filament.entity.skill;

import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SkillTree {
    private final Level level;
    public final SkilledEntity<?> caster;
    private final Target trigger;
    private final Vec3 origin;
    private final Map<String, Variable> vars = new HashMap<>(); // skill-scoped vars
    private final Deque<Skill> stack = new ArrayDeque<>();
    private List<Target> currentTargets; // inherited targets
    private int delayRemaining = 0;

    private boolean cancelled = false;
    private ExecutionResult result = ExecutionResult.NULL;

    public SkillTree(List<Skill> skills, SkilledEntity<?> caster, Target trigger, Vec3 origin, List<Target> inheritedTargets) {
        this.level = caster.level();
        this.caster = caster;
        this.trigger = trigger;
        this.origin = origin;
        this.currentTargets = inheritedTargets == null ? List.of() : new ArrayList<>(inheritedTargets);

        skills.forEach(stack::push);
    }

    public SkillTree copyWith(Skill skill) {
        return new SkillTree(List.of(skill), caster, trigger, origin, currentTargets);
    }

    public SkillTree copyWith(List<Skill> skills) {
        return new SkillTree(skills, caster, trigger, origin, currentTargets);
    }

    public SkillTree copyWith(List<Skill> skills, List<Target> targets) {
        return new SkillTree(skills, caster, trigger, origin, targets);
    }

    public SkillTree copyWithTargets(List<Target> targets) {
        return new SkillTree(List.of(), caster, trigger, origin, targets);
    }

    public boolean isFinished() {
        return stack.isEmpty() || cancelled;
    }

    public InteractionResult tick() {
        if (cancelled) return InteractionResult.FAIL;
        if (delayRemaining > 0) { delayRemaining--; return InteractionResult.PASS; }

        while (!stack.isEmpty()) {
            Skill skill = stack.pollLast();
            Mechanic mechanic = skill.mechanic();

            List<Target> targets = null;
            if (skill.targeter() != null) {
                targets = skill.targeter().find(this);
                targets = skill.targeter().sort(caster.level(), caster.threatTable(), caster.position(), targets);

            }

            if (!skill.canRun(caster)) {
                continue;
            }

            ExecutionResult executed = mechanic.execute(targets == null ? this : this.copyWithTargets(targets));
            if (executed.delay() > 0) {
                this.delayRemaining = executed.delay();
                return InteractionResult.PASS;
            } else {
                return executed.result();
            }
        }

        return InteractionResult.PASS;
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
        AABB box = AABB.unitCubeFromLowerCorner(caster().position()).inflate(radius);
        return caster.level().getEntities((Entity) caster, box);
    }

    public <T extends Entity & SkilledEntity<?>> T caster() {
        return (T) caster;
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

    public MobSkills<?> mobSkills() {
        return caster.mobSkills();
    }

    public ExecutionResult result() {
        return ExecutionResult.NULL;
    }
}