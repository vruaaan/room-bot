package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MyBookingsCmd extends Cmd {

    public MyBookingsCmd(TelegramClient telegramClient) {
        super(telegramClient); // calling constructor from superclass
    }

    @Override
    public void execute(String chatId, String text) { // INCOMPLETE
        sendMarkdown(chatId, "*Available rooms:* ...");
    }
}