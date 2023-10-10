package se.lth.base.server.trip;

public class Trip {

    private final int id;
    private final int driverId;
    private final int fromLocationId;
    private final int toLocationId;
    private final long startTime;
    private final long endTime;
    private final int seatCapacity;
    private final int status_id; 

    public Trip(int id, int driverId, int fromLocationId, int toLocationId, long startTime, long endTime,
            int seatCapacity) {
        this.id = id;
        this.driverId = driverId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatCapacity = seatCapacity;
        this.status_id = TripStatus.ACTIVE.getTripStatus();
    }

    /**
     * Another Constructor where Status_Id can be set manually 
     * 
     * @param id
     * @param driverId
     * @param fromLocationId
     * @param toLocationId
     * @param startTime
     * @param endTime
     * @param seatCapacity
     * @param status_id
     */
    public Trip(int id, int driverId, int fromLocationId, int toLocationId, long startTime, long endTime,
            int seatCapacity, int status_id) {
        this.id = id;
        this.driverId = driverId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatCapacity = seatCapacity;
        this.status_id = status_id; 
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

    public int getStatus(){
        return status_id; 
    }
}