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
@Document(collectionName = "raids")
public class RaidEvent {

    @DocumentId String name;
    int raidSize;
    String raidDestination;
    List<CharacterData> characters;
    String raidLead;
    String raidingDay;
    String additionalRaidingDay;
    int raidValueIdentifier;
}
