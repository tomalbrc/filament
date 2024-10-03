package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DropXp implements BlockBehaviour<DropXp.Config> {
    private final Config config;

    public DropXp(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public DropXp.Config getConfig() {
        return this.config;
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        if (bl) {
            this.spawnXpAfterEnchantCheck(serverLevel, blockPos, itemStack, this.getRange());
        }
    }

    private IntProvider getRange() {
        if (this.config.max == this.config.min) {
            return ConstantInt.of(this.config.min);
        }

        return UniformInt.of(this.config.min, this.config.max);
    }

    protected void spawnXpAfterEnchantCheck(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, IntProvider intProvider) {
        int i = EnchantmentHelper.processBlockExperience(serverLevel, itemStack, intProvider.sample(serverLevel.getRandom()));
        if (i > 0) {
            this.spawnXp(serverLevel, blockPos, i);
        }
    }

    private void spawnXp(ServerLevel serverLevel, BlockPos blockPos, int amount) {
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(blockPos), amount);
        }
    }

    public static class Config {
        public int min = 0;
        public int max = 0;
    }
}