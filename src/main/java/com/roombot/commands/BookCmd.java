package com.roombot.commands;

import com.roombot.model.Reservation;
import com.roombot.service.ReservationSvc;
import com.roombot.util.MessageUtils;
import com.roombot.util.ParseDay;
import com.roombot.util.ParseTime;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    public void execute(String chatId, String userHandle, String text) {
        String[] parts = text.split("\\s+"); // to split by amount of whitespace
        if (parts.length < 5) { // for improper usage 
            sendText(chatId, USAGE); // return usage message
        }
        String venue = parts[1];
        Optional<LocalDate> date = parseDate(parts[2]);
        Optional<LocalTime> start = ParseTime.parse(parts[3]);
        Optional<LocalTime> end = ParseTime.parse(parts[4]);

        if (date.isEmpty()) {
            sendText(chatId, DATEISSUE + parts[2] + "\".");
        }
        else if (start.isEmpty() || end.isEmpty()) {
            sendText(chatId, TIMEISSUE);
        }
        if (!end.get().isAfter(start.get())) {
            sendText(chatId, TIMELOGIC);
        }
        Optional<Reservation> res = date.flatMap(d -> 
            start.flatMap(s-> 
                end.map(e -> new Reservation(userHandle, chatId, venue, d, s, d, e)) 
            )
        );

        
        Reservation reservation = new Reservation(
                userHandle, chatId, venue,
                date.get(), start.get(),
                date.get(), end.get());


        try {
            if (reservations.hasConflict(reservation)) {
                sendText(chatId, "That slot clashes with an existing booking for " + venue + ".");
                return;
            }
            reservations.create(reservation);
            sendMarkdown(chatId, "*Booked!*\n" + MessageUtils.formatReservation(reservation));
        } catch (Exception e) {
            System.err.println("[BookCmd] save failed: " + e.getMessage());
            sendText(chatId, "Sorry, something went wrong saving your booking. Please try again.");
        }
    }

    private Optional<LocalDate> parseDate(String token) { // utilising the parsing 
        Optional<LocalDate> natural = ParseDay.parse(token);
        Optional<LocalDate> 
        if (natural.isPresent()) {
            return natural;
        }
        try {
            return Optional.of(LocalDate.parse(token, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}