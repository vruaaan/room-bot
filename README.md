# Room Reservation Bot

A Telegram bot for creating and viewing room reservations, built with Java 17,
the Telegram Bots API, and Firebase Firestore.

## Features

- Create room bookings with venue conflict detection
- View bookings by venue
- View bookings by date, today, or tomorrow
- View bookings created by your Telegram username
- Basic fallback handling for unknown commands

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
├── Main.java                    # Loads config, initializes Firebase, starts Telegram long polling
├── Bot.java                     # Routes incoming Telegram messages to command handlers
|
├── commands/
|   ├── Cmd.java                 # Base command class with send and argument helpers
|   ├── BookCmd.java             # /book - create a booking
|   ├── CancelCmd.java           # /cancel - cancel a booking
|   ├── RoomsCmd.java            # /rooms - bookings for a venue
|   ├── DateCmd.java             # /date - bookings for a date
|   ├── TodayCmd.java            # /tdy - today's bookings
|   ├── TmrCmd.java              # /tmr - tomorrow's bookings
|   ├── MineCmd.java             # /mybookings - bookings made by the current Telegram user
|   ├── HelpCmd.java             # /help and /start
|   └── UnknownCmd.java          # Fallback for unrecognized commands
|
├── model/
|   └── Reservation.java         # Reservation domain object and Firestore payload conversion
|
├── service/
|   ├── FirestoreSvc.java        # Low-level Firestore save/find/delete helpers
|   └── ReservationSvc.java      # Reservation queries and conflict checks
|
├── firebase/
|   └── FirebaseConfig.java      # Firebase Admin SDK initialization
|
└── util/
    ├── ParseDate.java           # Date parsing
    ├── ParseTime.java           # Time parsing
    ├── ParseVenue.java          # Venue parsing — loads venues.json into an alias lookup
    └── ParseMessage.java        # Formats outgoing bot messages
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
```

### 2. Configure venues

Venues are configured through a `venues.json` file in the project root. This
file is gitignored since different deployments may need different rooms, so
each setup must create its own copy.

Copy the example file to get started:

```bash
cp venues.example.json venues.json
```

`venues.json` maps each canonical venue name to a list of accepted aliases:

```json
{
  "13L": ["13", "13l", "13 lounge", "level 13 lounge", "lvl 13 lounge"],
  "StudyRoom": ["study room", "12 study room", "level 12 study room"]
}
```

- The canonical name (the JSON key) is what gets saved in Firestore.
- Each string in the array is a user-facing input the bot will accept and normalise to that
canonical name — matching is case-insensitive.
- To add a new venue, add another key to the JSON file with the value as an array of recognised names for that venue 

```json
{
  "13L": ["13", "13l", "13 lounge"],
  "MusicRoom": ["music room", "music rm", "mr"]
}
```

If `venues.json` is missing or malformed, the bot starts normally but every
venue lookup will fail — `/book` and `/rooms` will report an invalid venue
until the file is fixed.

### 3. Add Firebase credentials

Download a Firebase service account key and save it in the project root:

```text
firebaseaccount.json
```

Do not commit `.env`, `firebaseaccount.json` and `venues.json`.

### 4. Build

```bash
mvn clean package
```

### 5. Run

```bash
java -jar target/telegram-reminder-bot-1.0-SNAPSHOT.jar
```

## Bot Commands

| Command | Description |
|---|---|
| `/book <venue> <date> <start> <end>` | Create a booking if it does not clash with an existing booking for that venue |
| `/cancel <venue> <date> <start> <end>` | Cancels a booking if it is made by the user |
| `/rooms <venue>` | Show bookings for a venue |
| `/date <date>` | Show bookings for a date |
| `/tdy` | Show today's bookings |
| `/tmr` | Show tomorrow's bookings |
| `/mine` | Show bookings made by your Telegram username |
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

Venues are configured through `venues.json` (see [Setup](#2-configure-venues)
above). The repository includes a reference set of venues and aliases in
`venues.example.json`:

```json
{
  "12L": ["12", "12l", "12 lounge", "level 12 lounge", "lvl 12 lounge"],
  "13L": ["13", "13l", "13 lounge", "level 13 lounge", "lvl 13 lounge"],
  "14L": ["14", "14l", "14 lounge", "level 14 lounge", "lvl 14 lounge"],
  "StudyRoom": ["study room", "12 study room", "level 12 study room", "12 study rm", "level 12 study rm"]
}
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
  "telehandle": "@vruaaan",
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




## Bot Responses 

### `/book`

#### Successful booking
```text
venue: 13L
date : 2026-06-18
time  : 14:00 - 16:00
poc: @vruaaan
```

#### Booking conflict
```text
This slot clashes with an existing booking.
```

### `/cancel`

#### Successful cancellation
```text
Booking for 13L on 2026-06-18 (14:00 - 16:00) cancelled
```

#### Cancel — no matching booking found
```text
No matching bookings found
```

#### Cancel — booking exists but belongs to someone else
```text
Booking found not made by you
```

### `/rooms` , `/date` , `tdy` , `/tmr`

#### If there are bookings made
```text
Bookings made for 2026-06-18:
13L booked on 2026-06-18: 14:00 - 16:00 by @vruaaan
14L booked on 2026-06-18: 09:00 - 10:30 by @harris
```

#### If there are no bookings made
```text
No Bookings made for 2026-06-18
```

### `/mine`

#### If there are boookings made
```text
Bookings made by @vruaaan:
13L booked on 2026-06-18: 14:00 - 16:00
```

#### If user has no bookings made:
```text
You have no bookings made !
```


### `/help` , `/start`
````markdown 
*Venue Booking Bot*
Book and manage room reservations right from Telegram.

*Commands*

/book <venue> <date> <start> <end>
Create a booking. Fails if it clashes with an existing one.
_e.g. /book 13L tomorrow 2pm 4pm_

/cancel <venue> <date> <start> <end>
Cancel a booking you made. Must match exactly, and you must be the one who made it.
_e.g. /cancel 13L tomorrow 2pm 4pm_

/rooms <venue>
View all bookings for a venue.
_e.g. /rooms 13L_

/date <date>
View all bookings for a specific date.
_e.g. /date next fri_

/tdy — view today's bookings
/tmr — view tomorrow's bookings
/mine — view your own upcoming bookings

/help, /start — show this message

*Accepted venues*
Defined in venues.json — ask your admin if you're unsure which names are recognised.

*Accepted dates*
today, tdy, tonight, tomorrow, tmr, mon, next fri, next next mon, 15 jun, jun 15, 15-06-2026, 15062026

*Accepted times*
2pm, 2:30pm, 14:00, 1400, noon, midnight

*Notes*
- Times must have an end after the start, or the booking is treated as overnight if the end time is earlier in the day.
- Numeric dates are read as day-month-year, not year-month-year.
- You can only cancel bookings made under your own Telegram username.
````

### General Error Messages

#### Invalid venue or date
```text
I couldn't understand the venue or date.
```

#### Invalid date only
```text
I couldn't understand the date
```

#### Invalid time format
```text
I couldn't understand the time. Try formats like 2pm, 14:00 or 1400.
```



### Unknown command
```text
Unknown command. Type /help to see what I can do.
```

### Internal error (e.g. Firestore unavailable)

```text
Something went wrong, please try again.
```




