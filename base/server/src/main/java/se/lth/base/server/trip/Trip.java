package se.lth.base.server.trip;

import java.sql.Timestamp;

public class Trip {

    private final int id;
    private final int driverId;
    private final int fromLocationId;
    private final int toLocationId;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final int seatCapacity;

    public Trip(int id, int driverId, int fromLocationId, int toLocationId, Timestamp startTime, Timestamp endTime,
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

    public Timestamp getStartTime() {
        return new Timestamp(startTime.getTime());
    }

    public Timestamp getEndTime() {
        return new Timestamp(endTime.getTime());
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }
}
