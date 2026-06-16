package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseMessage;
import com.roombot.model.Reservation;
import java.util.List;

import java.time.LocalDate;

public class TmrCmd extends Cmd {

     public TmrCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        LocalDate tdy = LocalDate.now();
        try {
            List<Reservation> tdyRes = resSvc.findByDate(tdy); 
            String response = ParseMessage.parseDate(tdy, tdyRes);
            sendText(chatId,response);
        } catch (Exception e) {
            System.err.println("/tdy failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }

}