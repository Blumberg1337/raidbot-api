package de.affenbande.reloaded.raidapplicantsdistributor;

import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterDataRepository extends FirestoreReactiveRepository<CharacterData> {

}
