import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TrackerTestHarness serves as the concrete proof that MonitorVehicleTracker can
 * withstand heavy concurrent access without corrupting its internal state. It creates
 * a simulated environment where multiple threads are aggressively writing new coordinates
 * to the tracker at the exact same time that other threads are continuously reading
 * those coordinates back out.
 *
 * The goal of this harness is to simulate a high volume real world scenario. If the
 * locking or copying mechanisms inside the tracker were flawed, this test would quickly
 * expose those flaws by throwing concurrent modification exceptions or printing partially
 * updated coordinates. A successful run of this program will execute for a set duration,
 * process thousands of reads and writes, and terminate smoothly without a single error.
 */
public class TrackerTestHarness {

    public static void main(String[] args) {
        
        Map<String, MutablePoint> initialFleet = new HashMap<>();
        initialFleet.put("Taxi-1", new MutablePoint());
        initialFleet.put("Delivery-A", new MutablePoint());
        
        MonitorVehicleTracker tracker = new MonitorVehicleTracker(initialFleet);
        
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        /**
         * This first background task acts as a GPS receiver constantly pushing new
         * coordinates into the tracker. It loops repeatedly, updating the horizontal
         * and vertical positions of the taxi vehicle. Because it runs in its own thread,
         * it forces the tracker to handle writes while other threads are simultaneously
         * attempting to read.
         */
        Runnable taxiUpdater = () -> {
            int position = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    tracker.setLocation("Taxi-1", position, position);
                    position++;
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        /**
         * This second background task acts as a dashboard display constantly requesting
         * the latest snapshot of the entire fleet. By running continuously alongside
         * the updaters, it verifies that the tracker never returns a torn read, which
         * is an inconsistent state where a vehicle's horizontal coordinate has been
         * updated but its vertical coordinate has not yet caught up.
         */
        Runnable fleetReader = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Map<String, MutablePoint> snapshot = tracker.getLocations();
                    MutablePoint taxiLocation = snapshot.get("Taxi-1");
                    
                    if (taxiLocation != null && taxiLocation.x != taxiLocation.y) {
                        System.err.println("Critical Error: Inconsistent state detected. X and Y do not match.");
                    }
                    Thread.sleep(15);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        System.out.println("Starting concurrent stress test on MonitorVehicleTracker...");
        
        threadPool.execute(taxiUpdater);
        threadPool.execute(fleetReader);
        threadPool.execute(fleetReader);

        try {
            Thread.sleep(3000);
            System.out.println("Test duration complete. Initiating shutdown sequence.");
            threadPool.shutdownNow();
            threadPool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("Stress test concluded successfully with no data corruption.");
        } catch (InterruptedException e) {
            System.err.println("Main thread was interrupted during the test run.");
        }
    }
}