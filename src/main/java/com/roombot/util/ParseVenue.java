package com.roombot.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.List;

public class ParseVenue {
    private static final String VENUES_FILE = "venues.json";
    private static final Map<String, String> VENUE_ALIASES = loadVenueAliases();

    public static Optional<String> parse(String input) {
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
        Map<String, String> venueMap = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper(); // creates a mapper object
            Map<String, List<String>> raw = mapper.readValue(
                    new File(VENUES_FILE), // reading the file
                    mapper.getTypeFactory()
                            .constructMapType(HashMap.class, // class of the map to construct
                                    String.class, // class of the key
                                    List.class)); // class of the value

            for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
                String key = entry.getKey().trim();
                if (key.isBlank()) {
                    continue;
                }
                venueMap.put(key.toLowerCase(), key); // adds the normalised name to the map
                for (String altName : entry.getValue()) { // iterates over the array of alternative names
                    String normalisedAltName = altName.trim().toLowerCase(); // normalises the alternative names
                    if (!normalisedAltName.isBlank()) {
                        venueMap.put(normalisedAltName, key); // puts the alternative names in
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + VENUES_FILE + ": " + e.getMessage());
        }
        return venueMap;
    }
}