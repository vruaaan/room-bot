package com.roombot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseDate {
    // Map to use as a form of dictionary
    private static final Map<String, Month> MONTH_ALIASES = Map.of( 
        "jan", Month.JANUARY,
        "feb", Month.FEBRUARY,
        "mar", Month.MARCH,
        "apr", Month.APRIL,
        "may", Month.MAY,
        "jun", Month.JUNE,
        "jul", Month.JULY,
        "aug", Month.AUGUST,
        "sep", Month.SEPTEMBER,
        "oct", Month.OCTOBER
    );

    private static final Map<String, Month> MONTH_ALIASES_2 = Map.of( //Map.of capped at 10 items
        "nov", Month.NOVEMBER,
        "dec", Month.DECEMBER
    );

    // matches "3 june", "3rd jun", "june 3", "jun 3rd", "3rd of june"
    private static final Pattern DATE_MONTH = Pattern.compile(
        "^(?:(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\w+)|(\\w+)\\s+(\\d{1,2})(?:st|nd|rd|th)?)$"
    );

    // matches "03062026", "030626", "03/06/2026", "03-06-2026", "03/06/26"
    private static final Pattern ISO_DATE = Pattern.compile(
        "^(\\d{2})[/\\-]?(\\d{2})[/\\-]?(\\d{4}|\\d{2})$"
    );
    
    public static Optional<LocalDate> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase(); // formatting to readable form 
        
        Matcher dm = DATE_MONTH.matcher(normalised);
        if (dm.matches()) {
            String dayStr, monthStr;
            if (dm.group(1) != null) { // day-first: "3rd june"
                dayStr = dm.group(1);
                monthStr = dm.group(2);
            } else { // month-first: "june 3rd"
                monthStr = dm.group(3);
                dayStr = dm.group(4);
            }
            Month month = checkMonthMap(monthStr);

            if (month == null) {
                return Optional.empty();
            }

            int day = Integer.parseInt(dayStr);
            LocalDate today = LocalDate.now();
            int thisYear = today.getYear();

            LocalDate candidate = LocalDate.of(thisYear, month, day);
            if (candidate.isBefore(today)) {
                candidate = candidate.plusYears(1);
            }
            return Optional.of(candidate);
        }

        Matcher iso = ISO_DATE.matcher(normalised);
        if (iso.matches()) {
            int day = Integer.parseInt(iso.group(1));
            int month = Integer.parseInt(iso.group(2));
            String yearStr = iso.group(3);
            int year = yearStr.length() == 2
                ? 2000 + Integer.parseInt(yearStr) // "26" → 2026
                : Integer.parseInt(yearStr); // "2026" → 2026
            return Optional.of(LocalDate.of(year, month, day));
        }
        return Optional.empty();
    }

    private static Month checkMonthMap(String text) { // INCOMPLETE
        if (text == null || text.length() < 3) {
            return null;
        } 
        String key = text.substring(0, 3);
        Month m = MONTH_ALIASES.get(key);
        return m!= null ? m 
                        : MONTH_ALIASES_2.get(key);
    }
}