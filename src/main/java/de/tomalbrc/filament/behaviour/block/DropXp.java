package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
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
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean doSpawn) {
        if (doSpawn) {
            this.spawnXpAfterEnchantCheck(serverLevel, blockPos, itemStack, this.getRange(blockState));
        }
    }

    private IntProvider getRange(BlockState blockState) {
        if (this.config.max == this.config.min) {
            return ConstantInt.of(this.config.min.getOrDefault(blockState, 0));
        }

        return UniformInt.of(this.config.min.getOrDefault(blockState, 0), this.config.max.getOrDefault(blockState, 0));
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
        public BlockStateMappedProperty<Integer> min = BlockStateMappedProperty.of(0);
        public BlockStateMappedProperty<Integer> max = BlockStateMappedProperty.of(0);
    }
}