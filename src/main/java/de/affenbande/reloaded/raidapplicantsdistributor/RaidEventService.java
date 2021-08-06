package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaidEventService {

    @Autowired private final RaidEventRepository raidEventRepository;

    public void create(@NonNull RaidEvent raidEvent) {
        if (raidEventRepository.findById(raidEvent.getName()).block() == null) {
            raidEventRepository.save(raidEvent).block();
        }
    }

    public Mono<List<RaidEvent>> getAll() {
        return raidEventRepository.findAll().collectList();
    }

    public Mono<RaidEvent> getByName(@NonNull String name) {
        return raidEventRepository.findById(name);
    }
}

