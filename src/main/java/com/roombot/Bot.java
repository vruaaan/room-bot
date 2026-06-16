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
import com.roombot.commands.HelpCmd;
import com.roombot.commands.RoomsCmd;
import com.roombot.commands.MineCmd;
import com.roombot.commands.TodayCmd;
import com.roombot.commands.TmrCmd;
import com.roombot.commands.BookCmd;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final RoomsCmd roomsCmd;
    private final DateCmd dateCmd;
    private final MineCmd myBookingsCmd;
    private final TodayCmd tdyCmd;
    private final TmrCmd tmrCmd;
    private final BookCmd bookCmd;
    private final HelpCmd helpCmd;

    public Bot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.roomsCmd = new RoomsCmd(telegramClient);
        this.dateCmd = new DateCmd(telegramClient);
        this.myBookingsCmd = new MineCmd(telegramClient);
        this.tdyCmd = new TodayCmd(telegramClient);
        this.tmrCmd = new TmrCmd(telegramClient);
        this.bookCmd = new BookCmd(telegramClient);
        this.helpCmd = new HelpCmd(telegramClient);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) { // ignores updates that dont have messages
            return;
        }
        String text = update.getMessage().getText().trim(); //parsing message into string
        String chatId = update.getMessage().getChatId().toString();

        if (text.startsWith("/rooms")) { // calling /rooms
            roomsCmd.execute(chatId, text);
        } else if (text.startsWith("/date")) { // calling /date
            dateCmd.execute(chatId, text);
        } else if (text.startsWith("/mybookings")){ // calling /mybookings
            myBookingsCmd.execute(chatId, text);
        } else if (text.startsWith("/book")) { // calling /book 
            bookCmd.execute(chatId, text);
        } else if (text.startsWith("/tdy")) { // calling /tdy
            tdyCmd.execute(chatId, text);
        } else if (text.startsWith("/tmr")) { // calling /tmr
            tmrCmd.execute(chatId, text);
        } else if (text.startsWith("/start") || text.startsWith("/help")) { // calling /start or /help
            helpCmd.execute(chatId, text);
        } else { // unrecognisable command
            sendText(chatId, "Unknown command. Type /help to see what I can do.");
        }
    }


    // helper functions 
    private void sendText(String chatId, String text) { // for returning messages to users
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
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



