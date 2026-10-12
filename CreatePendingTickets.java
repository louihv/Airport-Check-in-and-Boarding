public class CreatePendingTickets {

    public static void main(String[] args) {
        try {
            FirebaseHelper.put("pendingTickets/Q-103",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1003\"," +
                "\"cabin\":\"Economy\"," +
                "\"checkInTime\":\"2026-10-12T08:35:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1003\"," +
                "\"name\":\"Maria Santos\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-103\"}");

            FirebaseHelper.put("pendingTickets/Q-104",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1004\"," +
                "\"cabin\":\"Business\"," +
                "\"checkInTime\":\"2026-10-12T08:40:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1004\"," +
                "\"name\":\"Jose Reyes\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-104\"}");

            FirebaseHelper.put("pendingTickets/Q-105",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1005\"," +
                "\"cabin\":\"Economy\"," +
                "\"checkInTime\":\"2026-10-12T08:45:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1005\"," +
                "\"name\":\"Ana Garcia\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-105\"}");

            FirebaseHelper.put("pendingTickets/Q-106",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1006\"," +
                "\"cabin\":\"Premium Economy\"," +
                "\"checkInTime\":\"2026-10-12T08:50:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1006\"," +
                "\"name\":\"Mark Mendoza\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-106\"}");

            FirebaseHelper.put("pendingTickets/Q-107",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1007\"," +
                "\"cabin\":\"Economy\"," +
                "\"checkInTime\":\"2026-10-12T08:55:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1007\"," +
                "\"name\":\"Sofia Cruz\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-107\"}");

            FirebaseHelper.put("pendingTickets/Q-108",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1008\"," +
                "\"cabin\":\"First Class\"," +
                "\"checkInTime\":\"2026-10-12T09:00:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1008\"," +
                "\"name\":\"Daniel Flores\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-108\"}");

            FirebaseHelper.put("pendingTickets/Q-109",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1009\"," +
                "\"cabin\":\"Economy\"," +
                "\"checkInTime\":\"2026-10-12T09:05:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1009\"," +
                "\"name\":\"Angela Ramos\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-109\"}");

            FirebaseHelper.put("pendingTickets/Q-110",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1010\"," +
                "\"cabin\":\"Premium Economy\"," +
                "\"checkInTime\":\"2026-10-12T09:10:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1010\"," +
                "\"name\":\"Gabriel Torres\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-110\"}");

            FirebaseHelper.put("pendingTickets/Q-111",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1011\"," +
                "\"cabin\":\"Economy\"," +
                "\"checkInTime\":\"2026-10-12T09:15:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1011\"," +
                "\"name\":\"Isabella Lim\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-111\"}");

            FirebaseHelper.put("pendingTickets/Q-112",
                "{\"baggage\":\"\",\"bookingRef\":\"PAX-1012\"," +
                "\"cabin\":\"Business\"," +
                "\"checkInTime\":\"2026-10-12T09:20:00\"," +
                "\"counter\":0,\"flightNo\":\"FL-1012\"," +
                "\"name\":\"Nathan Bautista\",\"servedBy\":\"\"," +
                "\"status\":\"PENDING\",\"ticketNo\":\"Q-112\"}");

            System.out.println("10 pendingTickets created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}