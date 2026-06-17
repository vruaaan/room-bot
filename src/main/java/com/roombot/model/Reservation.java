package com.roombot.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;


public class Reservation {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private String id; // Firestore document id (null until persisted/loaded)
    private final String teleHandle;
    private final String chatId;
    private final String venue;
    private final LocalDate dateStart;
    private final LocalTime timeStart;
    private final LocalDate dateEnd;
    private final LocalTime timeEnd;

    public Reservation(String teleHandle, String chatId, String venue, 
        LocalDate dateStart, LocalTime timeStart,
        LocalDate dateEnd, LocalTime timeEnd) {
        this.teleHandle = teleHandle;
        this.chatId = chatId;
        this.venue = venue;
        this.dateStart = dateStart;
        this.timeStart = timeStart;
        this.dateEnd = dateEnd;
        this.timeEnd = timeEnd;
    }

    public Reservation(String venue, LocalDate dateStart, LocalTime timeStart, 
                                    LocalDate dateEnd, LocalTime timeEnd) {
        this.teleHandle = "";
        this.chatId = "";
        this.venue = venue;
        this.dateStart = dateStart;
        this.timeStart = timeStart;
        this.dateEnd = dateEnd;
        this.timeEnd = timeEnd;
    }

    public String getVenue() {
        return venue;
    }

    public String getPOC() {
        return teleHandle;
    }

    public String getId() {
        return id;
    }

    private LocalDateTime startDateTime() { 
        return dateStart.atTime(timeStart); 
    }

    private LocalDateTime endDateTime() { 
        return dateEnd.atTime(timeEnd); 
    }

    private double getDurationHours() {
        return Duration.between(startDateTime(), endDateTime()).toMinutes() / 60.0;
    }

    public boolean clashing (Reservation other) { // to compare if 2 bookings are clashing
        return other.venue.equals(this.venue) && // same venue
            startDateTime().isBefore(other.endDateTime()) && // start before other ends
            other.startDateTime().isBefore(endDateTime()); // other starts before current ends
    }

    public Map<String, Object> toPayload() { // convert to Firestore payload
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("telehandle", teleHandle);
        m.put("chatId", chatId);
        m.put("venue", venue);
        m.put("date_start", dateStart.format(DATE_FMT));
        m.put("time_start", timeStart.format(TIME_FMT));
        m.put("date_end", dateEnd.format(DATE_FMT));
        m.put("time_end", timeEnd.format(TIME_FMT));
        m.put("duration", getDurationHours());
        m.put("createdAt", FieldValue.serverTimestamp());
        return m;
    }

    public static Reservation dbToRes(DocumentSnapshot doc) { // converts the data from firestore into a Reservation object
        Reservation r = new Reservation(
                doc.getString("telehandle"),
                doc.getString("chatId"),
                doc.getString("venue"),
                LocalDate.parse(doc.getString("date_start"), DATE_FMT),
                LocalTime.parse(doc.getString("time_start"), TIME_FMT),
                LocalDate.parse(doc.getString("date_end"), DATE_FMT),
                LocalTime.parse(doc.getString("time_end"), TIME_FMT));
        r.id = doc.getId();
        return r;
    }

    public boolean passed(LocalDateTime given) {
        return !endDateTime().isAfter(given);
    } 

    public String cancelString() {
        return "Booking for " + venue + " on " + dateStart + " (" + timeStart + " - " + timeEnd + ") cancelled";
    }

    @Override
    public String toString() {
        return venue + " booked on " + dateStart + ": " + timeStart + " - " + timeEnd;
    }   

    @Override 
    public boolean equals(Object other) {
        if (other instanceof Reservation res) {
            if (res.venue != this.venue) { // if the venues already not same
                return false; // not equal 
            }
            return res.dateStart == this.dateStart && 
                res.dateEnd == this.dateEnd && 
                res.timeStart == this.timeStart &&
                res.timeEnd == this.timeEnd;
        }
        return false;
    }

    

}
