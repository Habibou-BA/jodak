package com.jodak.enums;

/**
 * Médaille attribuée selon le rang. Le barème de points sert au tableau de bord.
 */
public enum Medal {

    OR(7),
    ARGENT(4),
    BRONZE(1);

    private final int points;

    Medal(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    /**
     * Médaille correspondant à un rang (1 → OR, 2 → ARGENT, 3 → BRONZE), ou {@code null} au-delà.
     * Règle RM-12.
     */
    public static Medal fromRank(int rank) {
        return switch (rank) {
            case 1 -> OR;
            case 2 -> ARGENT;
            case 3 -> BRONZE;
            default -> null;
        };
    }
}
