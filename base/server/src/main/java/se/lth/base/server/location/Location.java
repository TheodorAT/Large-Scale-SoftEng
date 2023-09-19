package se.lth.base.server.location;

public class Location {

    private final int locationId;
    private final String name;
    private final double latitude;
    private final double longitude;

    public Location(int locationId, String name, double latitude, double longitude) {
        this.locationId = locationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
