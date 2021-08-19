package de.affenbande.reloaded.raidapplicantsdistributor.satsolver;

import java.time.DayOfWeek;

public class Variable {
    private final String characterName;
    private final DayOfWeek weekDay;

    public Variable(String characterName, DayOfWeek weekDay) {
        this.characterName = characterName;
        this.weekDay = weekDay;
    }

    public String getCharacterName() {
        return characterName;
    }

    public DayOfWeek getWeekDay() {
        return weekDay;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        Variable variable = (Variable)o;
        return getCharacterName().equals(variable.getCharacterName()) && getWeekDay().equals(variable.getWeekDay());
    }
}
