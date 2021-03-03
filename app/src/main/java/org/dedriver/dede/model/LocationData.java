package org.dedriver.dede.model;

public class LocationData {
    private String uuid;
    private String label;
    private String alias;
    private String latitude;
    private String longitude;
    private String bearing;
    private String speed;
    private String vehicle;
    private long timestamp;

    public LocationData(String uuid, String label, String alias, String latitude,
                        String longitude, String bearing, String speed, String vehicle,
                        long timestamp) {
//        Timber.d("LocationData started");
        this.uuid = uuid;
        this.label = label;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
        this.speed = speed;
        this.vehicle = vehicle;
        this.timestamp = timestamp;
//        Timber.d("LocationData finished");
    }
}
