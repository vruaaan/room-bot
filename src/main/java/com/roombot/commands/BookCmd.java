package com.roombot.commands;

import com.roombot.model.Reservation;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseMessage;
import com.roombot.util.ParseTime;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalTime;


import java.util.Optional;

public class BookCmd extends Cmd {

    private static final String USAGE =
        "Usage: /book <venue> <date> <start> <end>\n" +
        "Example: /book 13L tomorrow 2pm 4pm";
        
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
        Optional<VenueDate> bookArgs = parseCancelBookArgs(parts);
        if (bookArgs.isEmpty() || start.isEmpty() || end.isEmpty() || !end.get().isAfter(start.get())) {
            sendText(chatId, buildErrorMessage(
                    bookArgs.map(ba -> ba.date()),
                    start,
                    end));
            return;
        }
        VenueDate parsed = bookArgs.get();
        Reservation res = new Reservation(teleHandle, chatId, parsed.venue(), parsed.date(), start.get(), parsed.date(), end.get());
        try {
            if (resSvc.hasConflict(res)) {
                sendText(chatId, "This slot clashes with an existing booking.");
                return; // to break the loop 
            }
            resSvc.create(res);
            sendText(chatId, ParseMessage.parseBooked(parsed.venue(), parsed.date(), start.get(), end.get(), teleHandle ));
        } catch (Exception e) {
            System.err.println("/book save failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again.");
        }
    }

}
