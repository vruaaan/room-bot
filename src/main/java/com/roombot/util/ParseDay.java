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
        // "next <day>" — strictly next week (always at least 1 day away)
        if (normalised.startsWith("next ")) {
            String dayPart = normalised.substring(5).trim();
            DayOfWeek dow = Map(dayPart);
            if (dow == null) return Optional.empty();
            // TemporalAdjusters.next() always moves forward, never stays on today
            return Optional.of(today.with(TemporalAdjusters.next(dow)));
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