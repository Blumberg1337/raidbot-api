package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharacterDataService {

    @Autowired private final CharacterDataRepository characterDataRepository;

    public void create(@NonNull final CharacterData characterData) {
        if (characterDataRepository.findById(characterData.getName()).block() == null) {
            CharacterData character = CharacterData
                .builder()
                .id(characterData.getName())
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
                .role(Objects.requireNonNull(
                    CharacterRole.getBySpec(characterData.getSpec())).toString())
                .build();
            characterDataRepository.save(character).block();
        }
    }

    public List<CharacterData> getAll() {
        return characterDataRepository.findAll().collectList().block();
    }

    public CharacterData getByName(@NonNull final String name) {
        return characterDataRepository.findById(name).block();
    }

    public void update(@NonNull final CharacterData character) {
        if (getByName(character.getName()) != null) {
            characterDataRepository.save(character).block();
        }
    }

    public void bench(@NonNull final CharacterData character) {
        CharacterData benchCount = CharacterData
            .builder()
            .id(character.getName())
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

    public List<CharacterData> addNamesToCharacters() {
        List<CharacterData> characters = getAll();
        List<CharacterData> updatedCharacters = new ArrayList<>();
        for (final CharacterData character : characters) {
            CharacterData characterWithName = CharacterData
                .builder()
                .id(character.getName())
                .name(character.getName())
                .characterClass(character.getCharacterClass())
                .spec(character.getSpec())
                .offTank(character.isOffTank())
                .raidLead(character.isRaidLead())
                .speedRunner(character.isSpeedRunner())
                .favoredItems(character.getFavoredItems())
                .possibleDaysToRaid(character.getPossibleDaysToRaid())
                .role(Objects.requireNonNull(CharacterRole.getBySpec(character.getSpec())).toString())
                .benchCount(character.getBenchCount())
                .build();
            update(characterWithName);
            updatedCharacters.add(character);
        }
        return updatedCharacters;
    }

    public void deleteAll() {
        characterDataRepository.deleteAll().block();
    }
}
