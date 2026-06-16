package com.roombot.commands;

import com.roombot.model.Reservation;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseDate;
import com.roombot.util.ParseTime;
import com.roombot.util.ParseVenue;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import java.util.Optional;

public class BookCmd extends Cmd {

    private static final String USAGE =
            "Usage: /book <venue> <date> <start> <end>\n" +
            "Example: /book 13L tomorrow 2pm 4pm";
    private static final String DATEISSUE = "I couldn't understand the date";
    private static final String TIMEISSUE = "I couldn't understand the time. Try formats like 2pm, 14:00 or 1400.";
    private static final String TIMELOGIC = "End time must be after the start time.";

    public BookCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc);
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        String args = this.parseArgs(text);
        String[] parts = args.split("\\s+"); // to split by amount of whitespace
        if (parts.length < 4) { // for improper usage 
            sendText(chatId, USAGE); // return usage message
            return;
        }

        Optional<LocalTime> start = ParseTime.parse(parts[parts.length - 2]);
        Optional<LocalTime> end = ParseTime.parse(parts[parts.length - 1]);
        Optional<BookArgs> bookArgs = parseBookArgs(parts);

        if (bookArgs.isEmpty() || start.isEmpty() || end.isEmpty() || !end.get().isAfter(start.get())) {
            sendText(chatId, buildErrorMessage(
                    bookArgs.map(BookArgs::date),
                    start,
                    end));
            return;
        }

        BookArgs parsed = bookArgs.get();
        Reservation res = new Reservation(teleHandle, chatId, parsed.venue(), parsed.date(), start.get(), parsed.date(), end.get());
        try {
            if (resSvc.hasConflict(res)) {
                sendText(chatId, "This slot clashes with an existing booking.");
                return; // to break the loop 
            }
            resSvc.create(res);
            sendMarkdown(chatId, "*Booked!*\n" + res.toString());
        } catch (Exception e) {
            System.err.println("/book save failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again.");
        }
    }

    private Optional<BookArgs> parseBookArgs(String[] parts) {
        int dateEndExclusive = parts.length - 2;
        for (int venueEndExclusive = 1; venueEndExclusive < dateEndExclusive; venueEndExclusive++) {
            String venueText = join(parts, 0, venueEndExclusive);
            String dateText = join(parts, venueEndExclusive, dateEndExclusive);

            Optional<String> venue = ParseVenue.parse(venueText);
            Optional<LocalDate> date = ParseDate.parse(dateText);
            if (venue.isPresent() && date.isPresent()) {
                return Optional.of(new BookArgs(venue.get(), date.get()));
            }
        }
        return Optional.empty();
    }

    private String join(String[] parts, int from, int to) {
        return String.join(" ", Arrays.copyOfRange(parts, from, to));
    }

    private String buildErrorMessage(Optional<LocalDate> date, Optional<LocalTime> start, Optional<LocalTime> end) {
        if (date.isEmpty()) {
            return DATEISSUE;
        } else if (start.isEmpty()) {
            return TIMEISSUE;
        } else if (end.isEmpty()) {
            return TIMEISSUE;
        } else if (start.isPresent() && end.isPresent() && !end.get().isAfter(start.get())) {  // start and end both present but end not after start
            return TIMELOGIC; 
        }
        return "Invalid input.";
    }

    private record BookArgs(String venue, LocalDate date) {
    }

}
