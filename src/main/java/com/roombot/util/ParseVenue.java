package com.roombot.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ParseVenue {
    private static final String DEFAULT_VENUES =
            "12L:12,12l,12 lounge,level 12 lounge,lvl 12 lounge;" +
            "13L:13,13l,13 lounge,level 13 lounge,lvl 13 lounge;" +
            "14L:14,14l,14 lounge,level 14 lounge,lvl 14 lounge;" +
            "StudyRoom:study room,12 study room,level 12 study room,12 study rm,level 12 study rm";

    private static final Map<String, String> VENUE_ALIASES = loadVenueAliases();

    public static Optional <String> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalised = input.trim().toLowerCase();
        if (normalised.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(VENUE_ALIASES.get(normalised));
    }

    private static Map<String, String> loadVenueAliases() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        String venues = stripQuotes(dotenv.get("VENUES", DEFAULT_VENUES));

        Map<String, String> aliases = new HashMap<>();
        for (String venueConfig : venues.split(";")) {
            String[] parts = venueConfig.split(":", 2);
            if (parts.length != 2) {
                continue;
            }

            String canonical = parts[0].trim();
            if (canonical.isBlank()) {
                continue;
            }

            aliases.put(canonical.toLowerCase(), canonical);
            for (String alias : parts[1].split(",")) {
                String normalisedAlias = alias.trim().toLowerCase();
                if (!normalisedAlias.isBlank()) {
                    aliases.put(normalisedAlias, canonical);
                }
            }
        }

        return aliases;
    }

    private static String stripQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 &&
                ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
                (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

}
