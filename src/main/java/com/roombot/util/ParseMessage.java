package com.roombot.util;
import com.roombot.model.Reservation;

import java.util.List;
import java.time.LocalDate;

public class ParseMessage {
    public static String parseBooked(String venue, String date, String timeStart, String timeEnd, String telehandle) { // used by /book
        return "venue: " + venue + "\n" +
            "date : " + date + "\n" +
            "time  : " + timeStart + " - " + timeEnd + "\n" +
            "poc: " + telehandle;
    }

    public static String parseMine(String telehandle, List<Reservation> resList) { // used by /mine
        if (resList.isEmpty()){
            return "You have no bookings made !";
        }
        else {
            return "Bookings made by " + telehandle + ":" + "\n" +
                ParseMessage.convResList(resList, false);
        }
    }

    public static String parseDate(LocalDate date, List<Reservation> resList) { // used by /date, /today, /tmr
        if (resList.isEmpty()){
            return "No Bookings made for " + date.toString();
        }
        else {
            return "Bookings made for " + date.toString() + ":" + "\n" + 
                ParseMessage.convResList(resList, true);
        }
    }

    public static String parseRoom(String venue, List<Reservation> resList){ // used by /room 
        if (resList.isEmpty()){
            return "No bookings made for " + venue;
        }
        else {
            return "Bookings made for " + venue + ":" + "\n" + 
                ParseMessage.convResList(resList, true);
        }
    }

    private static String convResList(List<Reservation> resList, Boolean showPOC){ // private helper
        if (showPOC) {
            return resList.stream().map(r -> r.toString() + " by " + r.getPOC() + "\n").reduce("", (x, y) -> x + y);
        }
        return resList.stream().map(r -> r.toString() + "\n").reduce("", (x, y) -> x + y);
    }


}