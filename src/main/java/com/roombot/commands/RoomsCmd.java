package com.roombot.commands;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.roombot.service.ReservationSvc;
import com.roombot.util.ParseVenue;
import com.roombot.util.ParseMessage;

public class RoomsCmd extends Cmd {
    public RoomsCmd(TelegramClient telegramClient, ReservationSvc resSvc) {
        super(telegramClient, resSvc); // calling constructor from superclas
    }

    @Override
    public void execute(String chatId, String teleHandle, String text) {
        String args = this.parseArgs(text);
        try {
            String venue = ParseVenue.parse(args)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid venue/venue format"));
            String response = ParseMessage.parseRoom(venue, resSvc.findByVenue(venue));
            sendText(chatId, response);
        } catch (IllegalArgumentException e) {
            sendText(chatId, e.getMessage());
        } catch (Exception e) {
            System.err.println("/room failed: " + e.getMessage());
            sendText(chatId, "Something went wrong, please try again");
        }
    }


}