package com.roombot.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreSvc { // for talking to firestore
    private final Firestore db;

    public FirestoreSvc(Firestore db) { // initialising 
        this.db = db;
    }

    public String save(String collection, Map<String, Object> data) 
            throws ExecutionException, InterruptedException { // for writing data into firestore
        DocumentReference ref = db.collection(collection).document();
        ref.set(data).get(); // .get() blocks until Firestore confirms
        return ref.getId(); // return the auto-generated document ID
    }

    public List<QueryDocumentSnapshot> findAll(String collection) // takes in a string to indicate the collection it is finding 
            throws ExecutionException, InterruptedException { // for reading data from firestore 
        return db.collection(collection) // finding collection 
                 .get().get() // after obtaining the collection, second .get unwraps it to get the value wrapped inside
                 .getDocuments(); // get all documents from the 
    }

    public List<QueryDocumentSnapshot> findWhere(String collection, String field, Object value) // takes in a string to indicate the collection, 
            throws ExecutionException, InterruptedException { // 
        return db.collection(collection) // finding collection 
                 .whereEqualTo(field, value) // filters condition
                 .get().get() // after obtaining documents, calls .get() again to unwrap it 
                 .getDocuments(); // get all documents from the filtered collection
    }

    public DocumentSnapshot findById(String collection, String documentId)
            throws ExecutionException, InterruptedException {
        return db.collection(collection) // find the 
                 .document(documentId)
                 .get().get();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    public void delete(String collection, String documentId) // takes in a string for the exact collection and documentId that it is looking for
            throws ExecutionException, InterruptedException { // deletes the document inside the specified collection with the specefic documentId
        db.collection(collection)
          .document(documentId)
          .delete().get();
    }
}