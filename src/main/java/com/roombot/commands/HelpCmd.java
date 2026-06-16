package com.roombot.commands;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;

public class HelpCmd extends Cmd {
    private static final String helpString = """
        *Venue Booking Bot*
        /seerooms — list all available rooms
        /seedate <YYYY-MM-DD> — check availability for a date
        /reserve <room> <date> <time> — make a reservation _(coming soon)_
        /myreservations — your upcoming bookings _(coming soon)_
        /cancel <id> — cancel a booking _(coming soon)_
        """; // NEED TO UPDATE HELP CMD 
    
    public HelpCmd(TelegramClient telegramClient, ReservationSvc resSvc){
        super(telegramClient, resSvc);
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        sendMarkdown(chatId, helpString);
    }
    
}
