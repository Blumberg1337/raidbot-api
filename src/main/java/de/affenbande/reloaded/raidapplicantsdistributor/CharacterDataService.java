package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharacterDataService {

    @Autowired private final CharacterDataRepository characterDataRepository;

    public void create(@NonNull final CharacterData characterData) {
        if (characterDataRepository.findById(characterData.getName()).block() == null) {
            // As WoW character names are unique per WoW-server they are a perfect fit for a db id, but need to be
            // referenced in the db entry as well
            final String id = characterData.getName();
            CharacterData character = CharacterData
                .builder()
                .id(id)
                .name(characterData.getName())
                .characterClass(characterData.getCharacterClass())
                .spec(characterData.getSpec())
                .offTank(characterData.isOffTank())
                .raidLead(characterData.isRaidLead())
                .speedRunner(characterData.isSpeedRunner())
                .favoredItems(characterData.getFavoredItems())
                .possibleDaysToRaid(characterData
                                        .getPossibleDaysToRaid()
                                        .stream()
                                        .map(DateUtil::convertPossibleDayToRaid)
                                        .collect(Collectors.toList()))
                .role(Objects.requireNonNull(CharacterRole.getBySpec(characterData.getSpec())).toString())
                .build();
            characterDataRepository.save(character).block();
        }
    }

    public List<CharacterData> getAll() {
        return characterDataRepository.findAll().collectList().block();
    }

    public CharacterData getById(@NonNull final String id) {
        return characterDataRepository.findById(id).block();
    }

    public void update(@NonNull final CharacterData character) {
        if (getById(character.getName()) != null) {
            characterDataRepository.save(character).block();
        }
    }

    public void bench(@NonNull final CharacterData character) {
        CharacterData benchCount = CharacterData
            .builder()
            .id(character.getId())
            .name(character.getName())
            .characterClass(character.getCharacterClass())
            .spec(character.getSpec())
            .offTank(character.isOffTank())
            .raidLead(character.isRaidLead())
            .speedRunner(character.isSpeedRunner())
            .favoredItems(character.getFavoredItems())
            .possibleDaysToRaid(character.getPossibleDaysToRaid())
            .role(Objects.requireNonNull(CharacterRole.getBySpec(character.getSpec())).toString())
            .benchCount(character.getBenchCount() + 1)
            .build();
        update(benchCount);
    }

    public void deleteAll() {
        characterDataRepository.deleteAll().block();
    }
}
