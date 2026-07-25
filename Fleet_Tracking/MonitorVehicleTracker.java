import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MutablePoint stores the two dimensional coordinates of a single vehicle in the fleet.
 *
 * This class is written to be as simple as possible on purpose. It does not use any
 * locking or synchronization to protect its own fields, which means it is not safe to
 * use from more than one thread at a time on its own. In plain language, if two
 * different threads tried to read or change the x and y fields of the same
 * MutablePoint object at the exact same moment, the outcome could be unpredictable and
 * one thread could end up seeing a location that never actually existed, such as an
 * old horizontal position paired with a new vertical position.
 *
 * This lack of built in protection is intentional and is the whole point of the
 * exercise this class is part of. It is only ever considered safe because
 * MonitorVehicleTracker, defined further down in this file, controls every single
 * point of access to it and never allows a shared instance of it to escape into code
 * that is outside of that control.
 */
class MutablePoint {

    /** The vehicle's position measured along the horizontal axis. */
    public int x;

    /** The vehicle's position measured along the vertical axis. */
    public int y;

    /**
     * Creates a new point positioned at the origin, meaning both coordinates begin at
     * zero. This constructor is used the first time a vehicle is registered with the
     * tracker, before any real location has ever been reported for it.
     */
    public MutablePoint() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a brand new point object holding the same coordinates as an existing
     * point, without keeping any lasting connection to that original object. After
     * this constructor runs, changing the new object has no effect whatsoever on the
     * object it was copied from, and changing the original has no effect on the copy.
     *
     * This copying behaviour is the second pillar of the safety strategy used by
     * MonitorVehicleTracker. Locking prevents two threads from touching the tracker's
     * internal data at the same time, and copying prevents any outside thread from
     * ever holding a usable reference to that internal data in the first place.
     * Together those two ideas are what make the tracker safe even though this class,
     * on its own, is not.
     *
     * @param source the existing point whose coordinates should be duplicated
     */
    public MutablePoint(MutablePoint source) {
        this.x = source.x;
        this.y = source.y;
    }
}

/**
 * MonitorVehicleTracker records the current location of every vehicle in a fleet, such
 * as delivery trucks, taxis, or patrol cars, and allows that information to be read
 * and updated safely by many threads running at the same time. A typical use of this
 * class would involve one thread continuously redrawing vehicle positions on a screen
 * while separate background threads receive fresh GPS coordinates and write them into
 * the tracker, all happening at once.
 *
 * The approach used here is known as the Java monitor pattern. All of the mutable data
 * the tracker owns is kept in a single map, and every method that touches that map is
 * synchronized on the same lock, which in this case is the tracker object itself.
 * Because only one thread is ever allowed to be inside a synchronized method on a
 * given object at any one time, this guarantees that no two threads can read or write
 * the fleet's data at the same moment, which is exactly what prevents that data from
 * becoming corrupted or internally inconsistent.
 *
 * A second safety measure works alongside the locking. Whenever a caller asks the
 * tracker for location information, the tracker never hands back the actual objects it
 * stores internally. Instead it builds and returns brand new copies. This matters
 * because if the tracker ever returned a live reference to its own data, a caller
 * could quietly change that data from completely outside the lock, without the
 * tracker's knowledge, and corrupt the fleet's records without triggering any error at
 * all. This problem is commonly referred to as representation exposure, and returning
 * independent copies instead of the originals is the standard way of closing that gap.
 *
 * One consequence of always returning copies is worth understanding clearly, since it
 * is a deliberate design tradeoff rather than an accident. The map returned by
 * getLocations represents exactly what the fleet looked like at the precise moment the
 * call was made. If a vehicle moves a moment later, that change will not appear inside
 * a map that has already been handed back to a caller. Anyone who needs continuously
 * up to date information has to call getLocations again to receive a fresh snapshot.
 * This guarantees that whatever a caller is looking at is always internally
 * consistent, meaning it is never a mixture of some old data and some new data, even
 * though it may occasionally be slightly out of date.
 */
public class MonitorVehicleTracker {

    /**
     * The complete set of known vehicles and their current locations. This is the only
     * piece of mutable state the tracker owns, and every access to it, whether reading
     * or writing, must happen while the lock on this tracker object is held. Every
     * method in this class is built around honouring that single requirement.
     */
    private final Map<String, MutablePoint> locations;

    /**
     * Creates a new tracker that is pre-populated with an initial set of vehicles and
     * their starting locations.
     *
     * The map supplied by the caller is deep copied before it is stored, rather than
     * simply being kept by reference. This is done so that the caller cannot continue
     * to hold a usable connection to the tracker's internal data through the map they
     * originally passed in. Without this copy, the caller could modify their original
     * map after handing it to the constructor and silently change the tracker's
     * records from entirely outside any locking, which would defeat the purpose of
     * every synchronized method defined below.
     *
     * @param locations the initial fleet, mapping each vehicle's identifier to its
     *                   starting location
     */
    public MonitorVehicleTracker(Map<String, MutablePoint> locations) {
        this.locations = deepCopy(locations);
    }

    /**
     * Returns a snapshot of every vehicle's current location at the exact moment this
     * method is called.
     *
     * This method is synchronized so that the snapshot it builds cannot be disturbed
     * by another thread updating a vehicle's location while the copy is being made.
     * The map that is returned is an independent copy that has also been wrapped so
     * that it cannot be modified, meaning any attempt to change the returned map, or
     * any of its entries, has no effect whatsoever on the tracker's real internal
     * records.
     *
     * @return an unmodifiable snapshot of every vehicle location at the time of the
     *         call
     */
    public synchronized Map<String, MutablePoint> getLocations() {
        return Collections.unmodifiableMap(deepCopy(locations));
    }

    /**
     * Returns the current location of a single named vehicle.
     *
     * As with getLocations, this method is synchronized so that the value it reads can
     * never be caught midway through an update being performed by another thread, and
     * it returns a freshly built copy of the point rather than the tracker's own
     * internal object, so that the caller cannot accidentally or deliberately alter
     * the tracker's records simply by modifying the object they were handed.
     *
     * @param id the identifier of the vehicle whose location is being requested
     * @return a copy of that vehicle's current location, or null if no vehicle with
     *         that identifier is known to the tracker
     */
    public synchronized MutablePoint getLocation(String id) {
        MutablePoint location = locations.get(id);
        return (location == null) ? null : new MutablePoint(location);
    }

    /**
     * Updates the recorded location of a single named vehicle.
     *
     * This is the only method in the class that changes the tracker's internal data,
     * and it is synchronized for the same reason the reading methods are. Because the
     * lock is held for the entire update, both coordinates are written before any
     * other thread is allowed to read this vehicle's location, which guarantees that a
     * reader will always see either the vehicle's previous position or its fully
     * updated new position, and never an inconsistent mixture of the two.
     *
     * @param id the identifier of the vehicle being updated
     * @param x  the vehicle's new position along the horizontal axis
     * @param y  the vehicle's new position along the vertical axis
     * @throws IllegalArgumentException if no vehicle with the given identifier is
     *                                  currently known to the tracker
     */
    public synchronized void setLocation(String id, int x, int y) {
        MutablePoint location = locations.get(id);
        if (location == null) {
            throw new IllegalArgumentException("No such vehicle registered with id: " + id);
        }
        location.x = x;
        location.y = y;
    }

    /**
     * Builds a brand new map whose keys are the same vehicle identifiers found in the
     * map supplied, but whose values are newly created MutablePoint objects holding
     * the same coordinates, rather than the original point objects themselves.
     *
     * This method is the mechanism that makes the copying strategy described earlier
     * actually work in practice. It is used both by the constructor, to isolate the
     * tracker from the caller's original map, and by getLocations, to isolate a
     * returned snapshot from the tracker's live internal data. This method performs no
     * locking of its own, so it must only ever be called by code that is already
     * holding the lock on the tracker, which both of its callers in this class do.
     *
     * @param source the map to duplicate
     * @return a new map containing independent copies of every entry found in source
     */
    private static Map<String, MutablePoint> deepCopy(Map<String, MutablePoint> source) {
        Map<String, MutablePoint> result = new HashMap<>();
        for (String id : source.keySet()) {
            result.put(id, new MutablePoint(source.get(id)));
        }
        return result;
    }
}