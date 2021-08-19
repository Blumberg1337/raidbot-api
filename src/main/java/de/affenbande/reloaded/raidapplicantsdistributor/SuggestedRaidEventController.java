package de.affenbande.reloaded.raidapplicantsdistributor;

import de.affenbande.reloaded.raidapplicantsdistributor.satsolver.SuggestedRaidEventServiceSat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/suggestedRaids")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SuggestedRaidEventController {

    SuggestedRaidEventService suggestedRaidEventService;
    SuggestedRaidEventServiceSat suggestedRaidEventServiceSat;

    @PostMapping(path = "/create")
    public List<SuggestedRaidEvent> create() throws GeneralSecurityException, IOException {
        return suggestedRaidEventService.create("a");
    }

    @PostMapping(path = "/create/sat")
    public String createSat() throws GeneralSecurityException, IOException {
        return suggestedRaidEventServiceSat.create("Test raid computed by MiniSAT");
    }
}
