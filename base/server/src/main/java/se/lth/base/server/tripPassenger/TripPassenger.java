package se.lth.base.server.tripPassenger;

public class TripPassenger {
    private final int tripId;
    private final int passengerId;

    public TripPassenger(int tripId, int passengerId) {
        this.tripId = tripId;
        this.passengerId = passengerId;
    }

    public int getTripId() {
        return tripId;
    }

    public int getPassengerId() {
        return passengerId;
    }

}
