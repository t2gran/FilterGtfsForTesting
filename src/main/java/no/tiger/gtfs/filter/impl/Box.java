package no.tiger.gtfs.filter.impl;

import org.onebusaway.gtfs.model.Stop;

public class Box {
    private final String name;

    /** min latitude (Oslo: 60) */
    private final double minLat;

    /** min longitude (Oslo: 11) */
    private final double minLon;

    /** min latitude (Oslo: 60) */
    private final double maxLat;

    /** min longitude (Oslo: 11) */
    private final double maxLon;

    public Box(String name, double minLat, double minLon, double maxLat, double maxLon) {
        this.name = name;
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    boolean outside(Stop stop) {
        return !inside(stop);
    }

    boolean inside(Stop stop) {
        double lat = stop.getLat();
        double lon = stop.getLon();
        return between(lat, minLat, maxLat) && between(lon, minLon, maxLon);
    }

    @Override
    public String toString() {
        return String.format("%s [(%5.2f, %5.2f), (%5.2f, %5.2f)]", name, minLat, minLon, maxLat, maxLon);
    }

    private static boolean between(double v, double min, double max) {
        return min <= v && v < max;
    }
}
