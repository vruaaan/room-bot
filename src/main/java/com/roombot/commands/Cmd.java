package com.roombot.commands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;

public abstract class Cmd {
    protected final TelegramClient telegramClient;
    protected final ReservationSvc resSvc;

    Cmd(TelegramClient telegramClient, ReservationSvc resSvc) { // called by subclasses
        this.telegramClient = telegramClient;
        this.resSvc = resSvc;
    }
    
    public abstract void execute(String chatId, String teleHandle, String text); // must be implemented by subclasses 

    //helper functions that all other commands can call 
    protected void sendText(String chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(msg);
    }

    protected void sendMarkdown(String chatId, String markdown) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(markdown)
                .parseMode("Markdown")
                .build();
        execute(msg);
    }

    protected String parseArgs(String text) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace == -1) {
            return "";
        }

        return trimmed.substring(firstSpace + 1).trim();
    }

    private void execute(SendMessage msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("[" + getClass().getSimpleName() + "] Failed to send message: " + e.getMessage());
        }
    }
}
