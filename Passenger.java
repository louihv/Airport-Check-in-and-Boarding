import java.time.LocalDateTime;

public class Passenger {
    private String bookingRef;
    private String name;
    private String flightNumber;
    private String baggageInfo;
    private String ticketNumber;
    private String status; // "WAITING", "SERVING", "COMPLETED", "SKIPPED"
    private int assignedCounter;
    private LocalDateTime checkInTime;

    public Passenger(String bookingRef, String name, String flightNumber, String baggageInfo, String ticketNumber) {
        this.bookingRef = bookingRef;
        this.name = name;
        this.flightNumber = flightNumber;
        this.baggageInfo = baggageInfo;
        this.ticketNumber = ticketNumber;
        this.status = "WAITING";
        this.assignedCounter = 0;
        this.checkInTime = LocalDateTime.now();
    }

    public String getBookingRef() { return bookingRef; }
    public String getName() { return name; }
    public String getFlightNumber() { return flightNumber; }
    public String getBaggageInfo() { return baggageInfo; }
    public String getTicketNumber() { return ticketNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAssignedCounter() { return assignedCounter; }
    public void setAssignedCounter(int assignedCounter) { this.assignedCounter = assignedCounter; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
}
