package com.example.root.ariel_trivia_app;

import com.google.common.hash.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIRequests {

    private String username, password, hostname, cookie;
    private int port;
    /**
     *
     * @param hostname The server hostname (e.g. localhost)
     * @param port The server port
     * @param username
     * @param password Password in SHA256
     */
    public APIRequests(String hostname, int port, String username, String password) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        APIRequests apiRequests = new APIRequests("localhost", 80, "abc", "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
        Trivia trivia = new Trivia();
        apiRequests.uploadTrivia(trivia);
    }

    public static void Test2() {
        String pass_sha256 = Hashing.sha256().hashString("ShlomiABC", StandardCharsets.UTF_8).toString();
        APIRequests.signup("localhost", 80, "ShlomiABC", pass_sha256);
    }

    //Works
    public static void Test1() {
        TriviaFilter filter = new TriviaFilter();
        filter.setDifficulty(4);
        filter.setDifficulty_o(TriviaFilter.Operator.gt);
        filter.setLabels(Arrays.asList("unix","maths"));

        APIRequests apiRequests = new APIRequests("localhost", 80, "abc", "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
        apiRequests.signin();
        apiRequests.requestTrivias(filter);
    }

    /**
     * Signs in and sets the cookie
     * @return True if success
     */
    public boolean signin() {
        Map<String, List<String>> form_data = new HashMap<>();

        form_data.put("username", Arrays.asList(username));
        form_data.put("password", Arrays.asList(password));

        OutputStream response = new ByteArrayOutputStream();

        try {
            int status_code = ServerConnector.POST(hostname, port,"signin", null, form_data, response, APICode.signin);
            String response_str = response.toString();
            System.out.println("=== signin Response Start ===");
            System.out.println(response_str);
            System.out.println("Status code: " + status_code);
            System.out.println("=== signin Response End ===");

            if(response == null || response_str.length() != 16) {
                System.err.println(response_str + "\n" + "Status code:" + status_code);
                return false;
            }
            cookie = response_str;
            System.out.println("Cookie set!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Singup.
     * @param hostname
     * @param port
     * @param username
     * @param password The password in SHA256.
     */
    public static void signup(String hostname, int port, String username, String password) {

        Map<String, List<String>> form_data = new HashMap<>();
        form_data.put("username", Arrays.asList(username));
        form_data.put("password", Arrays.asList(password));
        OutputStream out = new ByteArrayOutputStream();
        try {
            int status_code = ServerConnector.POST(hostname, port, "signup", null, form_data, out, APICode.signup);

            System.out.println("=== Signup Response Start ===");
            System.out.println(out.toString());

            System.out.println("Status code: " + status_code);
            System.out.println("=== Signup Response End ===");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * GET Method
     * @param filter
     * @return
     */
    public List<Trivia> requestTrivias(TriviaFilter filter) {
        List<Trivia> result = new ArrayList<>();

        try {
            Map<String, List<String>> param_data = new HashMap<>();

            int difficulty = filter.getDifficulty();
            TriviaFilter.Operator difficulty_o = filter.getDifficulty_o();

            int likes = filter.getLikes();
            TriviaFilter.Operator likes_o = filter.getLikes_o();

            List<String> labels = filter.getLabels();


            param_data.put("username", Arrays.asList(username));
            param_data.put("cookie", Arrays.asList(cookie));

            if(likes != -1 && likes_o != null) {
                param_data.put("likes", Arrays.asList(""+likes));
                param_data.put("likes_o", Arrays.asList(likes_o.name()));
            }


            if(difficulty != -1 && difficulty_o != null) {
                param_data.put("difficulty", Arrays.asList(""+difficulty));
                param_data.put("difficulty_o", Arrays.asList(difficulty_o.name()));
            }

            if(labels != null) {
                List<String> lbl_values = new ArrayList<>();
                for(String lbl : labels) {
                    lbl_values.add(lbl);
                }
                param_data.put("label", lbl_values);
            }

            OutputStream response = new ByteArrayOutputStream();
            int status_code = ServerConnector.GET(hostname, port, "trivias", param_data, response, APICode.getTrivias);

            System.out.println("=== requestTrivia Response Start ===");
            System.out.println(response.toString());

            System.out.println("Status code: " + status_code);
            System.out.println("=== requestTrivia Response End ===");

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Uploads to database trivia object
     * @param trivia
     */
    public void uploadTrivia(Trivia trivia) {
        uploadTrivia(trivia.toJson());
    }

    /**
     * Uploads to database trivia object
     * @param json
     */
    public void uploadTrivia(String json) {

        Map<String, List<String>> param_data = new HashMap<>();
        Map<String, List<String>> form_data = new HashMap<>();

        param_data.put("username", Arrays.asList(username));
        param_data.put("cookie", Arrays.asList(cookie));

        form_data.put("trivia", Arrays.asList(json));

        OutputStream out = new ByteArrayOutputStream();
        try {
            int status_code = ServerConnector.POST(hostname, port, "trivias", param_data, form_data, out, APICode.uploadTrivia);
            System.out.println("=== uploadTrivia Response Start ===");
            System.out.println(out.toString());
            System.out.println("Status code: " + status_code);
            System.out.println("=== uploadTrivia Response End ===");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}