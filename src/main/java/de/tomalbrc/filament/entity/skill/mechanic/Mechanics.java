package de.tomalbrc.filament.entity.skill.mechanic;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import de.tomalbrc.filament.util.Util;
import net.minecraft.resources.ResourceLocation;

public class Mechanics {
    public static RuntimeTypeAdapterFactory<Mechanic> TYPE_ADAPTER_FACTORY = RuntimeTypeAdapterFactory.of(Mechanic.class, "type");

    public static ResourceLocation register(ResourceLocation id, Class<? extends Mechanic> type) {
        TYPE_ADAPTER_FACTORY.registerSubtype(type, id.toString());
        return id;
    }

    public static ResourceLocation DELAY = register(Util.id("delay"), DelayMechanic.class);
    public static ResourceLocation MESSAGE = register(Util.id("message"), MessageMechanic.class);
    public static ResourceLocation POTION = register(Util.id("potion"), PotionMechanic.class);
    public static ResourceLocation SKILL = register(Util.id("skill"), RunMetaSkillMechanic.class);
    public static ResourceLocation SET_VARIABLE = register(Util.id("set_variable"), SetVariableMechanic.class);
    public static ResourceLocation SOUND = register(Util.id("sound"), SoundMechanic.class);
}
