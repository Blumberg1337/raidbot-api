package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestedRaidEventService {

    private static final Logger LOG = Logger.getLogger(SuggestedRaidEventService.class.getName());

    @Autowired private final SuggestedRaidEventRepository suggestedRaidEventRepository;
    private final CharacterDataService characterDataService;

    public List<SuggestedRaidEvent> create(final String raidName) throws GeneralSecurityException, IOException {
        final List<CharacterData> characters = characterDataService.getAll();

        if (characters != null && !characters.isEmpty()) {
            final Map<String, Set<CharacterData>> possibleRaids = getCharactersByAvailability(characters);
            final Map<String, Set<SuggestedRaidEvent>> suggestedRaidEventsByDays = new HashMap<>();
            final Map<String, Set<CharacterData>> benches = new HashMap<>();
            final Set<CharacterData> raidLeads = new HashSet<>();

            for (final Entry<String, Set<CharacterData>> possibleRaid : possibleRaids.entrySet()) {
                final Set<CharacterData> bench = new HashSet<>();
                final Set<CharacterData> applicants = possibleRaid.getValue();

                while (applicants.size() > 0) {
                    final SuggestedRaidEvent suggestedRaidEvent = generateSuggestedRaidEvent(
                        possibleRaid.getKey(), applicants);
                    final Set<CharacterData> participants = new HashSet<>(suggestedRaidEvent.getParticipants());

                    if (suggestedRaidEvent.getRaidLead() == null) {
                        bench.addAll(participants
                                         .stream()
                                         .filter((participant) -> !bench.contains(participant))
                                         .collect(Collectors.toSet()));
                    } else {
                        if (!suggestedRaidEventsByDays.containsKey(possibleRaid.getKey())) {
                            suggestedRaidEventsByDays.put(possibleRaid.getKey(), new HashSet<>());
                        }
                        suggestedRaidEventsByDays.get(possibleRaid.getKey()).add(suggestedRaidEvent);

                        LOG.info("before benches loop");
//                        benches.forEach((key, value) -> {
//                            final Set<CharacterData> benchedPlayers = value
//                                .stream()
//                                .filter(benchedPlayer -> suggestedRaidEvent.getParticipants().contains(benchedPlayer))
//                                .collect(Collectors.toSet());
//                            if (benchedPlayers.size() > 0) {
//                                benchedPlayers.forEach(benchedPlayer -> benches.get(key).remove(benchedPlayer));
//                            }
//                        });
                        LOG.info("after benches loop");
                    }

                    raidLeads.addAll(
                        participants.stream().filter(CharacterData::isRaidLead).collect(Collectors.toSet()));

                    applicants
                        .stream()
                        .filter(participants::contains)
                        .collect(Collectors.toSet())
                        .forEach(applicants::remove);
                    LOG.info("end of while applicants > 0 loop");
                }

                LOG.info("after while applicants > 0 loop");
                benches.put(possibleRaid.getKey(), bench);
            }
            LOG.info("after possible raids loop");

            for (final Map.Entry<String, Set<SuggestedRaidEvent>> suggestedRaidEventsByDay :
                suggestedRaidEventsByDays.entrySet()) {
                final Set<SuggestedRaidEvent> suggestedRaidEvents = suggestedRaidEventsByDay.getValue();
                LOG.info("before saving suggested raids");
                suggestedRaidEvents.forEach(
                    suggestedRaidEvent -> suggestedRaidEventRepository.save(suggestedRaidEvent).block());
                LOG.info("after saving suggested raids");
                LOG.info("before gsheets");
                final GoogleSheet googleSheet = new GoogleSheet();
                googleSheet.createRaidSheetForPossibleRaidingDay(suggestedRaidEventsByDay.getKey(), suggestedRaidEvents,
                                                                 benches.get(suggestedRaidEventsByDay.getKey())
                );
            }

            return suggestedRaidEventsByDays.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<SuggestedRaidEvent> getAll() {
        return suggestedRaidEventRepository.findAll().collectList().block();
    }

    public SuggestedRaidEvent getByName(@NonNull final String name) {
        return suggestedRaidEventRepository.findById(name).block();
    }

    public Map<String, Set<CharacterData>> getCharactersByAvailability(final List<CharacterData> characters) {

        final Map<String, Set<CharacterData>> charactersByAvailability = new HashMap<>(7);

        for (final CharacterData character : characters) {
            for (final String possibleDayToRaid : character.getPossibleDaysToRaid()) {
                if (charactersByAvailability.get(possibleDayToRaid) == null) {
                    final Set<CharacterData> availableCharacters = new HashSet<>();
                    availableCharacters.add(character);
                    charactersByAvailability.put(possibleDayToRaid, availableCharacters);
                } else {
                    charactersByAvailability.get(possibleDayToRaid).add(character);
                }
            }
        }

        return charactersByAvailability
            .entrySet()
            .stream()
            .filter(weekDay -> weekDay.getValue().size() >= 10)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public SuggestedRaidEvent generateSuggestedRaidEvent(final String raidingDay, final Set<CharacterData> applicants) {

        final LocalDateTime begin = LocalDateTime.now();
        final List<SuggestedRaidEvent> suggestedRaidEvents = getAll();
        final int suggestedRaidEventsCount = suggestedRaidEvents != null ? suggestedRaidEvents.size() : 0;
        final String karazhanStr = Raid.KARAZHAN.toString();
        final String suggestedRaidId = suggestedRaidEventsCount + "-" + karazhanStr + "-" + LocalDateTime.now();

        CharacterData raidLead = null;
        final Set<CharacterData> raidMembers = new HashSet<>();
        for (final CharacterData applicant : applicants) {
            final Set<CharacterData> melees = raidMembers
                .stream()
                .filter(raidMember -> raidMember.getRole().equals(CharacterRole.MELEE.toString()))
                .collect(Collectors.toSet());
            final Set<CharacterData> ranges = raidMembers
                .stream()
                .filter(raidMember -> raidMember.getRole().equals(CharacterRole.RANGED.toString()))
                .collect(Collectors.toSet());
            final Set<CharacterData> heals = raidMembers
                .stream()
                .filter(raidMember -> raidMember.getRole().equals(CharacterRole.HEAL.toString()))
                .collect(Collectors.toSet());
            final Set<CharacterData> tanks = raidMembers
                .stream()
                .filter(raidMember -> raidMember.getRole().equals(CharacterRole.TANK.toString()))
                .collect(Collectors.toSet());
            final Set<CharacterData> offTanks = melees
                .stream()
                .filter(CharacterData::isOffTank)
                .collect(Collectors.toSet());

            final Optional<CharacterData> anyMelee = melees.stream().findAny();
            final Optional<CharacterData> anyRanged = ranges.stream().findAny();
            final Optional<CharacterData> anyHeal = heals.stream().findAny();
            final Optional<CharacterData> anyTank = tanks.stream().findAny();
            final Optional<CharacterData> anyNonOffTank = melees
                .stream()
                .filter(raidMember -> !raidMember.isOffTank())
                .findAny();

            if (raidMembers.size() < Raid.KARAZHAN.getNumberOfPlayers()) {
                if (applicant.getRole().equals(CharacterRole.HEAL.toString()) &&
                    heals.size() < SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT) {
                    raidMembers.add(applicant);
                } else if (applicant.getRole().equals(CharacterRole.TANK.toString()) &&
                    tanks.size() < SuggestedRaidEvent.Config.TANK_MAXIMUM_THRESHOLD_DEFAULT) {
                    raidMembers.add(applicant);
                } else if (applicant.getRole().equals(CharacterRole.MELEE.toString())) {
                    if (offTanks.size() >= SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT &&
                        melees.size() <
                            SuggestedRaidEvent.Config.MELEE_MAXIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT ||
                        melees.size() < SuggestedRaidEvent.Config.MELEE_MAXIMUM_THRESHOLD_DEFAULT) {
                        raidMembers.add(applicant);
                    }
                } else if (applicant.getRole().equals(CharacterRole.RANGED.toString())) {
                    if (heals.size() == SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT &&
                        ranges.size() < SuggestedRaidEvent.Config.RANGED_MAXIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT ||
                        ranges.size() < SuggestedRaidEvent.Config.RANGED_MAXIMUM_THRESHOLD_DEFAULT) {
                        raidMembers.add(applicant);
                    }
                }
            } else {

                if (applicant.getRole().equals(CharacterRole.TANK.toString()) &&
                    tanks.size() < SuggestedRaidEvent.Config.TANK_MAXIMUM_THRESHOLD_DEFAULT) {
                    if (offTanks.size() <= SuggestedRaidEvent.Config.OFFTANK_MINIMUM_THRESHOLD_DEFAULT &&
                        melees.size() >
                            SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT) {
                        raidMembers.remove(anyNonOffTank.orElse(anyMelee.orElseThrow()));
                    } else if (melees.size() > SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_DEFAULT) {
                        raidMembers.remove(anyMelee.orElseThrow());
                    } else if (heals.size() <= SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT &&
                        ranges.size() > SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT ||
                        ranges.size() > SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_DEFAULT) {
                        raidMembers.remove(anyRanged.orElseThrow());
                    }
                    raidMembers.add(applicant);
                }

                if (applicant.getRole().equals(CharacterRole.HEAL.toString()) &&
                    heals.size() < SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT) {
                    if (offTanks.size() <= SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT &&
                        melees.size() >
                            SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT) {
                        raidMembers.remove(anyNonOffTank.orElse(anyMelee.orElseThrow()));
                    } else if (melees.size() > SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_DEFAULT) {
                        raidMembers.remove(anyMelee.orElseThrow());
                    } else if (ranges.size() >
                        SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT) {
                        raidMembers.remove(anyRanged.orElseThrow());
                    }
                    raidMembers.add(applicant);
                }

                if (applicant.isOffTank() &&
                    offTanks.size() < SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT &&
                    applicant.getRole().equals(CharacterRole.MELEE.toString()) &&
                    tanks.size() < SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT) {
                    raidMembers.remove(anyNonOffTank.orElse(anyRanged.orElseThrow()));
                    raidMembers.add(applicant);
                }

                if (raidMembers.stream().anyMatch(CharacterData::isRaidLead) && raidLead == null) {
                    raidLead = raidMembers.stream().filter(CharacterData::isRaidLead).findAny().orElseThrow();
                }

                if (applicant.isRaidLead() && raidLead == null) {
                    if (applicant.getRole().equals(CharacterRole.MELEE.toString())) {
                        if (melees.size() > SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_DEFAULT) {
                            raidMembers.remove(anyMelee.orElseThrow());
                        } else if (heals.size() == SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT &&
                            ranges.size() >
                                SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT ||
                            ranges.size() > SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_DEFAULT) {
                            raidMembers.remove(anyRanged.orElseThrow());
                        }
                    } else if (!applicant.getRole().equals(CharacterRole.RANGED.toString())) {
                        if (applicant.getRole().equals(CharacterRole.HEAL.toString())) {
                            raidMembers.remove(anyHeal.orElseThrow());
                        } else if (applicant.getRole().equals(CharacterRole.TANK.toString())) {
                            raidMembers.remove(anyTank.orElseThrow());
                        }
                    } else if ((heals.size() != SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT ||
                        ranges.size() <=
                            SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT) &&
                        ranges.size() <= SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_DEFAULT) {
                        if (offTanks.size() == SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT &&
                            melees.size() >
                                SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT) {
                            raidMembers.remove(anyNonOffTank.orElseThrow());
                        } else if (melees.size() > SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_DEFAULT) {
                            raidMembers.remove(anyNonOffTank.orElse(anyMelee.orElseThrow()));
                        }
                    } else {
                        raidMembers.remove(anyRanged.orElseThrow());
                    }

                    raidMembers.add(applicant);
                    raidLead = applicant;
                }

                boolean sufficientTankCount =
                    tanks.size() >= SuggestedRaidEvent.Config.TANK_MINIMUM_THRESHOLD_DEFAULT &&
                        tanks.size() + offTanks.size() >= SuggestedRaidEvent.Config.TANK_MAXIMUM_THRESHOLD_DEFAULT;
                boolean sufficientHealCount = heals.size() >= SuggestedRaidEvent.Config.HEAL_MINIMUM_THRESHOLD_DEFAULT;
                boolean sufficientRangedCount =
                    heals.size() == SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT &&
                        ranges.size() <= SuggestedRaidEvent.Config.RANGED_MAXIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT &&
                        ranges.size() >= SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_WITH_HEAL_MAXIMUM_DEFAULT ||
                        heals.size() >= SuggestedRaidEvent.Config.HEAL_MINIMUM_THRESHOLD_DEFAULT &&
                            heals.size() < SuggestedRaidEvent.Config.HEAL_MAXIMUM_THRESHOLD_DEFAULT &&
                            ranges.size() >= SuggestedRaidEvent.Config.RANGED_MINIMUM_THRESHOLD_DEFAULT &&
                            ranges.size() <= SuggestedRaidEvent.Config.RANGED_MAXIMUM_THRESHOLD_DEFAULT;
                boolean sufficientMeleeCount =
                    offTanks.size() == SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT && melees.size() <=
                        SuggestedRaidEvent.Config.MELEE_MAXIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT &&
                        melees.size() >=
                            SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_WITH_OFFTANK_MAXIMUM_DEFAULT ||
                        offTanks.size() >= SuggestedRaidEvent.Config.OFFTANK_MINIMUM_THRESHOLD_DEFAULT &&
                            offTanks.size() < SuggestedRaidEvent.Config.OFFTANK_MAXIMUM_THRESHOLD_DEFAULT &&
                            melees.size() >= SuggestedRaidEvent.Config.MELEE_MINIMUM_THRESHOLD_DEFAULT &&
                            melees.size() <= SuggestedRaidEvent.Config.MELEE_MAXIMUM_THRESHOLD_DEFAULT;

                if (raidMembers.size() == Raid.KARAZHAN.getNumberOfPlayers() && raidLead != null &&
                    sufficientTankCount && sufficientHealCount && sufficientRangedCount && sufficientMeleeCount) {
                    final LocalDateTime end = LocalDateTime.now();
                    final long duration = Duration.between(begin, end).getNano();
                    LOG.info("duration in ms: " + duration / 1000000L);
                    return SuggestedRaidEvent
                        .builder()
                        .name(suggestedRaidId)
                        .raidDestination(karazhanStr)
                        .raidSize(Raid.KARAZHAN.getNumberOfPlayers())
                        .participants(new ArrayList<>(raidMembers))
                        .raidLead(raidLead)
                        .raidingDay(raidingDay)
                        .additionalRaidingDay(null)
                        .raidValueIdentifier(100)
                        .build();
                }
            }
        }
        return SuggestedRaidEvent
            .builder()
            .name("Bench#" + suggestedRaidId)
            .raidDestination(karazhanStr)
            .raidSize(Raid.KARAZHAN.getNumberOfPlayers())
            .participants(new ArrayList<>(raidMembers))
            .raidLead(null)
            .raidingDay(raidingDay)
            .additionalRaidingDay(null)
            .raidValueIdentifier(100)
            .build();
    }

    public SuggestedRaidEvent generateSuggestedRaidEvent(final String raidName) {
        final List<CharacterData> characters = characterDataService.getAll();
        final String karazhanStr = Raid.KARAZHAN.toString().toLowerCase();

        return SuggestedRaidEvent
            .builder()
            .name(UUID.randomUUID().toString())
            .raidDestination(karazhanStr)
            .raidSize(Raid.KARAZHAN.getNumberOfPlayers())
            .participants(new ArrayList<>(characters))
            .raidLead(null)
            .raidingDay(LocalDateTime.now().toString())
            .additionalRaidingDay(null)
            .raidValueIdentifier(100)
            .build();
    }
}
