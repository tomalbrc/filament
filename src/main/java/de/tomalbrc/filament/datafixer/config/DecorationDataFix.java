package de.tomalbrc.filament.datafixer.config;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Rotating;
import de.tomalbrc.filament.behaviour.block.Waterloggable;
import de.tomalbrc.filament.data.DecorationData;

public class DecorationDataFix {
    public static void fixup(JsonElement element, DecorationData data) {
        BlockDataFix.fixup(element, data);

        var props = element.getAsJsonObject().get("properties");
        if (props == null) {
            data.behaviour().put(Behaviours.WATERLOGGABLE, new Waterloggable.Config());
        }
        else {
            var waterloggable = props.getAsJsonObject().get("waterloggable");
            if (waterloggable == null || waterloggable.getAsBoolean()) data.behaviour().put(Behaviours.WATERLOGGABLE, new Waterloggable.Config());

            var rotate = props.getAsJsonObject().get("rotate");
            var rotateSmooth = props.getAsJsonObject().get("rotateSmooth");
            var rotate_smooth = props.getAsJsonObject().get("rotate_smooth");
            var hasRotBeh = data.behaviour().test(x -> DecorationRotationProvider.class.isAssignableFrom(x.type()));
            if (!hasRotBeh && ((rotate == null && data.properties().placement.wall()) || rotate != null && rotate.getAsBoolean())) {
                var conf = new Rotating.Config();
                conf.smooth = (rotateSmooth != null && rotateSmooth.getAsBoolean()) || (rotate_smooth != null && rotate_smooth.getAsBoolean());
                data.behaviour().put(Behaviours.ROTATING, conf);
            }
        }
    }
}
