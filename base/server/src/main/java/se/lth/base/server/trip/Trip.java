package se.lth.base.server.trip;

import java.util.Date;


public class Trip {

    private final int id;
    private final int driverId;
    private final int fromLocationId;
    private final int toLocationId;
    private final Date startTime;
    private final Date endTime;
    private final int seatCapacity;

    public Trip(int id, int driverId, int fromLocationId, int toLocationId, Date startTime, Date endTime, int seatCapacity) {
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

    public int getDriverId(){
        return driverId;
    }

    public int getFromLocationId() {
        return fromLocationId;
    }
    
    public int getToLocationId() {
        return toLocationId;
    }
    
    public Date getStartTime() {
        return new Date(startTime.getTime());
    }
    
    public Date getEndTime() {
        return new Date(endTime.getTime());
    }
    
    public int getSeatCapacity() {
        return seatCapacity;
    }
}
