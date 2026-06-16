package com.roombot;

import com.google.cloud.firestore.Firestore;
import com.roombot.firebase.FirebaseConfig;
import com.roombot.service.FirestoreSvc;
import com.roombot.service.ReservationSvc;

import io.github.cdimascio.dotenv.Dotenv;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main { // entry point — composition root, loads config, wires services, registers the bot, starts the reminder scheduler
    public static void main(String[] args) throws Exception {
        // 1. config
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN is missing. Add it to your .env file.");
        }

        // 2. firebase + data layer
        Firestore db = FirebaseConfig.init();
        FirestoreSvc store = new FirestoreSvc(db);
        ReservationSvc reservations = new ReservationSvc(store);

        // 3. telegram client (shared between the bot and the reminder scheduler)
        TelegramClient telegramClient = new OkHttpTelegramClient(botToken);

        // 4. bot
        Bot bot = new Bot(telegramClient, reservations);

        // 5. scheduled cleanup
        ScheduledExecutorService cleanupScheduler = startCleanupScheduler(reservations);

        // 6. start long polling and keep the process alive
        try (TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication()) {
            app.registerBot(botToken, bot);
            System.out.println("Bot running...");
            Thread.currentThread().join(); // block forever
        } finally {
            cleanupScheduler.shutdownNow();
        }
    }

    private static ScheduledExecutorService startCleanupScheduler(ReservationSvc reservations) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                reservations.deletePast();
                System.out.println("Deleted past reservations.");
            } catch (Exception e) {
                System.err.println("Failed to delete past reservations: " + e.getMessage());
            }
        }, 0, 12, TimeUnit.HOURS);
        return scheduler;
    }
}
