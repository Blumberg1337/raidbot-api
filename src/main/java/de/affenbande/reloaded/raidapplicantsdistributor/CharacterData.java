package de.affenbande.reloaded.raidapplicantsdistributor;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.Comparator;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "characters")
public class CharacterData {

    @DocumentId private String name;
    String characterClass;
    String spec;
    boolean offTank;
    boolean raidLead;
    boolean speedRunner;
    List<String> favoredItems;
    List<String> possibleDaysToRaid;
    String role;
    int benchCount;

    public static class CharacterDataComparator implements Comparator<CharacterData> {

        public int compare(@NonNull final CharacterData character1, @NonNull final CharacterData character2) {
            int roleCompare = CharacterRole
                .valueOf(character1.getRole())
                .getValue()
                .compareTo(CharacterRole.valueOf(character2.getRole()).getValue());
            int classCompare = character1.getCharacterClass().compareTo(character2.getCharacterClass());
            int specCompare = character1.getSpec().compareTo(character2.getSpec());
            int nameCompare = character1.getName().compareTo(character2.getName());
            return roleCompare == 0 ? (classCompare == 0
                ? (specCompare == 0 ? nameCompare : specCompare)
                : classCompare) : roleCompare;
        }
    }
}
