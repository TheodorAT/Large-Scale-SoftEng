package se.lth.base.server.tripPassenger;

/**
 * Links a trip with a passenger.
 * 
 * @author Anton Tingelholm
 */

public class TripPassenger {
    private final int tripId;
    private final int passengerId;

    /**
     * Constructor
     * 
     * @param tripId
     *            Id of trip
     * @param passengerId
     *            Id of passenger
     */

    public TripPassenger(int tripId, int passengerId) {
        this.tripId = tripId;
        this.passengerId = passengerId;
    }

    /**
     * This method returns the tripId
     * 
     * @return int tripId
     */
    public int getTripId() {
        return tripId;
    }

    /**
     * This method returns the passengerId
     * 
     * @return int passengerId
     */
    public int getPassengerId() {
        return passengerId;
    }

}
