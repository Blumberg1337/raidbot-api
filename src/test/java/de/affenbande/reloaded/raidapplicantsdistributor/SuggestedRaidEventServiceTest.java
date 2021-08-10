package de.affenbande.reloaded.raidapplicantsdistributor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.when;

class SuggestedRaidEventServiceTest {
    @InjectMocks SuggestedRaidEventService suggestedRaidEventService;
    @Mock CharacterDataService characterDataService;
    @Mock SuggestedRaidEventRepository suggestedRaidEventRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll() {
    }

    @Test
    void getByName() {
    }

    @Test
    void testGenerateSuggestedRaidEvent() {
//        when(suggestedRaidEventRepository.findAll()).thenReturn(Flux.empty());
//        when(characterDataService.getAll()).thenReturn(new LinkedList<>(createCharacterDataSet(100)));
//        final List<CharacterData> characters = new LinkedList<>(createCharacterDataSet(100));
//        final Map<String, Set<CharacterData>> possibleRaids
//            = suggestedRaidEventService.getCharactersByAvailability(characters);
//        final Map.Entry<String, Set<CharacterData>> possibleRaid = possibleRaids
//            .entrySet()
//            .stream()
//            .findAny()
//            .orElse(null);
//        final SuggestedRaidEvent result = possibleRaid != null
//            ? suggestedRaidEventService.generateSuggestedRaidEvents(possibleRaid.getKey(), possibleRaid.getValue())
//            : null;
//        Assert.notNull(result, "result was: " + result);
    }

    private Set<CharacterData> createCharacterDataSet(final int limit) {
        Set<CharacterData> characterDataSet = new HashSet<>();

        for (int i = 0; i < limit; ++i) {
            final List<String> daysByCharacter = new LinkedList<>();
            final List<String> days = new ArrayList<>(
                List.of("Montag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"));
            final int daysCount = ThreadLocalRandom.current().nextInt(0, days.size());

            for (int j = 0; j < daysCount; ++j) {
                final int day = ThreadLocalRandom.current().nextInt(0, days.size());
                daysByCharacter.add(days.get(day));
                days.remove(day);
            }

            CharacterData characterData = CharacterData
                .builder()
                .name("Test_" + i)
                .characterClass("CharacterClass_" + i)
                .spec("Spec_" + i)
                .offTank(i % 5 == 0)
                .raidLead(i % 10 == 0)
                .speedRunner(i % 2 == 0)
                .favoredItems(null)
                .possibleDaysToRaid(daysByCharacter)
                .role(CharacterRole.values()[ThreadLocalRandom
                    .current()
                    .nextInt(0, CharacterRole.values().length)].toString())
                .build();
            characterDataSet.add(characterData);
        }

        return characterDataSet;
    }
}
