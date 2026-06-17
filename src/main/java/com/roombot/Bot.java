// update router — receives all Telegram updates and dispatches to the matching command
package com.roombot;

import com.roombot.commands.BookCmd;
import com.roombot.commands.CancelCmd;
import com.roombot.commands.Cmd;
import com.roombot.commands.DateCmd;
import com.roombot.commands.HelpCmd;
import com.roombot.commands.MineCmd;
import com.roombot.commands.RoomsCmd;
import com.roombot.commands.TmrCmd;
import com.roombot.commands.TodayCmd;
import com.roombot.commands.UnknownCmd;
import com.roombot.service.ReservationSvc;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final Map<String, Cmd> commands = new HashMap<>();
    private final Cmd unknownCmd;

    public Bot(TelegramClient telegramClient, ReservationSvc reservations) {
        // register commands
        commands.put("/rooms", new RoomsCmd(telegramClient, reservations));
        commands.put("/date", new DateCmd(telegramClient, reservations));
        commands.put("/mine", new MineCmd(telegramClient, reservations));
        commands.put("/book", new BookCmd(telegramClient, reservations));
        commands.put("/tdy", new TodayCmd(telegramClient, reservations));
        commands.put("/tmr", new TmrCmd(telegramClient, reservations));
        commands.put("/cancel", new CancelCmd(telegramClient, reservations));

        HelpCmd help = new HelpCmd(telegramClient, reservations);
        commands.put("/help", help);
        commands.put("/start", help);

        this.unknownCmd = new UnknownCmd(telegramClient, reservations);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return; // ignore non-text updates
        }
        Message message = update.getMessage();
        String text = message.getText().trim();
        String chatId = message.getChatId().toString();
        String userHandle = resolveHandle(message);

        // first token, with any "@botname" suffix stripped, lower-cased
        // e.g. "/rooms@MyBot 13L" -> "/rooms"
        String key = text.split("\\s+")[0].split("@")[0].toLowerCase();
        Cmd handler = commands.getOrDefault(key, unknownCmd);
        handler.execute(chatId, userHandle, text);
    }

    private static String resolveHandle(Message message) {
        if (message.getFrom() != null && message.getFrom().getUserName() != null) {
            return "@" + message.getFrom().getUserName();
        }
        return "unknown";
    }
}