import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    public static void saveUser(String username, String password, String role, String counter) throws Exception {
        String hashed = hash(password);
        String json = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"counter\":\"%s\"}",
            username,
            hashed,
            role,
            counter == null ? "" : counter
        );
        String path = "users/" + role.toLowerCase() + "/" + username;
        put(path, json);
    }

    public static String login(String username, String password) throws Exception {
        String hashed = hash(password);
        String[] roles = {"staff", "admin"};

        for (String r : roles) {
            String data = get("users/" + r);

            if (data != null && !data.equals("null") && !data.isEmpty()) {
                if (data.contains("\"username\":\"" + username + "\"") &&
                    (data.contains("\"password\":\"" + hashed + "\"") ||
                     data.contains("\"password\": \"" + hashed + "\""))) {
                    return r.toUpperCase();
                }
            }
        }
        return null;
    }

    public static void setStaffOnline(String username, int counter) throws Exception {
        String json = String.format(
            "{\"username\":\"%s\",\"counter\":%d,\"status\":\"Online\",\"loginTime\":\"%s\"}",
            username, counter, java.time.LocalDateTime.now().toString()
        );
        put("onlineStaff/" + username, json);
    }

    public static void setStaffOffline(String username) throws Exception {
        delete("onlineStaff/" + username);
    }

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
        put("tickets/" + ticketNo + "/status", "\"" + status + "\"");
        put("tickets/" + ticketNo + "/counter", String.valueOf(counter));
    }

    /** Returns rows for the User Management table: {Username, Role, Counter, Status} */
    public static List<Object[]> getAllUsers() throws Exception {
        List<Object[]> result = new ArrayList<>();

        // Collect currently online usernames
        Set<String> onlineUsers = new HashSet<>();
        String onlineJson = get("onlineStaff");
        if (onlineJson != null && !onlineJson.equals("null") && !onlineJson.trim().isEmpty() && !onlineJson.equals("{}")) {
            Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(onlineJson);
            while (m.find()) {
                onlineUsers.add(m.group(1));
            }
        }

        // Read both admin and staff nodes
        String[] roles = {"admin", "staff"};
        for (String rolePath : roles) {
            String data = get("users/" + rolePath);
            if (data == null || data.equals("null") || data.trim().isEmpty() || data.equals("{}")) {
                continue;
            }

            // Find each child object: "usernameKey" : { ... }
            Matcher keyMatcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(data);
            while (keyMatcher.find()) {
                String usernameKey = keyMatcher.group(1);
                int start = keyMatcher.end() - 1; // position of '{'
                int end = findMatchingBrace(data, start);
                if (end == -1) continue;

                String obj = data.substring(start, end + 1);

                String username = extractJsonString(obj, "username");
                if (username == null || username.isEmpty()) {
                    username = usernameKey;
                }

                String role = extractJsonString(obj, "role");
                if (role == null || role.isEmpty()) {
                    role = rolePath.toUpperCase();
                }

                String counter = extractJsonString(obj, "counter");
                if (counter == null || counter.trim().isEmpty()) {
                    counter = "-";
                }

                String status = onlineUsers.contains(username) ? "Online" : "Offline";

                result.add(new Object[]{username, role, counter, status});
            }
        }

        return result;
    }

    private static int findMatchingBrace(String s, int openPos) {
        int depth = 0;
        for (int i = openPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String extractJsonString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static List<java.util.Map<String, String>> getAllTickets() throws Exception {
    List<java.util.Map<String, String>> tickets = new ArrayList<>();
    String data = get("tickets");

    if (data == null || data.equals("null") || data.trim().isEmpty() || data.equals("{}")) {
        return tickets;
    }

    // Match each child: "TICKET_KEY" : { ... }
    Matcher keyMatcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(data);
    while (keyMatcher.find()) {
        String key = keyMatcher.group(1);
        int start = keyMatcher.end() - 1;
        int end = findMatchingBrace(data, start);
        if (end == -1) continue;

        String obj = data.substring(start, end + 1);

        java.util.Map<String, String> t = new java.util.HashMap<>();
        t.put("ticketNo",      extractJsonString(obj, "ticketNo") != null ? extractJsonString(obj, "ticketNo") : key);
        t.put("bookingRef",    nullToEmpty(extractJsonString(obj, "bookingRef")));
        t.put("name",          nullToEmpty(extractJsonString(obj, "name")));
        t.put("flightNo",      nullToEmpty(extractJsonString(obj, "flightNo")));
        t.put("baggage",       nullToEmpty(extractJsonString(obj, "baggage")));
        t.put("status",        nullToEmpty(extractJsonString(obj, "status")));
        t.put("checkInTime",   nullToEmpty(extractJsonString(obj, "checkInTime")));

        // counter is stored as number, so we need a different extractor
        String counterStr = extractJsonNumber(obj, "counter");
        t.put("counter", counterStr != null ? counterStr : "0");

        tickets.add(t);
    }

    // Sort by checkInTime (oldest first) so queue order is correct
    tickets.sort((a, b) -> {
        String t1 = a.get("checkInTime");
        String t2 = b.get("checkInTime");
        if (t1 == null) return 1;
        if (t2 == null) return -1;
        return t1.compareTo(t2);
    });

    return tickets;
    }

    /** Returns only tickets whose status is WAITING (already sorted by check-in time) */
    public static List<java.util.Map<String, String>> getWaitingTickets() throws Exception {
        List<java.util.Map<String, String>> all = getAllTickets();
        List<java.util.Map<String, String>> waiting = new ArrayList<>();
        for (java.util.Map<String, String> t : all) {
            if ("WAITING".equalsIgnoreCase(t.get("status"))) {
                waiting.add(t);
            }
        }
        return waiting;
    }

    /** Find a single ticket by ticket number (case-insensitive) */
    public static java.util.Map<String, String> getTicketByNumber(String ticketNo) throws Exception {
        if (ticketNo == null || ticketNo.trim().isEmpty()) return null;
        List<java.util.Map<String, String>> all = getAllTickets();
        for (java.util.Map<String, String> t : all) {
            if (ticketNo.equalsIgnoreCase(t.get("ticketNo"))) {
                return t;
            }
        }
        return null;
    }


    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String extractJsonNumber(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
}