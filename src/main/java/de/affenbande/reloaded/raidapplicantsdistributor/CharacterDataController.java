package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/characters")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CharacterDataController {

    CharacterDataService characterDataService;

    @GetMapping(produces = "application/json")
    public List<CharacterData> getAll() {
        return characterDataService.getAll();
    }

    @GetMapping(path = "/{name}", produces = "application/json")
    public CharacterData getByName(@PathVariable @NonNull String name) {
        return characterDataService.getByName(name);
    }

    @PostMapping(path = {"/create"})
    public void create(@RequestBody CharacterData characterData) {
        characterDataService.create(characterData);
    }

    @GetMapping(path = "/test")
    public String test() {
        Map<String, String> env = System.getenv();
        env.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("Success");
        return "Success";
    }

    @GetMapping(path = "/deleteAll")
    public void deleteAll() {
        characterDataService.deleteAll();
    }
}
