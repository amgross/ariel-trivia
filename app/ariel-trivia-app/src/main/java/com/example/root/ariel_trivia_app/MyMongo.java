package com.example.root.ariel_trivia_app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.common.hash.Hashing;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * All public functions in this class are static AND are network tasks.
 */
public class MyMongo {
    private static String uri = "mongodb://Admin:Admin1@ds049219.mlab.com:49219/ariel-trivia";
    private static String TAG = "MyMongo";

    /**
     * Check if server is up or down.
     * @return True if the server works.
     */
    public static boolean isUp() {
        try {
            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            MongoDatabase database = mongoClient.getDatabase("ariel-trivia");
            MongoCollection<Document> d = database.getCollection("test");
            d.insertOne(new Document("arye document", "rooor"));
            mongoClient.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * @return Array of JSONObjects that represents trivias. null if error occured.
     */
    public static List<Document> getAllTrivias() {
        try {
            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            MongoDatabase database = mongoClient.getDatabase("ariel-trivia");
            MongoCollection<Document> d = database.getCollection("trivias");

            FindIterable<Document> itr = d.find();
            List<Document> lst = new ArrayList<>();
            itr.into(lst);

            mongoClient.close();

            return lst;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * In testing.
     * @param collection
     */
    public static void selectAllRecordsFromACollection(DBCollection collection)
    {
        DBCursor cursor = collection.find();
        while(cursor.hasNext())
        {
            System.out.println(cursor.next());
        }
    }

    /**
     *
     * @param username The email of the user. (The email = username)
     * @param password The password of the user. Unencrypted.
     * @return True if successful. False is failed.
     */
    public static boolean register_user(String username, String password) {
        String enc_pass = sha256(password);

        try {
            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            MongoDatabase database = mongoClient.getDatabase("ariel-trivia");
            MongoCollection<Document> users_col = database.getCollection("users");


            JSONObject doc = new JSONObject();
            doc.put("username", username);
            doc.put("password", enc_pass);

            users_col.insertOne(Document.parse(doc.toString()));

            mongoClient.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }


    }

    /**
     * Encrpy message using SHA 256 hashing algorithem
     * @param message The message to encrypt
     * @return Returns 256 byte encrypted message. Returns String for ease of use.
     */
    private static String sha256(String message) {
        return Hashing.sha256()
                .hashString(message, StandardCharsets.UTF_8)
                .toString();
    }


}
