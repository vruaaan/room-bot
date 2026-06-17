package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseMessage;

import java.time.LocalDate;

public class TmrCmd extends Cmd {
    public TmrCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        LocalDate tmr = LocalDate.now().plusDays(1);
        try {
            String response = ParseMessage.parseDate(tmr, resSvc.findByDate(tmr));
            sendText(chatId, response);
        } catch (Exception e) {
            System.err.println("/tmr failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }
}