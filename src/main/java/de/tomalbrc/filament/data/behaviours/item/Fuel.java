package de.tomalbrc.filament.data.behaviours.item;

import de.tomalbrc.filament.behaviour.Behaviour;
import de.tomalbrc.filament.behaviour.item.ItemBehaviour;

/**
 * Item behaviours
 */
public class Fuel implements ItemBehaviour {
    /**
     * The value associated with the fuel, used in furnaces and similar item burning blocks
     */
    public int value = 10;
}