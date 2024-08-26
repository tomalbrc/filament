package de.tomalbrc.filament.data.behaviours.decoration;

import de.tomalbrc.filament.behaviour.decoration.DecorationBehaviour;
import org.joml.Vector3f;

/**
 * Seat behaviours for decoration
 */
public class Seat implements DecorationBehaviour {
    /**
     * The player seating offset
     */
    public Vector3f offset = new Vector3f();

    /**
     * The rotation direction of the seat
     */
    public float direction = 0;
}
