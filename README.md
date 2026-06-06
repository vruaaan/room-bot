# Room Reservation Bot

A Telegram bot for managing room reservations and reminders, built with Java 17, the Telegram Bots API, and Firebase Firestore.

---

## Features

- Browse available rooms via Telegram commands
- Create and manage room reservations
- Automated reminders before reservation start times
- Persistent storage via Firebase Firestore

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Telegram API | telegrambots-longpolling 8.0.0 |
| Database | Firebase Firestore (Admin SDK 9.7.0) |
| Scheduler | Quartz Scheduler 2.5.0 |
| Build | Maven + maven-shade-plugin |
| Config | java-dotenv |

---

## Project Structure

```
src/main/java/com/bot/
├── Main.java                        # Entry point — starts bot and initialises services
├── Bot.java                         # Message router — receives and dispatches Telegram updates
│
├── commands/
│   ├── BookCmd.java                 # /book — to make a booking
│   ├── DateCmd.java                 # /date  — to check the bookings on a specified date
|   ├── MyBookingsCmd.java           # /mybookings - to check for user's own bookings
|   ├── RoomsCmd.java                # /rooms - to check the bookings of a specified venue 
│   ├── TodayCmd.java                # /tdy - to check the bookings made for today 
|   └── TmrCmd.java                  # /tmr - to check the bookings made for thurs
|
├── service/
│   ├── ReminderService.java         # Business logic for creating/fetching/deleting reminders (NOT DONE YET)
│   └── FireStoreService.java        # Low-level Firestore CRUD operations 
│
├── scheduler/
│   └── ReminderScheduler.java       # Quartz job — polls Firestore and sends due reminders(NOT DONE YET)
│
├── firebase/
│   └── FirebaseConfig.java          # Initialises Firebase Admin SDK, returns Firestore instance
│
└── util/
    ├── MessageUtils.java          # Formats outgoing Telegram messages
    └── ParseTimes.java            # Converts the time inside messages into a fixed format (NOT DONE YET)
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- A [Telegram Bot Token](https://core.telegram.org/bots#botfather) from @BotFather
- A Firebase project with Firestore enabled
- A Firebase service account key (`serviceAccountKey.json`)

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/telegram-reminder-bot.git
cd telegram-reminder-bot
```

### 2. Add your environment variables

Copy the example env file and fill in your bot token:

```bash
cp .env.example .env
```

`.env`:
```
BOT_TOKEN=your_telegram_bot_token_here
```

### 3. Add your Firebase service account key

Download `serviceAccountKey.json` from your Firebase project settings and place it in the project root:

```
Firebase Console → Project Settings → Service Accounts → Generate new private key
```

> ⚠️ **Never commit `.env` or `serviceAccountKey.json` to version control.**

### 4. Build the project

```bash
mvn clean package
```

### 5. Run the bot

```bash
java -jar target/telegram-reminder-bot-1.0-SNAPSHOT.jar
```

---

## Bot Commands

| Command | Description |
|---|---|
| `/seerooms` | List all available rooms |
| `/seedate <date>` | Check room availability for a specific date |
| `/reserve <room> <date> <time>` | Make a reservation *(planned)* |
| `/myreservations` | View your upcoming reservations *(planned)* |
| `/cancel <id>` | Cancel a reservation *(planned)* |

---

## Firestore Data Model

### Collection: `reservations`

```json
{
  "tele_handle" : "@vruaaan",
  "duration" : float,
  "venue" : "13L",
  "date_start": "2025-07-01",
  "time_start": "14:00",
  "date_end": "2025-07-01",
  "time_end": "17:00",
  "reminderSent": false,
  "createdAt": "timestamp",
  "id": "auto-generated",
  "chatId": "123456789",
}
```