// message router, receives all telegram messages
// routes telegram messages into commands to handlers
package com.roombot;
// importing the 
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
// importing the commands 
import com.roombot.commands.DateCmd; 
import com.roombot.commands.RoomsCmd;
import com.roombot.commands.MyBookingsCmd;
import com.roombot.commands.TodayCmd;
import com.roombot.commands.TmrCmd;
import com.roombot.commands.BookCmd;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final RoomsCmd RoomsCmd;
    private final DateCmd DateCmd;
    private final MyBookingsCmd MyBookingsCmd;
    private final TodayCmd TodayCmd;
    private final TmrCmd TmrCmd;
    private final BookCmd BookCmd;

    public Bot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.RoomsCmd = new RoomsCmd(telegramClient);
        this.DateCmd = new DateCmd(telegramClient);
        this.MyBookingsCmd = new MyBookingsCmd(telegramClient);
        this.TodayCmd = new TodayCmd(telegramClient);
        this.TmrCmd = new TmrCmd(telegramClient);
        this.BookCmd = new BookCmd(telegramClient);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) { // ignores updates that dont have messages
            return;
        }
        String text = update.getMessage().getText().trim(); //parsing message into string
        String chatId = update.getMessage().getChatId().toString();

        if (text.startsWith("/seerooms")) { // calling seerooms
            seeRoomsCmd.execute(chatId, text);
        } else if (text.startsWith("/seedate")) { // calling seedate
            seeDateCmd.execute(chatId, text);
        } else if (text.startsWith("/start") || text.startsWith("/help")) { // calling /start or /help
            sendHelp(chatId);
        } else { // unrecognisable command
            sendText(chatId, "Unknown command. Type /help to see what I can do.");
        }
    }


    // helper functions 
    private void sendHelp(String chatId) { // for sending users guiding instructions, triggered by /help
        String help = """
                *Room Reservation Bot*
                /seerooms — list all available rooms
                /seedate <YYYY-MM-DD> — check availability for a date
                /reserve <room> <date> <time> — make a reservation _(coming soon)_
                /myreservations — your upcoming bookings _(coming soon)_
                /cancel <id> — cancel a booking _(coming soon)_
                """;
        sendMarkdown(chatId, help);
    }

    private void sendText(String chatId, String text) { // for returning messages to users
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(msg);
    }

    private void sendMarkdown(String chatId, String markdown) { 
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
            // swap for a real logger once you wire up SLF4J
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}



