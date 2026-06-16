package com.roombot.util;

import java.time.LocalTime;
import java.time.LocalDate;

public class BookMessage {
    private final String userHandle;
    private final LocalDate date;
    private final LocalTime timeStart;
    private final LocalTime timeEnd;
    private final String venue;

    public BookMessage(String userHandle, LocalDate date, LocalTime timeStart, LocalTime timeEnd, String venue) {
        this.userHandle = userHandle;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.venue = venue;
    }

    @Override
    public String toString() {
    return "venue: " + venue + "\n" +
           "date : " + date + "\n" +
           "time  : " + timeStart + " - " + timeEnd + "\n" +
           "poc: " + userHandle;
    }
}