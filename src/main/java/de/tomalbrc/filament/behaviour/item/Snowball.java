package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Item behaviour for simple snowball shooting
 */
public class Snowball implements ItemBehaviour<Snowball.Config> {
    private final Config config;

    public Snowball(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Snowball.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        ItemBehaviour.super.init(item, behaviourHolder);
        if (config.dispenserSupport) DispenserBlock.registerBehavior(item, new SnowballDispenseBehavior(item, ProjectileItem.DispenseConfig.DEFAULT));
    }

    @Override
    public InteractionResult use(Item item, Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(net.minecraft.world.entity.projectile.Snowball::new, serverLevel, itemStack, player, 0.0F, config.power, config.inaccuracy);
        }

        player.awardStat(Stats.ITEM_USED.get(item));
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new net.minecraft.world.entity.projectile.Snowball(level, pos.x(), pos.y(), pos.z(), stack);
    }

    public List<String> commands() {
        return config.hitCommands == null ? this.config.hitCommand == null ? null : List.of(this.config.hitCommand) : config.hitCommands;
    }

    public List<String> entityHitCommands() {
        return config.entityHitCommands == null ? this.config.entityHitCommand == null ? null : List.of(this.config.entityHitCommand) : config.entityHitCommands;
    }

    public void run(net.minecraft.world.entity.projectile.Snowball snowball, Vec3 pos) {
        if (config.hitCommand == null && config.hitCommands == null)
            return;

        var owner = snowball.getOwner();
        if (owner instanceof ServerPlayer serverPlayer) {
            var cmd = commands();
            if (cmd != null)
                ExecuteUtil.asPlayer(serverPlayer, config.executeAtHit ? pos : null, cmd.toArray(new String[0]));
        }
    }

    public void runEntity(net.minecraft.world.entity.projectile.Snowball snowball, Vec3 pos, Entity entity) {
        if (config.entityHitCommand == null && config.entityHitCommands == null && config.hitCommand == null && config.hitCommands == null)
            return;

        var owner = snowball.getOwner();
        if (owner instanceof ServerPlayer serverPlayer) {
            var cmd = commands();
            var hitCmd = entityHitCommands();

            if (hitCmd == null) {
                hitCmd = cmd;
            }

            if (hitCmd == null)
                return;

            for (int i = 0; i < hitCmd.size(); i++) {
                hitCmd.set(i, hitCmd.get(i).replace("%target%", entity.getStringUUID()));
            }

            ExecuteUtil.asPlayer(serverPlayer, config.executeAtHit ? pos : null, hitCmd.toArray(new String[0]));
        }
    }

    public static class Config {
        public float inaccuracy = 1.f;
        public float power = 1.5f;

        public boolean dispenserSupport = false;

        public String hitCommand;
        public List<String> hitCommands;

        public String entityHitCommand;
        public List<String> entityHitCommands;

        public boolean executeAtHit = true;
    }

    public static class SnowballDispenseBehavior extends DefaultDispenseItemBehavior {
        private final SimpleItem projectileItem;
        private final ProjectileItem.DispenseConfig dispenseConfig;

        public SnowballDispenseBehavior(Item projectileItem, ProjectileItem.DispenseConfig dispenseConfig) {
            this.projectileItem = (SimpleItem)projectileItem;
            this.dispenseConfig = dispenseConfig;
        }

        @Override
        public @NotNull ItemStack execute(BlockSource blockSource, ItemStack item) {
            ServerLevel serverLevel = blockSource.level();
            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
            Position position = this.dispenseConfig.positionFunction().getDispensePosition(blockSource, direction);
            Projectile.spawnProjectileUsingShoot(this.projectileItem.getOrThrow(Behaviours.SNOWBALL).asProjectile(serverLevel, position, item, direction), serverLevel, item, direction.getStepX(), direction.getStepY(), direction.getStepZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
            return item;
        }

        @Override
        protected void playSound(BlockSource blockSource) {
            blockSource.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH), blockSource.pos(), 0);
        }
    }
}
