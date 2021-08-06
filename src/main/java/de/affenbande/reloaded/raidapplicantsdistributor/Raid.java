package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum Raid {
    KARAZHAN(10),
    GRUULS_LAIR(25),
    MAGTHERIDONS_LAIR(25);

    int numberOfPlayers;
}
