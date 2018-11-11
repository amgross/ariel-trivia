package Handlers;


import com.google.common.hash.Hashing;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class SignupHandler extends MyMongoHandler implements HttpHandler {
    private static String uri;
    private enum rCode {
        usernameOrPasswordNull(1), alreadyRegistered(2), unknown(3), OK(200);

        private final int value;
        private rCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };

    public SignupHandler(String uri) {
        this.uri = uri;
    }

    /**
     * Handle the given request and generate an appropriate response.
     * See {@link HttpExchange} for a description of the steps
     * involved in handling an exchange.
     *
     * @param he the exchange containing the request from the
     *                 client and used to send the response
     * @throws NullPointerException if exchange is <code>null</code>
     */
    @Override
    public void handle(HttpExchange he) throws IOException {
        Map<String, Object> p = parseBodyQuery(he);
        String username = (String) p.get("username");
        String password = (String) p.get("password"); //Already encrypted

        if(username == null || password == null) {
            sendResponse(he, "Username or password is null", rCode.usernameOrPasswordNull.getValue());
            return;
        }

        if(isUserAlreadyRegistered(username)) {
            sendResponse(he, "Username already registered", rCode.alreadyRegistered.getValue());
            return;
        }

        Document doc = register_user(username, password);
        if(doc == null) {
            sendResponse(he, "Unknown error: insertion failed", rCode.unknown.getValue());
            return;
        }
        String user_id = (String) doc.get( "_id" );
        sendResponse(he, user_id, rCode.OK.getValue());
    }

    private boolean isUserAlreadyRegistered(String username) {
        MongoClientURI mongoClientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(mongoClientURI);
        MongoDatabase database = mongoClient.getDatabase("ariel-trivia");
        MongoCollection<Document> users_col = database.getCollection("users");

        Document doc = users_col.find(eq("username",username)).first();
        if(doc == null)
            return false;
        else
            return true;
    }


    /**
     *
     * @param username Username
     * @param password_sha256 The password of the user.
     * @return Document that was inserted. Null if failed.
     */
    private Document register_user(String username, String password_sha256) {
        try {
            long before = System.currentTimeMillis();
            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            MongoDatabase database = mongoClient.getDatabase("ariel-trivia");
            MongoCollection<Document> users_col = database.getCollection("users");


            Document doc = new Document();
            doc.put("username", username);
            doc.put("password", password_sha256);
            users_col.insertOne(doc);
            mongoClient.close();
            return doc;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }




}

