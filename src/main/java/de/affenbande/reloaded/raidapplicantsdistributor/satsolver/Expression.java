package de.affenbande.reloaded.raidapplicantsdistributor.satsolver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.time.format.TextStyle;
import java.util.*;

public class Expression {
    private final List<Clause> clauses = new ArrayList<>();
    private final BiMap<Variable, Integer> knownVariables = HashBiMap.create();

    public void addClause(Clause clause) {
        clauses.add(clause);
        for (var variable : clause.getLiterals().keySet()) {
            knownVariables.putIfAbsent(variable, knownVariables.size() + 1);
        }
    }

    public String generateCNF() {
        // write header
        StringBuilder builder = new StringBuilder("p cnf ").append(knownVariables.size()).append(' ').append(clauses.size()).append('\n');
        // write clauses
        for (var clause : clauses) {
            for (var literal : clause.getLiterals().entrySet()) {
                if (literal.getValue()) {
                    builder.append('-');
                }
                builder.append(knownVariables.get(literal.getKey())).append(' ');
            }
            builder.append("0\n");
        }
        return builder.toString();
    }

    public String parseSolution(String solution) {
        var literals = new ArrayList<>(Arrays.asList(solution.split(" ")));

        // get rid of the trailing "0"
        literals.remove(literals.size() - 1);

        // inverse-map integers to propositional variables
        var knownVariableKeys = knownVariables.inverse();

        StringBuilder builder = new StringBuilder("Solution available :)\n\n");
        for (var lit : literals) {
            var negated = lit.startsWith("-");
            var variableKey = Integer.parseUnsignedInt(negated ? lit.substring(1) : lit);

            var variable = knownVariableKeys.get(variableKey);
            builder
                    .append(variable.getCharacterName())
                    .append(' ')
                    .append(variable.getWeekDay().getDisplayName(TextStyle.SHORT, Locale.GERMAN))
                    .append(": ")
                    .append(negated ? "NO" : "YES")
                    .append('\n');
        }
        return builder.toString();
    }
}
