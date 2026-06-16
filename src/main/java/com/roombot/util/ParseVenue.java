package com.roombot.util;

import java.util.List;
import java.util.Optional;

public class ParseVenue {
    private static final List<String> L12 = List.of(
        "12 lounge", "12l", "level 12 lounge", "12", "lvl 12 lounge"
    );

    private static final List<String> L13 = List.of(
        "13 lounge", "13l", "level 13 lounge", "13", "lvl 13 lounge"
    );

    private static final List<String> L14 = List.of(
        "14 lounge", "14l", "level 14 lounge", "14", "lvl 14 lounge"
    );

    private static final List<String> STUDYROOM = List.of(
        "study room", "12 study room", "level 12 study room", "12 study rm", "level 12 study rm"
    );

    public static Optional <String> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase();
        if (L12.contains(normalised)){
            return Optional.of("12L");
        }
        else if (L13.contains(normalised)){
            return Optional.of("13L");
        }
        else if (L14.contains(normalised)){
            return Optional.of("14L");
        }
        else if (STUDYROOM.contains(normalised)){
            return Optional.of("StudyRoom");
        }
        return Optional.empty();
    }

}
