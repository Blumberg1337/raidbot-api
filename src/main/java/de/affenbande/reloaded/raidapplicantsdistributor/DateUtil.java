package de.affenbande.reloaded.raidapplicantsdistributor;

import lombok.NonNull;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public abstract class DateUtil {

    public static String convertPossibleDayToRaid(final String possibleDayToRaid) {
        if (possibleDayToRaid.equals("1")) {
            return DayOfWeek.WEDNESDAY.toString();
        }
        if ( possibleDayToRaid.equals("2")) {
            return DayOfWeek.THURSDAY.toString();
        }
        if (possibleDayToRaid.equals("3")) {
            return DayOfWeek.FRIDAY.toString();
        }
        if (possibleDayToRaid.equals("4")) {
            return DayOfWeek.SATURDAY.toString();
        }
        if (possibleDayToRaid.equals("5")) {
            return DayOfWeek.SUNDAY.toString();:
        }
        if (possibleDayToRaid.equals("6")) {
            return DayOfWeek.MONDAY.toString();
        }
        if (possibleDayToRaid.equals("7")) {
            return DayOfWeek.TUESDAY.toString();
        } else {
            throw new DateTimeException("Couldn't convert possibleDayToRaid-value: " + possibleDayToRaid);
        }
    }

    public static LocalDateTime getNextRaidingDate(@NonNull final DayOfWeek dayOfWeek) {
        return LocalDateTime
            .now()
            .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
            .with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    public static LocalDateTime getNextRaidingDate(@NonNull final String day) {
        return getNextRaidingDate(convertToDayOfWeek(day));
    }

    public static DayOfWeek convertToDayOfWeek(@NonNull final String day) {
        return DayOfWeek.valueOf(day);
    }

    public static String format(LocalDateTime raidingDate) {
        String var10000 = format(raidingDate.getDayOfWeek());
        return var10000 + " - " +
            raidingDate.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.GERMANY));
    }

    public static String format(DayOfWeek raidingDay) {
        return raidingDay.getDisplayName(TextStyle.FULL, Locale.GERMAN);
    }

    public static String format(DayOfWeek raidingDay, Locale locale) {
        return raidingDay.getDisplayName(TextStyle.FULL, locale);
    }

    public static String format(DayOfWeek raidingDay, TextStyle textStyle, Locale locale) {
        return raidingDay.getDisplayName(textStyle, locale);
    }
}
