package de.affenbande.reloaded.raidapplicantsdistributor.satsolver;

import java.util.HashMap;
import java.util.Map;

public class Clause {
    private final Map<Variable, Boolean> literals = new HashMap<>();

    public void addLiteral(Variable variable) {
        addLiteral(variable, false);
    }

    public void addLiteral(Variable variable, boolean negated) {
        literals.put(variable, negated);
    }

    public Map<Variable, Boolean> getLiterals() {
        return literals;
    }
}
