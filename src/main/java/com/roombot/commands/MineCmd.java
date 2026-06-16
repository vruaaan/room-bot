package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MineCmd extends Cmd {

    public MineCmd(TelegramClient telegramClient) {
        super(telegramClient); // calling constructor from superclass
    }

    @Override
    public void execute(String chatId, String text) { // INCOMPLETE
        sendMarkdown(chatId, "*Available rooms:* ...");
    }
}