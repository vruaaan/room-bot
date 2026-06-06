package com.roombot.commands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

abstract class Cmd {
    protected final TelegramClient telegramClient;

    Cmd(TelegramClient telegramClient) { // called by subclasses
        this.telegramClient = telegramClient;
    }

    public abstract void execute(String chatId, String text); // must be implemented by subclasses 

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

    private void execute(SendMessage msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("[" + getClass().getSimpleName() + "] Failed to send message: " + e.getMessage());
        }
    }
}