package com.roombot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;

public class ParseTimes {

    private static final Map<String, DayOfWeek> DAY_ALIASES = Map.of(
        "monday", DayOfWeek.MONDAY,
        "mon", DayOfWeek.MONDAY,
        "tuesday", DayOfWeek.TUESDAY,
        "tue", DayOfWeek.TUESDAY,
        "wednesday", DayOfWeek.WEDNESDAY,
        "wed", DayOfWeek.WEDNESDAY,
        "thursday", DayOfWeek.THURSDAY,
        "thu", DayOfWeek.THURSDAY,
        "friday", DayOfWeek.FRIDAY,
        "fri", DayOfWeek.FRIDAY
    );

    private static final Map<String, DayOfWeek> DAY_ALIASES_2 = Map.of( //Map.of capped at 10 items
        "saturday", DayOfWeek.SATURDAY,
        "sat",      DayOfWeek.SATURDAY,
        "sunday",   DayOfWeek.SUNDAY,
        "sun",      DayOfWeek.SUNDAY
    );

    /**
     * Parses a natural-language date string into a LocalDate.
     *
     * Recognised inputs (case-insensitive):
     *   today, tdy, tonight       → today
     *   tomorrow, tmr, tmrw       → tomorrow
     *   monday … sunday / mon … sun → next occurrence of that weekday (today counts)
     *   next monday … next sunday  → the occurrence at least 1 day away
     *
     */
    public static Optional<LocalDate> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase();
        LocalDate today = LocalDate.now();

        switch (normalised) {
            case "today":
            case "tdy":
            case "tonight":
                return Optional.of(today);
            case "tomorrow":
            case "tmr":
            case "tmrw":
                return Optional.of(today.plusDays(1));
            default:
                break;
        }
        // "next <day>" — strictly next week (always at least 1 day away)
        if (normalised.startsWith("next ")) {
            String dayPart = normalised.substring(5).trim();
            DayOfWeek dow = lookupDay(dayPart);
            if (dow == null) return Optional.empty();
            // TemporalAdjusters.next() always moves forward, never stays on today
            return Optional.of(today.with(TemporalAdjusters.next(dow)));
        }

        // bare weekday name — next occurrence including today
        DayOfWeek dow = lookupDay(normalised);
        if (dow != null) {
            return Optional.of(today.with(TemporalAdjusters.nextOrSame(dow)));
        }

        return Optional.empty();
    }

    private static DayOfWeek checkMap(String text) {
        DayOfWeek dow = DAY_ALIASES.get(text);
        return dow != null ? dow : DAY_ALIASES_2.get(string);
    }
}