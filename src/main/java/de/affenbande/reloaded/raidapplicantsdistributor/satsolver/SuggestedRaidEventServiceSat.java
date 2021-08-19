package de.affenbande.reloaded.raidapplicantsdistributor.satsolver;

import de.affenbande.reloaded.raidapplicantsdistributor.CharacterData;
import de.affenbande.reloaded.raidapplicantsdistributor.CharacterDataService;
import de.affenbande.reloaded.raidapplicantsdistributor.SuggestedRaidEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestedRaidEventServiceSat {

    public static final String BENCH = "BENCH";
    private static final Logger LOG = Logger.getLogger(SuggestedRaidEventServiceSat.class.getName());

    @Autowired
    private final SuggestedRaidEventRepository suggestedRaidEventRepository;
    private final CharacterDataService characterDataService;

    public String create(final String raidName) throws GeneralSecurityException, IOException {
        // get character data
        final var characters = characterDataService.getAll();

        // initialize MiniSAT
        Process miniSat = Runtime.getRuntime().exec("minisat -verb=0 /dev/stdin /dev/stdout");
        BufferedReader stdout = new BufferedReader(new InputStreamReader(miniSat.getInputStream()));
        Writer stdin = new OutputStreamWriter(miniSat.getOutputStream(), "UTF-8");

        // write CNF to MiniSAT stdin
        var formula = transformProblem(characters);
        stdin.write(formula.generateCNF());
        stdin.close();

        // read MiniSAT stdout - first line is either SAT or UNSAT
        final boolean satisfiable = stdout.readLine().equals("SAT");
        if(!satisfiable) {
            return "Sorry, no raid could be created";
        }
        // when SAT, the second line holds the variable assignments
        return interpretSolution(formula, stdout.readLine());
    }

    private Expression transformProblem(List<CharacterData> characters) {
        // create propositional logic variables for each character for each raiding day
        Map<CharacterData, Map<DayOfWeek, Variable>> vars = new HashMap<>(characters.size());
        for (var chr : characters) {
            HashMap<DayOfWeek, Variable> days = new HashMap<>();
            vars.put(chr, days);
            for (var day : DayOfWeek.values()) {
                days.put(day, new Variable(chr.getName(), day));
            }
        }

        // create empty expression that gets filled over time
        Expression ex = new Expression();

        for (var chr : characters) {
            var raidDays = chr.getPossibleDaysToRaid().stream().map(DayOfWeek::valueOf).collect(Collectors.toList());

            // add constraint: character cannot raid on the days that the user didn't specify
            for (var day : Arrays.stream(DayOfWeek.values()).filter(day -> !raidDays.contains(day)).collect(Collectors.toList())) {
                Clause unit = new Clause();
                unit.addLiteral(vars.get(chr).get(day), true);
                ex.addClause(unit);
            }

            // add constraint: character can only raid on at most one of the days that the user specified
            // if a character raids on day X, it implies that this character doesn't raid on any other day Y, Z, …
            // formally: X → -Y, X → -Z, …
            // in propositional logic: (-X + -Y) (-X + -Z) …
            for (int dayIdx = 0; dayIdx < raidDays.size() - 1; ++dayIdx) {
                for (int otherDayIdx = dayIdx + 1; otherDayIdx < raidDays.size(); ++otherDayIdx) {
                    var day = raidDays.get(dayIdx);
                    var otherDay = raidDays.get(otherDayIdx);

                    Clause pair = new Clause();
                    pair.addLiteral(vars.get(chr).get(day), true);
                    pair.addLiteral(vars.get(chr).get(otherDay), true);
                    ex.addClause(pair);
                }
            }
        }

        return ex;
    }

    private String interpretSolution(Expression ex, String solution) {
        return ex.parseSolution(solution);
    }
}
