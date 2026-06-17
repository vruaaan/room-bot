package com.roombot.commands;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;

public class HelpCmd extends Cmd {
    private static final String helpString = """
    *Venue Booking Bot*
    
    /book <venue> <date> <start> <end> — make a booking
      _e.g. /book 13L tomorrow 2pm 4pm_

    /rooms <venue> — view bookings for a venue
      _e.g. /rooms 13L_

    /date <date> — view bookings for a date
      _e.g. /date next fri_

    /tdy — view today's bookings
    /tmr — view tomorrow's bookings
    /mine — view your own bookings

    /help, /start — show this message

    *Accepted dates:* today, tmr, mon, next fri, 15 jun, 15-06-2026
    *Accepted times:* 2pm, 14:00, 1400, noon, midnight
    """;
    
    public HelpCmd(TelegramClient telegramClient, ReservationSvc resSvc){
        super(telegramClient, resSvc);
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        sendMarkdown(chatId, helpString);
    }
    
}
