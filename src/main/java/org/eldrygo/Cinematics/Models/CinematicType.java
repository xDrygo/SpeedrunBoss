package org.eldrygo.Cinematics.Models;

public enum CinematicType {
    START("start"),
    WINNER("winner");

    private final String id;

    CinematicType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static CinematicType fromName(String name) {
        for (CinematicType type : values()) {
            if (type.id.equalsIgnoreCase(name)) return type;
        }
        return null;
    }
}