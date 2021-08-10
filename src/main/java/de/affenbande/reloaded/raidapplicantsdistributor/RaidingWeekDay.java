package de.affenbande.reloaded.raidapplicantsdistributor;

public enum RaidingWeekDay {
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
    MONDAY,
    TUESDAY;

    public Integer getValue() {
        return ordinal();
    }
}
