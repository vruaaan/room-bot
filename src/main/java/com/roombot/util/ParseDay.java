package com.roombot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;

public class ParseDay {
    // Map to use as a form of dictionary
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
        "sat", DayOfWeek.SATURDAY,
        "sunday", DayOfWeek.SUNDAY,
        "sun", DayOfWeek.SUNDAY
    );
    
    public static Optional<LocalDate> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase(); // formatting to readable form 
        LocalDate today = LocalDate.now(); // get current date
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

        // "next <day>" — each "next" advances by one additional week
        if (normalised.startsWith("next ")) {
            int extraWeeks = 0;
            String remaining = normalised;
            while (remaining.startsWith("next ")) {
                extraWeeks++;
                remaining = remaining.substring(5).trim();
            }
            DayOfWeek dow = checkMap(remaining);
            if (dow == null) {
                return Optional.empty();
            }
            // Each additional "next" adds another 7 days on top.
            LocalDate base = today.with(TemporalAdjusters.next(dow)); //first "next" uses TemporalAdjusters.next() to always move forward at least 1 day.
            return Optional.of(base.plusWeeks(extraWeeks - 1));
        }

        // bare weekday name — next occurrence including today
        DayOfWeek dow = checkMap(normalised);
        if (dow != null) {
            return Optional.of(today.with(TemporalAdjusters.nextOrSame(dow)));
        }

        return Optional.empty();
    }

    private static DayOfWeek checkMap(String text) {
        DayOfWeek dow = DAY_ALIASES.get(text);
        return dow != null ? dow : DAY_ALIASES_2.get(text);
    }
}