package se.lth.base.server.trip;

public class Trip {

    private final int id;
    private final int driverId;
    private final int fromLocationId;
    private final int toLocationId;
    private final long startTime;
    private final long endTime;
    private final int seatCapacity;

    public Trip(int id, int driverId, int fromLocationId, int toLocationId, long startTime, long endTime,
            int seatCapacity) {
        this.id = id;
        this.driverId = driverId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatCapacity = seatCapacity;
    }

    public int getId() {
        return id;
    }

    public int getDriverId() {
        return driverId;
    }

    public int getFromLocationId() {
        return fromLocationId;
    }

    public int getToLocationId() {
        return toLocationId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }
}
