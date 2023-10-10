package se.lth.base.server.trip;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public enum TripStatus {

    ACTIVE(1), CANCELLED(2), REQUESTED(3);

    public static class Names {
        public static final String ACTIVE = "ACTIVE";
        public static final String CANCELLED = "CANCELLED";
        public static final String REQUESTED = "REQUESTED";
    }

    private final int trip_status;

    /**
     * Constructs a new TripStatus enum constant with the specified trip status value.
     *
     * @param trip_status
     *            The integer value representing the trip status.
     */
    TripStatus(int trip_status) {
        this.trip_status = trip_status;
    }

    /**
     * Get the status of the trip
     * 
     * @return int of the status
     */
    public int getTripStatus() {
        return trip_status;
    }

    /**
     * Get all the current statuses
     * 
     */
    public static Set<TripStatus> ALL_TRIPSTATUSES = new LinkedHashSet<>(Arrays.asList(ACTIVE, CANCELLED, REQUESTED));

}
