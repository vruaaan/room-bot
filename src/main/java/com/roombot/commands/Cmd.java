package com.roombot.commands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseDate;
import com.roombot.util.ParseVenue;

public abstract class Cmd {
    protected final TelegramClient telegramClient;
    protected final ReservationSvc resSvc;
    private static final String DATEISSUE = "I couldn't understand the date";
    private static final String TIMEISSUE = "I couldn't understand the time. Try formats like 2pm, 14:00 or 1400.";
    private static final String TIMELOGIC = "End time must be after the start time.";
    private static final String VENUEDATEISSUE = "Ed time must be after the start time.";

    Cmd(TelegramClient telegramClient, ReservationSvc resSvc) { // called by subclasses
        this.telegramClient = telegramClient;
        this.resSvc = resSvc;
    }
    
    public abstract void execute(String chatId, String teleHandle, String text); // must be implemented by subclasses 

    //helper functions that all other commands can call 
    protected void sendText(String chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(msg);
    }

    protected void sendMarkdown(String chatId, String markdown) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(markdown)
                .parseMode("Markdown")
                .build();
        execute(msg);
    }

    protected String parseArgs(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace == -1) {
            return "";
        }
        return trimmed.substring(firstSpace + 1).trim();
    }

    protected String buildErrorMessage(Optional<String> venue, Optional<LocalDate> date, Optional<LocalTime> start, Optional<LocalTime> end) {
        if (venue.isEmpty() && date.isEmpty()) {
            return VENUEDATEISSUE; 
        }else if (date.isEmpty()) {
            return DATEISSUE;
        } else if (start.isEmpty()) {
            return TIMEISSUE;
        } else if (end.isEmpty()) {
            return TIMEISSUE;
        } else if (start.isPresent() && end.isPresent() && !end.get().isAfter(start.get())) {
            return TIMELOGIC;
        }
        return "Invalid input.";
    }

    protected Optional<VenueDate> parseCancelBookArgs(String[] parts) {
        int dateEndExclusive = parts.length - 2;
        for (int venueEndExclusive = 1; venueEndExclusive < dateEndExclusive; venueEndExclusive++) {
            String venueText = join(parts, 0, venueEndExclusive);
            String dateText = join(parts, venueEndExclusive, dateEndExclusive);
            Optional<String> venue = ParseVenue.parse(venueText);
            Optional<LocalDate> date = ParseDate.parse(dateText);
            if (venue.isPresent() && date.isPresent()) {
                return Optional.<VenueDate>of(new VenueDate(venue.get(), date.get()));
            }
        }
        return Optional.empty();
    }

    protected String join(String[] parts, int from, int to) {
        return String.join(" ", Arrays.copyOfRange(parts, from, to));
    }

    protected record VenueDate(String venue, LocalDate date) {}

    private void execute(SendMessage msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("[" + getClass().getSimpleName() + "] Failed to send message: " + e.getMessage());
        }
    }
}
