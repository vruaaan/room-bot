# Room Reservation Bot

A Telegram bot for managing room reservations and reminders, built with Java 17, the Telegram Bots API, and Firebase Firestore.

---

## Features

- Browse bookings by venue (with a tappable venue menu)
- Check bookings by date (with a guided date prompt), today, or tomorrow
- Create room bookings with conflict detection
- View your own bookings
- Automated reminders before a booking starts

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Telegram API | telegrambots-longpolling / telegrambots-client 8.0.0 |
| Database | Firebase Firestore (Admin SDK 9.7.0) |
| Scheduler | Quartz Scheduler 2.5.0 |
| Build | Maven + maven-shade-plugin |
| Config | java-dotenv |

---

## Project Structure

```
src/main/java/com/roombot/
├── Main.java                      # entry point / composition root
│                                  #   loads .env → inits Firebase → builds services
│                                  #   → registers the bot → starts the reminder scheduler
├── Bot.java                       # update router — dispatches commands, ForceReply
│                                  #   replies, and inline-button (callback) taps
│
├── commands/
│   ├── Cmd.java                   # abstract base: TelegramClient + ReservationService
│   │                              #   + send / markup / callback helpers
│   ├── BookCmd.java               # /book — make a booking (with conflict check)
│   ├── RoomsCmd.java              # /rooms — bookings for a venue (inline venue menu)
│   ├── DateCmd.java               # /date — bookings on a date (ForceReply prompt)
│   ├── TodayCmd.java              # /tdy — today's bookings
│   ├── TmrCmd.java                # /tmr — tomorrow's bookings
│   ├── MyBookingsCmd.java         # /mybookings — your own bookings
│   ├── HelpCmd.java               # /help, /start — command list
│   └── UnknownCmd.java            # fallback for unrecognised input
│
├── model/
│   └── Reservation.java           # domain object <-> Firestore document
│
├── service/
│   ├── FirestoreSvc.java          # low-level Firestore CRUD (save/find/update/delete)
│   ├── ReservationService.java    # booking logic + venue conflict detection
│   └── ReminderService.java       # finds due reminders, marks them sent
│
├── scheduler/
│   ├── ReminderScheduler.java     # schedules the Quartz job (every minute)
│   └── ReminderJob.java           # the job: send due reminders, mark as sent
│
├── firebase/
│   └── FirebaseConfig.java        # initialises Firebase Admin SDK, returns Firestore
│
└── util/
    ├── MessageUtils.java          # formats outgoing Telegram messages
    ├── ConversationState.java     # in-memory "waiting for X from this chat" state
    ├── ParseDay.java              # natural-language date parsing (today/tmr/next mon/…)
    └── ParseTime.java             # time parsing (3pm / 15:30 / 1530 / noon …)
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- A [Telegram Bot Token](https://core.telegram.org/bots#botfather) from @BotFather
- A Firebase project with Firestore enabled
- A Firebase service account key (`firebaseaccount.json`)

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/room-bot.git
cd room-bot
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

Download the service account key from your Firebase project and save it in the project
root as `firebaseaccount.json`:

```
Firebase Console → Project Settings → Service Accounts → Generate new private key
```

> ⚠️ **Never commit `.env` or `firebaseaccount.json` to version control.**

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
| `/rooms [venue]` | Bookings for a venue. With no venue, shows a tappable menu to pick one. |
| `/date [date]` | Bookings on a date. With no date, prompts you to reply with one. |
| `/tdy` | Today's bookings |
| `/tmr` | Tomorrow's bookings |
| `/book <venue> <date> <start> <end>` | Make a booking (rejected if it clashes with an existing one) |
| `/mybookings` | Your own upcoming bookings (uses your Telegram @username) |
| `/help`, `/start` | Show the command list |

### Interactive flows

```
/date            → bot opens a reply box: "Which date?" → you type "tomorrow" → bookings
/date 2025-07-01 → answered directly, no prompt

/rooms           → bot shows venue buttons [13L][14L][15L][Hall][Seminar Room] → tap one → bookings
/rooms 13L       → answered directly, no menu
```

The venue menu list is hardcoded in `RoomsCmd` (`VENUES`).

### Booking format

```
/book <venue> <date> <start> <end>
```

Examples:
```
/book 13L tomorrow 2pm 4pm
/book 13L 2025-07-01 14:00 17:00
/book 13L mon 0900 1030
```

Accepted dates: natural language (`today`, `tmr`, `mon`, `next fri`) or ISO (`2025-07-01`).
Accepted times: `3pm`, `3:30pm`, `15:30`, `1530`, `noon`, `midnight`.

---

## Reminders

A Quartz job (`ReminderScheduler` + `ReminderJob`) runs every minute, finds bookings that
start within the lead window (default **15 minutes**, set in `Main`) and haven't been
reminded yet, sends the user a Telegram reminder, and marks the booking `reminderSent: true`.

---

## Firestore Data Model

### Collection: `reservations`

```json
{
  "tele_handle": "@vruaaan",
  "chatId": "123456789",
  "venue": "13L",
  "date_start": "2025-07-01",
  "time_start": "14:00",
  "date_end": "2025-07-01",
  "time_end": "17:00",
  "duration": 3.0,
  "reminderSent": false,
  "createdAt": "<server timestamp>"
}
```

The document ID is Firestore's auto-generated id (read back via `Reservation.fromSnapshot`).
