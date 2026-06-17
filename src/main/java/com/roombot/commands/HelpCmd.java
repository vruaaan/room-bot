package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;

public class HelpCmd extends Cmd {
    private static final String helpString = """
            *Venue Booking Bot*
            Book and manage room reservations right from Telegram.

            *Commands*

            /book <venue> <date> <start> <end>
            Create a booking. Fails if it clashes with an existing one.
              _e.g. /book 13L tomorrow 2pm 4pm_

            /cancel <venue> <date> <start> <end>
            Cancel a booking you made. Must match exactly, and you must be the one who made it.
              _e.g. /cancel 13L tomorrow 2pm 4pm_

            /rooms <venue>
            View all bookings for a venue.
              _e.g. /rooms 13L_

            /date <date>
            View all bookings for a specific date.
              _e.g. /date next fri_

            /tdy — view today's bookings
            /tmr — view tomorrow's bookings
            /mine — view your own upcoming bookings

            /help, /start — show this message

            *Accepted venues*
            Defined in venues.json — ask your admin if you're unsure which names are recognised.

            *Accepted dates*
            today, tdy, tonight, tomorrow, tmr, mon, next fri, next next mon, 15 jun, jun 15, 15-06-2026, 15062026

            *Accepted times*
            2pm, 2:30pm, 14:00, 1400, noon, midnight

            *Notes*
            - Times must have an end after the start, or the booking is treated as overnight if the end time is earlier in the day.
            - Numeric dates are read as day-month-year, not year-month-year.
            - You can only cancel bookings made under your own Telegram username.
            """;

    public HelpCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc);
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        sendMarkdown(chatId, helpString);
    }

}
