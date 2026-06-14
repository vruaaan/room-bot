package com.roombot.util;

import java.time.LocalTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTime {

    // 12-hour: "3pm", "3:30pm", "3:30 pm", "3 pm"
    private static final Pattern TWELVE_HR = Pattern.compile("^(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)$");

    // 24-hour: "15:30", "0900", "09:00"
    private static final Pattern TWENTYFOUR_HR = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    // Military compact: "0900", "1530" (4 digits, no colon)
    private static final Pattern MILITARY = Pattern.compile("^(\\d{2})(\\d{2})$");

    public static Optional<LocalTime> parse(String input) {
        if (input == null) { 
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase();
        switch (normalised) {// natural language shortcuts
            case "noon":
            case "midday":
                return Optional.of(LocalTime.NOON);
            case "midnight":
                return Optional.of(LocalTime.MIDNIGHT);
            default:
                break;
        }
        Matcher m12 = TWELVE_HR.matcher(normalised); // 12-hour format
        if (m12.matches()) {
            int hour = Integer.parseInt(m12.group(1));
            int minute = m12.group(2) != null ? Integer.parseInt(m12.group(2)) : 0;
            String period = m12.group(3);

            if (hour < 1 || hour > 12 || minute > 59) {
                return Optional.empty(); // invalid format for time 
            }
            if (period.equals("am")) { // am
                hour = (hour == 12) ? 0 : hour;  
            } else { // pm
                hour = (hour == 12) ? 12 : hour + 12;
            }
            return Optional.of(LocalTime.of(hour, minute));
        }
        Matcher m24 = TWENTYFOUR_HR.matcher(normalised); // 24-hour colon format 
        if (m24.matches()) {
            int hour = Integer.parseInt(m24.group(1));
            int minute = Integer.parseInt(m24.group(2));
            if (hour > 23 || minute > 59) { // invalid format for time 
                return Optional.empty();
            }
            return Optional.of(LocalTime.of(hour, minute));
        }
        Matcher mil = MILITARY.matcher(normalised); // military compact format (e.g. "0900", "1530") 
        if (mil.matches()) {
            int hour = Integer.parseInt(mil.group(1));
            int minute = Integer.parseInt(mil.group(2));
            if (hour > 23 || minute > 59) {
                return Optional.empty();
            }
            return Optional.of(LocalTime.of(hour, minute));
        }
        return Optional.empty();
    }
}