package de.tomalbrc.filament.util;

@SuppressWarnings("unused")
public enum HideFlags {
    HideEnchantments(1),
    HideAttributeModifiers(1 << 1),
    HideUnbreakable(1 << 2),
    HideCanDestroy(1 << 3),
    HideCanPlaceOn(1 << 4),
    HideOthers(1 << 5),
    HideDyed(1 << 6),
    HideUpgrade(1 << 7);

    private final int value;

    HideFlags(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int combineFlags(HideFlags... flags) {
        int combinedValue = 0;
        for (HideFlags flag : flags) {
            combinedValue |= flag.getValue();
        }
        return combinedValue;
    }
}