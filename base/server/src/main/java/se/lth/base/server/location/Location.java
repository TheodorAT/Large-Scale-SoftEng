package se.lth.base.server.location;

public class Location {

    // Calculate the haversine distance between two locations
    public static double calculateDistance(Location from, Location to) {

        double deltaLat = Math.toRadians(to.latitude - from.latitude);
        double deltaLon = Math.toRadians(to.longitude - from.longitude);

        double fromLatRadian = Math.toRadians(from.getLatitude());
        double toLatRadian = Math.toRadians(to.getLatitude());

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.pow(Math.sin(deltaLon / 2), 2) * Math.cos(fromLatRadian) * Math.cos(toLatRadian);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371 * c;

        return distance;
    }

    public static double calculateDistance(double fromLat, double fromLon, double toLat, double toLon) {
        return calculateDistance(new Location(0, "", "", fromLat, fromLon), new Location(0, "", "", toLat, toLon));
    }

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
