import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class FirebaseHelper {

    private static final String DB_URL = "https://airport-queuing-system-default-rtdb.firebaseio.com/";

    public static String get(String path) throws Exception {
    URL url = new URL(DB_URL + path + ".json");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);

    int responseCode = conn.getResponseCode();
    InputStream stream = (responseCode >= 200 && responseCode < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();

    if (stream == null) {
        throw new IOException("HTTP " + responseCode + " with empty body");
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) sb.append(line);
    reader.close();
    conn.disconnect();

    String body = sb.toString();

    if (responseCode < 200 || responseCode >= 300) {
        throw new IOException("HTTP " + responseCode + " → " + body);
    }

    return body;
}

public static void put(String path, String json) throws Exception {
    URL url = new URL(DB_URL + path + ".json");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("PUT");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);

    try (OutputStream os = conn.getOutputStream()) {
        os.write(json.getBytes(StandardCharsets.UTF_8));
    }

    int responseCode = conn.getResponseCode();
    InputStream stream = (responseCode >= 200 && responseCode < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();

    String body = "";
    if (stream != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        body = sb.toString();
    }
    conn.disconnect();

    if (responseCode < 200 || responseCode >= 300) {
        throw new IOException("PUT failed HTTP " + responseCode + " → " + body);
    }
}

    public static void patch(String path, String json) throws Exception {
        URL url = new URL(DB_URL + path + ".json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST"); 
        // Better way for PATCH:
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        conn.getResponseCode();
        conn.disconnect();
    }

    public static void delete(String path) throws Exception {
        URL url = new URL(DB_URL + path + ".json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.getResponseCode();
        conn.disconnect();
    }

    //  PASSWORD HASHING 
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password; // fallback
        }
    }

    //  CONVENIENCE METHODS 

    // Register / Save user
    public static void saveUser(String username, String password, String role, String counter) throws Exception {
    String hashed = hash(password);
    String json = String.format(
        "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"counter\":\"%s\"}",
        username,
        hashed,
        role,
        counter == null ? "" : counter
    );

    // users/staff/staff01  or  users/admin/admin
    String path = "users/" + role.toLowerCase() + "/" + username;
    put(path, json);
}

    // Simple login check
   public static String login(String username, String password) throws Exception {
    String hashed = hash(password);
    String[] roles = {"staff", "admin"};

    for (String r : roles) {
        // Try the path that matches your current data
        String data = get("users/" + r);   // ← changed

        if (data != null && !data.equals("null") && !data.isEmpty()) {
            // Also check that the username inside the object matches
            if (data.contains("\"username\":\"" + username + "\"") &&
                (data.contains("\"password\":\"" + hashed + "\"") ||
                 data.contains("\"password\": \"" + hashed + "\""))) {
                return r.toUpperCase();
            }
        }
    }
    return null;
}

    // Set staff online
    public static void setStaffOnline(String username, int counter) throws Exception {
    String json = String.format(
        "{\"username\":\"%s\",\"counter\":%d,\"status\":\"Online\",\"loginTime\":\"%s\"}",
        username, counter, java.time.LocalDateTime.now().toString()
        );
        put("onlineStaff/" + username, json);
    }

    // Set staff offline
        public static void setStaffOffline(String username) throws Exception {
        delete("onlineStaff/" + username);
    }

    // Save a ticket / check-in record
    public static void saveTicket(Passenger p) throws Exception {
    String json = String.format(
        "{\"ticketNo\":\"%s\",\"bookingRef\":\"%s\",\"name\":\"%s\",\"flightNo\":\"%s\"," +
        "\"baggage\":\"%s\",\"status\":\"%s\",\"counter\":%d,\"checkInTime\":\"%s\"}",
        p.getTicketNumber(),
        p.getBookingRef(),
        p.getName(),
        p.getFlightNumber(),
        p.getBaggageInfo(),
        p.getStatus(),
        p.getAssignedCounter(),
        p.getCheckInTime().toString()
    );
    put("tickets/" + p.getTicketNumber(), json);
    }

    public static void updateTicketStatus(String ticketNo, String status, int counter) throws Exception {
        String json = String.format(
            "{\"status\":\"%s\",\"counter\":%d}",
            status, counter
        );
        // Using PUT on the whole object is safer for simple REST
        // For partial update you can also use PATCH if you prefer
        String existing = get("tickets/" + ticketNo);
        if (existing != null && !existing.equals("null")) {
            // simple approach: re-save with new status (you can improve later)
            // For now just overwrite status fields via a full save if needed
        }
        // Easiest reliable way for school project:
        put("tickets/" + ticketNo + "/status", "\"" + status + "\"");
        put("tickets/" + ticketNo + "/counter", String.valueOf(counter));
    }
}