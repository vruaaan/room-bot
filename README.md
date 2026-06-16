# Room Reservation Bot

A Telegram bot for creating and viewing room reservations, built with Java 17,
the Telegram Bots API, and Firebase Firestore.

## Features

- Create room bookings with venue conflict detection
- View bookings by venue
- View bookings by date, today, or tomorrow
- View bookings created by your Telegram username
- Basic fallback handling for unknown commands

Reminders are intentionally not implemented in the current version.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Telegram API | telegrambots-longpolling / telegrambots-client 8.0.0 |
| Database | Firebase Firestore via Firebase Admin SDK |
| Build | Maven + maven-shade-plugin |
| Config | java-dotenv |

## Project Structure

```text
src/main/java/com/roombot/
|-- Main.java                    # Loads config, initializes Firebase, starts Telegram long polling
|-- Bot.java                     # Routes incoming Telegram messages to command handlers
|
|-- commands/
|   |-- Cmd.java                 # Base command class with send and argument helpers
|   |-- BookCmd.java             # /book - create a booking
|   |-- RoomsCmd.java            # /rooms - bookings for a venue
|   |-- DateCmd.java             # /date - bookings for a date
|   |-- TodayCmd.java            # /tdy - today's bookings
|   |-- TmrCmd.java              # /tmr - tomorrow's bookings
|   |-- MineCmd.java             # /mybookings - bookings made by the current Telegram user
|   |-- HelpCmd.java             # /help and /start
|   `-- UnknownCmd.java          # Fallback for unrecognized commands
|
|-- model/
|   `-- Reservation.java         # Reservation domain object and Firestore payload conversion
|
|-- service/
|   |-- FirestoreSvc.java        # Low-level Firestore save/find/delete helpers
|   `-- ReservationSvc.java      # Reservation queries and conflict checks
|
|-- firebase/
|   `-- FirebaseConfig.java      # Firebase Admin SDK initialization
|
`-- util/
    |-- ParseDate.java           # Date parsing
    |-- ParseTime.java           # Time parsing
    |-- ParseVenue.java          # Venue parsing and canonical venue names
    `-- ParseMessage.java        # Formats outgoing bot messages
```

## Prerequisites

- Java 17+
- Maven 3.8+
- A Telegram bot token from BotFather
- A Firebase project with Firestore enabled
- A Firebase service account key saved as `firebaseaccount.json` in the project root

## Setup

### 1. Add environment variables

Copy `.env.example` to `.env` and fill in your Telegram bot token:

```bash
cp .env.example .env
```

```env
BOT_TOKEN=your_telegram_bot_token_here
VENUES=12L:12,12l,12 lounge;13L:13,13l,13 lounge
```

`VENUES` uses this format:

```text
CanonicalName:alias,alias,alias;OtherCanonicalName:alias,alias,alias
```

The canonical name is what gets saved in Firestore. Each alias is a user-facing
input that the bot will accept.

### 2. Add Firebase credentials

Download a Firebase service account key and save it in the project root:

```text
firebaseaccount.json
```

Do not commit `.env` or `firebaseaccount.json`.

### 3. Build

```bash
mvn clean package
```

### 4. Run

```bash
java -jar target/telegram-reminder-bot-1.0-SNAPSHOT.jar
```

## Bot Commands

| Command | Description |
|---|---|
| `/book <venue> <date> <start> <end>` | Create a booking if it does not clash with an existing booking for that venue |
| `/rooms <venue>` | Show bookings for a venue |
| `/date <date>` | Show bookings for a date |
| `/tdy` | Show today's bookings |
| `/tmr` | Show tomorrow's bookings |
| `/mybookings` | Show bookings made by your Telegram username |
| `/help`, `/start` | Show the help message |

There are no interactive menus or reply prompts in the current implementation.
Commands must include their required arguments directly.

## Booking Format

```text
/book <venue> <date> <start> <end>
```

Examples:

```text
/book 13L tomorrow 2pm 4pm
/book 13L mon 0900 1030
/book study room next fri 14:00 17:00
```

The bot canonicalizes recognized venue aliases before saving bookings. For
example, `13`, `13l`, and `13 lounge` are saved as `13L`.

## Supported Venues

Venues are configured through the `VENUES` environment variable. The repository
includes this default example in `.env.example`:

```env
VENUES=12L:12,12l,12 lounge,level 12 lounge,lvl 12 lounge;13L:13,13l,13 lounge,level 13 lounge,lvl 13 lounge;14L:14,14l,14 lounge,level 14 lounge,lvl 14 lounge;StudyRoom:study room,12 study room,level 12 study room,12 study rm,level 12 study rm
```

To add a new venue, append another semicolon-separated entry:

```env
VENUES=12L:12,12l;13L:13,13l;MusicRoom:music room,music rm,mr
```

## Supported Dates

Accepted natural-language dates include:

```text
today
tdy
tonight
tomorrow
tmr
tmrw
mon
monday
next fri
next next mon
15 jun
jun 15
```

Numeric dates are parsed as day-month-year, not year-month-day:

```text
15-06-2026
15062026
15/06/26
```

## Supported Times

Accepted time formats include:

```text
3pm
3:30pm
15:30
1530
0900
noon
midnight
```

End time must be after start time. Bookings are currently same-day bookings.

## Firestore Data Model

Collection: `reservations`

```json
{
  "telehandle": "@username",
  "chatId": "123456789",
  "venue": "13L",
  "date_start": "2026-06-17",
  "time_start": "14:00",
  "date_end": "2026-06-17",
  "time_end": "17:00",
  "duration": 3.0,
  "createdAt": "<server timestamp>"
}
```

The document ID is generated by Firestore. Loaded documents are converted back
into `Reservation` objects with `Reservation.dbToRes`.

## Current Limitations

- No reminder scheduler or reminder messages
- No booking cancellation command
- No interactive venue buttons or ForceReply date prompts
- No test suite is currently included in the repository
