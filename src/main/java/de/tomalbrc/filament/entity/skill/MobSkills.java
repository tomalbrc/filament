package de.tomalbrc.filament.entity.skill;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.entity.FilamentMob;
import de.tomalbrc.filament.entity.skill.target.Target;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MobSkills {
    private static final Map<String, Variable> GLOBAL_VARIABLES = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceKey<Level>, Map<String, Variable>> LEVEL_VARIABLES = new Object2ObjectOpenHashMap<>();

    private final Map<Trigger, List<Skill>> skills = new EnumMap<>(Trigger.class);
    private final FilamentMob parent;

    public MobSkills(FilamentMob parent) {
        this.parent = parent;
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

    public void fireTrigger(Trigger trigger) {
        var ctx = new SkillContext(
                (ServerLevel) this.parent.level(),
                this.parent,
                ImmutableList.of(),
                this.parent.position(),
                Trigger.ON_TIMER,
                this.parent.getVariables()
        );

        List<Skill> skills = this.skills.get(trigger);
        if (skills == null) return;

        for (Skill skill : skills) {
            if (skill.healthCondition() != null && !skill.healthCondition().isMet(ctx.caster())) continue;
            if (skill.chance() != null && skill.chance() < 1.0 && this.parent.getRandom().nextDouble() > skill.chance()) continue;

            List<Target> targets = skill.targeter().find(ctx);
            skill.mechanic().execute(ctx.withTargets(targets));
        }
    }

    public void tick(ServerLevel serverLevel) {
        this.fireTrigger(Trigger.ON_TIMER);
    }

    public void onAttack(ServerLevel serverLevel, Entity entity) {

    }

    public void onSpawn() {
        this.onSpawnOrLoad();
    }

    public void onDespawn() {

    }

    public void onLoad() {
        this.onSpawnOrLoad();
    }

    public void onSpawnOrLoad() {

    }

    public void onDeath() {

    }

    public void onTimer() {

    }

    public void onInteract(Player player, InteractionHand hand) {

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

    public void onBreed(Animal parent) {

    }

    public void onChangeWorld() {

    }

    public void onChangeTarget(LivingEntity entity) {

    }

    public void onBucket() {

    }

    public void onSkillDamage(Skill skill, SkillContext context) {

    }

    public void onDamage(ServerLevel level, DamageSource damageSource, float amount) {

    }
}