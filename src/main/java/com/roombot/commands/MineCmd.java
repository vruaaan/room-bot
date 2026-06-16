package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;

import com.roombot.util.ParseMessage;

public class MineCmd extends Cmd {
    public MineCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    public void execute(String chatId, String teleHandle, String text) {
        try {
            String response = ParseMessage.parseMine(teleHandle, resSvc.findByUser(teleHandle));
            sendText(chatId,response);
        } catch (Exception e) {
            System.err.println("/tdy failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }
}