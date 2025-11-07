package de.tomalbrc.filament.entity.skill.target.impl;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.AbstractTargeter;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ForwardTargeter extends AbstractTargeter {
    @SerializedName(value = "forward", alternate = {"f", "amount", "a"})
    float forward = 5;
    @SerializedName(value = "rotate", alternate = {"rot"})
    float rotate = 0;
    @SerializedName(value = "useeyelocation", alternate = {"uel"})
    boolean useeyelocation = false;
    @SerializedName(value = "lockpitch")
    boolean lockpitch = false;

    @Override
    public List<Target> find(SkillTree context) {
        var entity = (Entity) context.caster;
        var vec = entity.getForward().multiply(1., lockpitch ? 0. : 1., 1.).normalize().yRot(Mth.DEG_TO_RAD * rotate);
        return List.of(Target.of(entity.level(), (useeyelocation ? entity.getEyePosition() : entity.position()).add(vec.scale(forward))));
    }
}