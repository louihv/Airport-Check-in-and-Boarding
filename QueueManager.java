import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueManager {
    private static QueueManager instance;
    private final List<Passenger> queueList = new ArrayList<>();
    private final Map<Integer, Passenger> activeCounters = new HashMap<>();
    private int ticketCounter = 100;          
    private boolean counterLoaded = false;
    private final List<Runnable> updateListeners = new ArrayList<>();

    private QueueManager() {
        for (int i = 1; i <= 4; i++) {
            activeCounters.put(i, null);
        }
    }

    public static synchronized QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    public void addListener(Runnable listener) {
        updateListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable r : updateListeners) {
            r.run();
        }
    }

    private synchronized void ensureCounterLoaded() {
        if (counterLoaded) return;
        try {
            List<Map<String, String>> tickets = FirebaseHelper.getAllTickets();
            int max = 100;
            Pattern p = Pattern.compile("Q-(\\d+)", Pattern.CASE_INSENSITIVE);
            for (Map<String, String> t : tickets) {
                String no = t.get("ticketNo");
                if (no == null) continue;
                Matcher m = p.matcher(no.trim());
                if (m.find()) {
                    try {
                        int n = Integer.parseInt(m.group(1));
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) {}
                }
            }
            ticketCounter = max;
        } catch (Exception e) {
            System.err.println("Could not load ticket counter from Firebase, using local: " + e.getMessage());
        }
        counterLoaded = true;
    }

    public synchronized String generateTicketNumber() {
        ensureCounterLoaded();
        ticketCounter++;
        return "Q-" + ticketCounter;
    }

    public boolean addPassenger(Passenger p) {
        queueList.add(p);
        notifyListeners();

        new Thread(() -> {
            try {
                FirebaseHelper.saveTicket(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return true;
    }

    public List<Passenger> getAllPassengers() {
        return new ArrayList<>(queueList);
    }

    public List<Passenger> getWaitingQueue() {
        List<Passenger> waiting = new ArrayList<>();
        for (Passenger p : queueList) {
            if ("WAITING".equals(p.getStatus())) {
                waiting.add(p);
            }
        }
        return waiting;
    }

    public Passenger callNextPassenger(int counterId) {
        for (Passenger p : queueList) {
            if ("WAITING".equals(p.getStatus())) {
                p.setStatus("SERVING");
                p.setAssignedCounter(counterId);
                activeCounters.put(counterId, p);
                notifyListeners();

                new Thread(() -> {
                    try {
                        FirebaseHelper.updateTicketStatus(p.getTicketNumber(), "SERVING", counterId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                return p;
            }
        }
        return null;
    }

    public void completeService(int counterId) {
        Passenger p = activeCounters.get(counterId);
        if (p != null) {
            p.setStatus("COMPLETED");
            activeCounters.put(counterId, null);
            notifyListeners();

            new Thread(() -> {
                try {
                    FirebaseHelper.updateTicketStatus(p.getTicketNumber(), "COMPLETED", counterId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void skipPassenger(int counterId) {
        Passenger p = activeCounters.get(counterId);
        if (p != null) {
            p.setStatus("SKIPPED");
            activeCounters.put(counterId, null);
            notifyListeners();

            new Thread(() -> {
                try {
                    FirebaseHelper.updateTicketStatus(p.getTicketNumber(), "SKIPPED", counterId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public Passenger getServingPassenger(int counterId) {
        return activeCounters.get(counterId);
    }

    public Map<Integer, Passenger> getActiveCounters() {
        return activeCounters;
    }
}