package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseDate;
import com.roombot.util.ParseMessage;

import java.time.LocalDate;

public class DateCmd extends Cmd {
    public DateCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        try {
            LocalDate date = ParseDate.parse(text)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid date"));
            String response = ParseMessage.parseDate(date, resSvc.findByDate(date));
            sendText(chatId, response);
        } catch (IllegalArgumentException e) {
            sendText(chatId, "Invalid date format.");
        } catch (Exception e) {
            System.err.println("/date failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }
}