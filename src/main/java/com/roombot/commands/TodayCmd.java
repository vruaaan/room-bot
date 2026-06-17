package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseMessage;

import java.time.LocalDate;

public class TodayCmd extends Cmd {
    public TodayCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        LocalDate tdy = LocalDate.now();
        try {
            String response = ParseMessage.parseDate(tdy, resSvc.findByDate(tdy));
            sendText(chatId, response);
        } catch (Exception e) {
            System.err.println("/tdy failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }
}