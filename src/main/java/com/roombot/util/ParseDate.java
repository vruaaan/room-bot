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

    private static final Map<String, DayOfWeek> DAY_ALIASES = Map.of(
            "monday", DayOfWeek.MONDAY, "mon", DayOfWeek.MONDAY,
            "tuesday", DayOfWeek.TUESDAY, "tue", DayOfWeek.TUESDAY,
            "wednesday", DayOfWeek.WEDNESDAY, "wed", DayOfWeek.WEDNESDAY,
            "thursday", DayOfWeek.THURSDAY, "thu", DayOfWeek.THURSDAY,
            "friday", DayOfWeek.FRIDAY, "fri", DayOfWeek.FRIDAY);

    private static final Map<String, DayOfWeek> DAY_ALIASES_2 = Map.of(
            "saturday", DayOfWeek.SATURDAY, "sat", DayOfWeek.SATURDAY,
            "sunday", DayOfWeek.SUNDAY, "sun", DayOfWeek.SUNDAY);

    private static final Map<String, Month> MONTH_ALIASES = Map.of(
            "jan", Month.JANUARY, "feb", Month.FEBRUARY,
            "mar", Month.MARCH, "apr", Month.APRIL,
            "may", Month.MAY, "jun", Month.JUNE,
            "jul", Month.JULY, "aug", Month.AUGUST,
            "sep", Month.SEPTEMBER, "oct", Month.OCTOBER);

    private static final Map<String, Month> MONTH_ALIASES_2 = Map.of(
            "nov", Month.NOVEMBER,
            "dec", Month.DECEMBER);

    private static final Pattern DATE_MONTH = Pattern.compile(
            "^(?:(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\w+)|(\\w+)\\s+(\\d{1,2})(?:st|nd|rd|th)?)$");

    private static final Pattern ISO_DATE = Pattern.compile(
            "^(\\d{2})[/\\-]?(\\d{2})[/\\-]?(\\d{4}|\\d{2})$");

    public static Optional<LocalDate> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase();
        LocalDate today = LocalDate.now();
        switch (normalised) { // today / tomorrow
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

        if (normalised.startsWith("next ")) { // for chaining "next"
            int extraWeeks = 0;
            String remaining = normalised;
            while (remaining.startsWith("next ")) {
                extraWeeks++;
                remaining = remaining.substring(5).trim();
            }
            DayOfWeek dow = checkDayMap(remaining);
            if (dow == null)
                return Optional.empty();
            LocalDate base = today.with(TemporalAdjusters.next(dow));
            return Optional.of(base.plusWeeks(extraWeeks - 1));
        }

        DayOfWeek dow = checkDayMap(normalised); // checking based off day of week
        if (dow != null) {
            return Optional.of(today.with(TemporalAdjusters.nextOrSame(dow)));
        }

        Matcher dm = DATE_MONTH.matcher(normalised);
        if (dm.matches()) { // checking based off textual dates
            String dayStr, monthStr;
            if (dm.group(1) != null) {
                dayStr = dm.group(1);
                monthStr = dm.group(2);
            } else {
                monthStr = dm.group(3);
                dayStr = dm.group(4);
            }
            Month month = checkMonthMap(monthStr);
            if (month == null) {
                return Optional.empty();
            }
            int day = Integer.parseInt(dayStr);
            LocalDate candidate = LocalDate.of(today.getYear(), month, day);
            if (candidate.isBefore(today))
                candidate = candidate.plusYears(1);
            return Optional.of(candidate);
        }

        Matcher iso = ISO_DATE.matcher(normalised);
        if (iso.matches()) { // checking based of numerical dates
            int day = Integer.parseInt(iso.group(1));
            int month = Integer.parseInt(iso.group(2));
            String yearStr = iso.group(3);
            int year = yearStr.length() == 2
                    ? 2000 + Integer.parseInt(yearStr)
                    : Integer.parseInt(yearStr);
            return Optional.of(LocalDate.of(year, month, day));
        }

        return Optional.empty();
    }

    // HELPERS
    private static DayOfWeek checkDayMap(String text) {
        DayOfWeek dow = DAY_ALIASES.get(text);
        return dow != null ? dow : DAY_ALIASES_2.get(text);
    }

    private static Month checkMonthMap(String text) {
        if (text == null || text.length() < 3)
            return null;
        String key = text.substring(0, 3);
        Month m = MONTH_ALIASES.get(key);
        return m != null ? m : MONTH_ALIASES_2.get(key);
    }
}