package com.roombot.service;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.roombot.model.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReservationSvc { // additional layer that utilises CRUD functions from FirestoreSvc
    public static final String COLLECTION = "reservations";
    private final FirestoreSvc store;

    public ReservationSvc(FirestoreSvc store) {
        this.store = store; // storing the database
    }

    public String create(Reservation reservation) // creates reservation inside firestore
            throws ExecutionException, InterruptedException {
        return store.save(COLLECTION, reservation.toPayload());
    }

    public void deletePast() // deletes bookings that are passed
            throws ExecutionException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> passed = findAll().stream().filter(x -> x.passed(now)).toList();
        for (Reservation reservation : passed) {
            store.delete(COLLECTION, reservation.getId());
        }
    }

    public void delete(String bookingId)
            throws ExecutionException, InterruptedException {
        store.delete(COLLECTION, bookingId);
    }

    public List<Reservation> findAll() // find all data
            throws ExecutionException, InterruptedException {
        return dbToRes(store.findAll(COLLECTION));
    }

    public List<Reservation> findByVenue(String venue) // find
            throws ExecutionException, InterruptedException {
        return dbToRes(store.findWhere(COLLECTION, "venue", venue));
    }

    public List<Reservation> findByDate(LocalDate date)
            throws ExecutionException, InterruptedException {
        return dbToRes(store.findWhere(COLLECTION, "date_start", date.toString()));
    }

    public List<Reservation> findByUser(String teleHandle)
            throws ExecutionException, InterruptedException {
        return dbToRes(store.findWhere(COLLECTION, "telehandle", teleHandle));
    }

    public boolean hasConflict(Reservation candidate)
            throws ExecutionException, InterruptedException {
        return findByVenue(candidate.getVenue()).stream().anyMatch(r -> candidate.clashing(r));
    }

    private static List<Reservation> dbToRes(List<QueryDocumentSnapshot> docs) {
        return docs.stream().map(d -> Reservation.dbToRes(d)).toList();
    }
}
