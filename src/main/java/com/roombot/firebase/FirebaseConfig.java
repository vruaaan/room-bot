package com.roombot.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig { // to initialise firebase SDK once and returns a firestore instance
    private static Firestore db;

    public static Firestore init() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("firebaseaccount.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
        return db;
    }

    public static Firestore getDb() {
        if (db == null) {
            throw new IllegalStateException("Firebase has not been initialised. Call FirebaseConfig.init() first.");
        }
        return db;
    }
}