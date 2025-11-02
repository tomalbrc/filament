package de.tomalbrc.filament.entity.skill.mechanic.impl;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;

import java.util.List;

public class FeedMechanic implements Mechanic {
    @SerializedName(value = "amount", alternate = {"a"})
    int amount;
    @SerializedName(value = "saturation", alternate = {"s"})
    float saturation;
    @SerializedName(value = "overfeed", alternate = {"o", "of"})
    boolean overfeed;

    public FeedMechanic(int amount, float saturation, boolean overfeed) {
        this.amount = amount;
        this.saturation = saturation;
        this.overfeed = overfeed;
    }

    @Override
    public int execute(SkillTree tree) {
        List<Target> targets = tree.getCurrentTargets();
        if (targets != null) {
            for (Target target : targets) {
                if (target.getEntity() instanceof ServerPlayer player) {
                    player.getFoodData().eat(new FoodProperties(amount, saturation, overfeed));
                }
            }
        }

        return 0;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.FEED;
    }
}