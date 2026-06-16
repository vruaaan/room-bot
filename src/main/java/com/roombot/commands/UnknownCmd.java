package com.roombot.commands;

import com.roombot.service.ReservationSvc;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class UnknownCmd extends Cmd {

    public UnknownCmd(TelegramClient telegramClient, ReservationSvc reservations) {
        super(telegramClient, reservations);
    }

    @Override
    public void execute(String chatId, String userHandle, String text) {
        sendText(chatId, "Unknown command. Type /help to see what I can do.");
    }
}