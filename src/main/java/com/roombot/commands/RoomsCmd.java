package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RoomsCmd extends Cmd {

    public RoomsCmd(TelegramClient telegramClient) {
        super(telegramClient); // calling constructor from superclass
    }

    @Override
    public void execute(String chatId, String text) { // INCOMPLETE
        String roomName = extractRoom(text);

        sendMarkdown(chatId, "*Available rooms:* ...");
    }

    private String extractRoom(String text) {
        if 
    }

}