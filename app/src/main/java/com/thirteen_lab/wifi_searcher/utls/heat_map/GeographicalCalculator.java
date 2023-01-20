package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;


public class GeographicalCalculator {

    private static double EARTH_RADIUS = 6378137.0;

    public static class InMeters {
        public static double getNorthwardsDisplacement(Location location1, Location location2) {
            return
                    (location2.getLatitude() - location1.getLatitude()) *
                            EARTH_RADIUS * Math.PI / 180.0;
        }

        public static double getEastwardsDisplacement(Location location1, Location location2) {
            return
                    (location2.getLongitude() - location1.getLongitude()) *
                            Math.cos(Math.PI * location1.getLatitude() / 180.0) *
                            EARTH_RADIUS * Math.PI / 180.0;
        }
    }

}
