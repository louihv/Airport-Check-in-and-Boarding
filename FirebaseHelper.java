import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.*;

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

    public static String[] login(String username, String password) throws Exception {
    String hashed = hash(password);
    String[] roles = {"staff", "admin"};

        for (String r : roles) {
            String data = get("users/" + r + "/" + username);

            if (data != null && !data.equals("null") && !data.isEmpty()) {
                if (data.contains("\"password\":\"" + hashed + "\"") ||
                    data.contains("\"password\": \"" + hashed + "\"")) {

                    // extract counter if present
                    String counter = "";
                    int idx = data.indexOf("\"counter\"");
                    if (idx != -1) {
                        int start = data.indexOf(':', idx) + 1;
                        int end = data.indexOf(',', start);
                        if (end == -1) end = data.indexOf('}', start);
                        if (end > start) {
                            counter = data.substring(start, end)
                                        .replace("\"", "")
                                        .trim();
                        }
                    }

                    setOnline(username, r, counter);

                    return new String[]{ r.toUpperCase(), counter };
                }
            }
        }
        return null;
    }

    public static void setOnline(String username, String role, String counter) throws Exception {
    int counterNum = 0;
    try {
        if (counter != null && !counter.trim().isEmpty()) {
            counterNum = Integer.parseInt(counter.trim());
        }
    } catch (NumberFormatException ignored) {}

    String json = String.format(
        "{\"username\":\"%s\",\"role\":\"%s\",\"counter\":%d,\"status\":\"Online\",\"loginTime\":\"%s\"}",
        username,
        role,
        counterNum,
        java.time.LocalDateTime.now().toString()
    );
    put("onlineStaff/" + username, json);
    }

    public static void setOffline(String username) throws Exception {
        delete("onlineStaff/" + username);
    }


    public static void saveBaggage(String bookingRef, String tagNo, double weight, String status) throws Exception {
    String json = String.format(
        "{\"bookingRef\":\"%s\",\"tagNo\":\"%s\",\"weight\":%.2f,\"status\":\"%s\",\"timestamp\":\"%s\"}",
        bookingRef,
        tagNo,
        weight,
        status,
        java.time.LocalDateTime.now().toString()
    );
    put("baggage/" + tagNo, json);

    String ticketsJson = get("tickets");
    if (ticketsJson != null && !ticketsJson.equals("null") && !ticketsJson.isEmpty()) {
        Matcher keyMatcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(ticketsJson);
        while (keyMatcher.find()) {
            String ticketKey = keyMatcher.group(1);
            int start = keyMatcher.end() - 1;
            int end = findMatchingBrace(ticketsJson, start);
            if (end == -1) continue;

            String obj = ticketsJson.substring(start, end + 1);
            String ref = extractJsonString(obj, "bookingRef");
            if (bookingRef.equalsIgnoreCase(ref)) {
                String baggageInfo = tagNo + " (" + String.format("%.2f", weight) + " kg)";
                put("tickets/" + ticketKey + "/baggage", "\"" + baggageInfo + "\"");
                break;
            }
        }
    }
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

    public static List<Object[]> getAllUsers() throws Exception {
        List<Object[]> result = new ArrayList<>();

        Set<String> onlineUsers = new HashSet<>();
        String onlineJson = get("onlineStaff");
        if (onlineJson != null && !onlineJson.equals("null") && !onlineJson.trim().isEmpty() && !onlineJson.equals("{}")) {
            Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(onlineJson);
            while (m.find()) {
                onlineUsers.add(m.group(1));
            }
        }

        String[] roles = {"admin", "staff"};
        for (String rolePath : roles) {
            String data = get("users/" + rolePath);
            if (data == null || data.equals("null") || data.trim().isEmpty() || data.equals("{}")) {
                continue;
            }

            Matcher keyMatcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{").matcher(data);
            while (keyMatcher.find()) {
                String usernameKey = keyMatcher.group(1);
                int start = keyMatcher.end() - 1;
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

    public static java.util.Map<String, String> getPassenger(String bookingRef) throws Exception {
        if (bookingRef == null || bookingRef.trim().isEmpty()) {
            return null;
        }

        String data = get("passengers/" + bookingRef.trim());

        if (data == null || data.equals("null") || data.trim().isEmpty() || data.equals("{}")) {
            return null;
        }

        java.util.Map<String, String> passenger = new java.util.HashMap<>();
        passenger.put("passengerName", nullToEmpty(extractJsonString(data, "passengerName")));
        passenger.put("flightId",      nullToEmpty(extractJsonString(data, "flightId")));
        passenger.put("airline",       nullToEmpty(extractJsonString(data, "airline")));
        passenger.put("departureAirport", nullToEmpty(extractJsonString(data, "departureAirport")));
        passenger.put("arrivalAirport",   nullToEmpty(extractJsonString(data, "arrivalAirport")));
        passenger.put("departureTime",    nullToEmpty(extractJsonString(data, "departureTime")));
        passenger.put("flightStatus",     nullToEmpty(extractJsonString(data, "flightStatus")));

        String duration = extractJsonNumber(data, "flightDurationMinutes");
        passenger.put("flightDurationMinutes", duration != null ? duration : "");

        String distance = extractJsonNumber(data, "distanceMiles");
        passenger.put("distanceMiles", distance != null ? distance : "");

        String price = extractJsonNumber(data, "priceUsd");
        if (price == null) {
            Pattern p = Pattern.compile("\"priceUsd\"\\s*:\\s*([0-9]+\\.?[0-9]*)");
            Matcher m = p.matcher(data);
            if (m.find()) price = m.group(1);
        }
        passenger.put("priceUsd", price != null ? price : "");

        return passenger;
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

            String counterStr = extractJsonNumber(obj, "counter");
            t.put("counter", counterStr != null ? counterStr : "0");

            tickets.add(t);
        }

        tickets.sort((a, b) -> {
            String t1 = a.get("checkInTime");
            String t2 = b.get("checkInTime");
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        });

        return tickets;
    }

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