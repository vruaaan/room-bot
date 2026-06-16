package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseDate;

import java.util.Optional;
import java.time.LocalDate;

public class DateCmd extends Cmd {

    public DateCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        Optional<LocalDate> date = ParseDate.parse(text);
        Optional<LocalDate> date2 = Parse 


        sendMarkdown(chatId, "*Available rooms:* ...");
    }
}