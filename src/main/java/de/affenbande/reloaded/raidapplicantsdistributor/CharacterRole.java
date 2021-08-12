package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum CharacterRole {
    TANK(List.of("tank", "protection"), List.of("Wilder Kampf (Tank)", "Schutz")),
    HEAL(List.of("restoration", "discipline", "holy"), List.of("Wiederherstellung", "Disziplin", "Heilig")),
    RANGED(List.of("balance", "beast_mastery", "marksmanship", "survival", "arcane", "fire", "frost", "shadow",
                   "elemental", "affliction", "demonology", "destruction"
    ), List.of("Gleichgewicht", "Tierherrschaft", "Treffsicherheit", "Überleben", "Arkan", "Feuer", "Frost", "Schatten",
               "Elementar", "Gebrechen", "Dämonologie", "Zerstörung"
    )),
    MELEE(List.of("feral", "retribution", "assassination", "combat", "subtlety", "enhancement", "arms", "fury"),
          List.of("Wilder Kampf (Katze)", "Vergeltung", "Meucheln", "Kampf", "Täuschung", "Verstärkung", "Waffen",
                  "Furor"
          )
    );

    List<String> associatedSpecs;
    List<String> associatedSpecNames;

    public static CharacterRole getBySpec(final String spec) {
        if (TANK.associatedSpecs.contains(spec)) {
            return TANK;
        } else if (HEAL.associatedSpecs.contains(spec)) {
            return HEAL;
        } else if (RANGED.associatedSpecs.contains(spec)) {
            return RANGED;
        } else {
            return MELEE.associatedSpecs.contains(spec) ? MELEE : null;
        }
    }

    public static CharacterRole getBySpecName(final String specName) {
        if (TANK.associatedSpecNames.contains(specName)) {
            return TANK;
        } else if (HEAL.associatedSpecNames.contains(specName)) {
            return HEAL;
        } else if (RANGED.associatedSpecNames.contains(specName)) {
            return RANGED;
        } else {
            return MELEE.associatedSpecNames.contains(specName) ? MELEE : null;
        }
    }

    public Integer getValue() {
        return ordinal();
    }
}
