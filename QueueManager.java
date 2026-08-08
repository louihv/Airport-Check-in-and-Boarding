import java.util.*;

public class QueueManager {
    private static QueueManager instance;
    private final List<Passenger> queueList = new ArrayList<>();
    private final Map<Integer, Passenger> activeCounters = new HashMap<>();
    private int ticketCounter = 100;
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

    public String generateTicketNumber() {
        ticketCounter++;
        return "Q-" + ticketCounter;
    }

    public boolean addPassenger(Passenger p) {
        queueList.add(p);
        notifyListeners();
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
        }
    }

    public void skipPassenger(int counterId) {
        Passenger p = activeCounters.get(counterId);
        if (p != null) {
            p.setStatus("SKIPPED");
            activeCounters.put(counterId, null);
            notifyListeners();
        }
    }

    public Passenger getServingPassenger(int counterId) {
        return activeCounters.get(counterId);
    }

    public Map<Integer, Passenger> getActiveCounters() {
        return activeCounters;
    }
}
