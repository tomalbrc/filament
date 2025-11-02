package de.tomalbrc.filament.entity.skill;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MobSkills {
    private static final Map<String, Variable> GLOBAL_VARIABLES = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceKey<Level>, Map<String, Variable>> LEVEL_VARIABLES = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, Map<String, Variable>> ENTITY_VARIABLES = new Object2ObjectOpenHashMap<>();

    private final Map<SkillTrigger, List<Skill>> skills = new EnumMap<>(SkillTrigger.class);
    private final FilamentMob parent;

    // per-mob cooldowns: skillId -> expiryServerTick
    private final Object2LongOpenHashMap<ResourceLocation> cooldowns = new Object2LongOpenHashMap<>();

    // active skilltrees for this mob
    private final List<SkillTree> activeTrees = new CopyOnWriteArrayList<>();

    public MobSkills(FilamentMob parent) {
        this.parent = parent;
    }

    public static Map<String, Variable> getEntityVariables(UUID entity) {
        return ENTITY_VARIABLES.computeIfAbsent(entity, (x) -> new Object2ObjectOpenHashMap<>());
    }

    public static Map<String, Variable> getWorldVariables(ResourceKey<Level> world) {
        return LEVEL_VARIABLES.computeIfAbsent(world, (x) -> new Object2ObjectOpenHashMap<>());
    }

    public static Map<String, Variable> getGlobalVariables() {
        return GLOBAL_VARIABLES;
    }

    public void add(Skill skill) {
        this.skills.computeIfAbsent(skill.trigger(), k -> new ArrayList<>()).add(skill);
    }

    public boolean isOnCooldown(ResourceLocation skillId, Level serverLevel) {
        long now = serverLevel.getGameTime();
        long expiry = this.cooldowns.getLong(skillId); // 0 if absent
        return expiry > now;
    }

    public void setCooldown(ResourceLocation skillId, int cooldownTicks, Level serverLevel) {
        long expiry = serverLevel.getGameTime() + Math.max(0, cooldownTicks);
        this.cooldowns.put(skillId, expiry);
    }

    public long getCooldownRemaining(String skillId, Level serverLevel) {
        long now = serverLevel.getGameTime();
        long expiry = this.cooldowns.getLong(skillId);
        return Math.max(0L, expiry - now);
    }

    public List<Target> applyTargetConditions(List<Condition> targetConditions, List<Target> inputTargets, SkillTree tree) {
        if (targetConditions == null || targetConditions.isEmpty()) return inputTargets == null ? List.of() : inputTargets;
        if (inputTargets == null || inputTargets.isEmpty()) return List.of();

        List<Target> out = new ArrayList<>();
        for (Target t : inputTargets) {
            boolean success = true;
            for (Condition cond : targetConditions) {
                if (!cond.test(tree, t)) {
                    success = false;
                    break;
                }
            }
            if (success) out.add(t);
        }

        return out;
    }

    public void submitTree(SkillTree tree) {
        tree.tick();
        if (!tree.isFinished()) activeTrees.add(tree);
    }

    public void cancelAllTrees() {
        for (SkillTree tree : activeTrees) tree.cancel();
        activeTrees.clear();
    }

    public void tick(ServerLevel serverLevel) {
        if (!this.parent.isAlive() || this.parent.isRemoved()) {
            cancelAllTrees();
            return;
        }

        fireTrigger(SkillTrigger.ON_TIMER, Target.of());

        // tick and cleanup active trees
        for (Iterator<SkillTree> it = activeTrees.iterator(); it.hasNext();) {
            SkillTree tree = it.next();

            if (!this.parent.isAlive() || this.parent.isRemoved()) {
                tree.cancel();
                it.remove();
                continue;
            }

            tree.tick();

            // remove finished trees
            if (tree.isFinished()) it.remove();
        }
    }

    public void fireTrigger(SkillTrigger skillTrigger, Target triggerer) {
        List<Skill> list = this.skills.get(skillTrigger);
        if (list == null) return;

        for (Skill skill : list) {
            if (skillTrigger == SkillTrigger.ON_TIMER && skill.time() != 0 && parent.tickCount % skill.time() != 0)
                continue;

            if (!skill.canRun(this.parent))
                continue;

            SkillTree tree = new SkillTree(
                    this,
                    ImmutableList.of(skill),
                    this.parent,
                    triggerer,
                    this.parent.position(),
                    ImmutableList.of() // inherited targets are resolved via the targeter
            );

            // resolve targets via the targeter on the new tree
            List<Target> targets = skill.targeter().find(tree);
            tree.setCurrentTargets(targets);

            submitTree(tree);
        }
    }

    public void onAttack(ServerLevel serverLevel, Entity entity) {
        fireTrigger(SkillTrigger.ON_ATTACK, Target.of(entity));
    }

    public void onSpawn() {
        this.fireTrigger(SkillTrigger.ON_SPAWN, Target.of(this.parent));
        this.onSpawnOrLoad();
    }

    public void onDespawn() {
        this.fireTrigger(SkillTrigger.ON_DESPAWN, Target.of(this.parent));
    }

    public void onLoad() {
        this.fireTrigger(SkillTrigger.ON_LOAD, Target.of(this.parent));
        this.onSpawnOrLoad();
    }

    public void onSpawnOrLoad() {
        this.fireTrigger(SkillTrigger.ON_SPAWN_OR_LOAD, Target.of(this.parent));
    }

    public void onDeath() {
        this.fireTrigger(SkillTrigger.ON_DEATH, Target.of(this.parent));
    }

    public void onInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) this.fireTrigger(SkillTrigger.ON_INTERACT, Target.of(player));
    }

    public void onBreed(Animal parent) {
        this.fireTrigger(SkillTrigger.ON_BREED, Target.of(parent));
    }

    public void onChangeTarget(LivingEntity entity) {
        this.fireTrigger(SkillTrigger.ON_CHANGE_TARGET, Target.of(entity));
    }

    public void onDamage(ServerLevel level, DamageSource damageSource, float amount) {
        this.fireTrigger(SkillTrigger.ON_DAMAGED, Target.of(damageSource.getEntity()));
    }

    public void onChangeWorld() {

    }

    public void onBucket() {

    }

    public void onSkillDamage(Skill skill, SkillTree tree) {

    }

    public void onPlayerKill(Player player) {

    }

    public void onExplode() {

    }

    public void onPrime() {

    }

    public void onCreeperCharge() {

    }

    public void onSignal(String signal) {

    }

    public void onShoot(Projectile projectile) {

    }

    public void onBowHit(Projectile projectile) {

    }

    public void onTame() {

    }
}