package de.tomalbrc.filament.data.behaviours.decoration;

import de.tomalbrc.filament.behaviour.decoration.DecorationBehaviour;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;

/**
 * Seat behaviours for decoration
 */
public class Seat extends ObjectArrayList<Seat.SeatMeta> implements DecorationBehaviour {
    public static class SeatMeta {
        /**
         * The player seating offset
         */
        public Vector3f offset = new Vector3f();

        /**
         * The rotation direction of the seat
         */
        public float direction = 0;
    }
}
