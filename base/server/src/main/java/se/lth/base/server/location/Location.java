package se.lth.base.server.location;

public class Location {

    private final int locationId;
    private final String municipality;
    private final String name;
    private final double latitude;
    private final double longitude;

    public Location(int locationId, String municipality, String name, double latitude, double longitude) {
        this.locationId = locationId;
        this.municipality = municipality;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getMunicipality() {
        return municipality;
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
