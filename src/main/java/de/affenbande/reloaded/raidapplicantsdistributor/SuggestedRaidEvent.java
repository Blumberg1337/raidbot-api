package de.affenbande.reloaded.raidapplicantsdistributor;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "suggestedRaids")
public class SuggestedRaidEvent {

    @DocumentId String name;
    int raidSize;
    String raidDestination;
    List<CharacterData> participants;
    CharacterData raidLead;
    String raidingDay;
    String additionalRaidingDay;
    int raidValueIdentifier;

    public static class Config {
        public static final int TANK_MINIMUM_THRESHOLD_DEFAULT = 1;
        public static final int TANK_MAXIMUM_THRESHOLD_DEFAULT = 2;
        public static final int OFFTANK_MINIMUM_THRESHOLD_DEFAULT = 0;
        public static final int OFFTANK_MAXIMUM_THRESHOLD_DEFAULT = 1;
        public static final int MELEE_MINIMUM_THRESHOLD_DEFAULT = 1;
        public static final int MELEE_MAXIMUM_THRESHOLD_DEFAULT = 3;
        public static final int MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT = 2;
        public static final int MELEE_MAXIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT = 4;
        public static final int RANGED_MINIMUM_THRESHOLD_DEFAULT = 3;
        public static final int RANGED_MAXIMUM_THRESHOLD_DEFAULT = 5;
        public static final int RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT = 2;
        public static final int RANGED_MAXIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT = 4;
        public static final int HEAL_MINIMUM_THRESHOLD_DEFAULT = 2;
        public static final int HEAL_MAXIMUM_THRESHOLD_DEFAULT = 3;
    }
}
