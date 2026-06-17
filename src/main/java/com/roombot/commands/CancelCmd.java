package com.roombot.commands;

import com.roombot.model.Reservation;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseTime;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalTime;
import java.util.Optional;

public class CancelCmd extends Cmd {

    private static final String USAGE = "Usage: /cancel <venue> <date> <start> <end>\n" +
            "Example: /cancel 13L tomorrow 2pm 4pm";

    private static final String NOT_FOUND = "No matching bookings found";
    private static final String NOT_YOURS = "Booking found not made by you";

    public CancelCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc);
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        String args = this.parseArgs(text);
        String[] parts = args.split("\\s+");
        if (parts.length < 4) {
            sendText(chatId, USAGE);
            return;
        }
        Optional<LocalTime> start = ParseTime.parse(parts[parts.length - 2]);
        Optional<LocalTime> end = ParseTime.parse(parts[parts.length - 1]);
        Optional<VenueDate> cancelArgs = parseCancelBookArgs(parts);
        if (cancelArgs.isEmpty() || start.isEmpty() || end.isEmpty() || !end.get().isAfter(start.get())) {
            sendText(chatId, buildErrorMessage( // returning error message if missing fields
                    cancelArgs.map(ca -> ca.venue()),
                    cancelArgs.map(ca -> ca.date()),
                    start,
                    end));
            return;
        }
        VenueDate parsed = cancelArgs.get();
        try {
            Optional<Reservation> match = findMatch(parsed, start.get(), end.get());
            if (match.isEmpty()) {
                sendText(chatId, NOT_FOUND);
                return;
            }
            if (!match.get().getPOC().equals(teleHandle)) {
                sendText(chatId, NOT_YOURS);
                return;
            }
            resSvc.delete(match.get().getId());
            sendText(chatId, match.get().cancelString());
        } catch (Exception e) {
            System.err.println("/cancel failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again.");
        }
    }

    private Optional<Reservation> findMatch(VenueDate venuedate, LocalTime start, LocalTime end)
            throws Exception {
        return resSvc.findByVenue(venuedate.venue())
                .stream()
                .filter(r -> r.matches(venuedate.venue(), venuedate.date(), start, end))
                .findFirst();
    }

}