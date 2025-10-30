package de.tomalbrc.filament.entity.skill.target;

public class TargeterFactory {
    public static Targeter create(TargetData data) {
        if (data == null) return new SelfTargeter();

        return switch (data.getType().toLowerCase()) {
            case "self" -> new SelfTargeter();
            case "players_in_radius" -> new PlayersInRadiusTargeter(data.getRadius());
            default -> throw new IllegalArgumentException("Unknown targeter: " + data.getType());
        };
    }
}