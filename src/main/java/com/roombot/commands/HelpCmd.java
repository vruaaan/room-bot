package com.roombot.commands;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class HelpCmd extends Cmd {
    private static final String helpString = """
        *Room Reservation Bot*
        /seerooms — list all available rooms
        /seedate <YYYY-MM-DD> — check availability for a date
        /reserve <room> <date> <time> — make a reservation _(coming soon)_
        /myreservations — your upcoming bookings _(coming soon)_
        /cancel <id> — cancel a booking _(coming soon)_
        """;
    
    public HelpCmd(TelegramClient telegramClient){
        super(telegramClient);
    }

    @Override
    public void execute(String chatId, String text) {
        sendMarkdown(chatId, helpString);
    }
    
}
