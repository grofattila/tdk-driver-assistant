package hu.bme.tmit.driverphone.model;

import android.location.Location;

public class MetaData {

    public static final String CSV_HEADER = "time;" +
            "locationNetworkLat;" +
            "locationNetworkLong" +
            "locationGPSLat;" +
            "locationGPSLong;" +
            "accelerometerX;" +
            "accelerometerY;" +
            "accelerometerZ;" +
            "gyroscopeX;" +
            "gyroscopeY;" +
            "gyroscopeZ;";

    private static Location locationNetwork;
    private static Location locationGPS;
    private static float velocity;
    private static long time;
    private static float accelerometerX;
    private static float accelerometerY;
    private static float accelerometerZ;
    private static float gyroscopeX;
    private static float gyroscopeY;
    private static float gyroscopeZ;

    public static Location getLocationNetwork() {
        return locationNetwork;
    }

    public static void setLocationNetwork(Location locationNetwork) {
        MetaData.locationNetwork = locationNetwork;
    }

    public static Location getLocationGPS() {
        return locationGPS;
    }

    public static void setLocationGPS(Location locationGPS) {
        MetaData.locationGPS = locationGPS;
    }

    public static float getVelocity() {
        return velocity;
    }

    public static void setVelocity(float velocity) {
        MetaData.velocity = velocity;
    }

    public static long getTime() {
        return time;
    }

    public static void setTime(long time) {
        MetaData.time = time;
    }

    public static float getAccelerometerX() {
        return accelerometerX;
    }

    public static void setAccelerometerX(float accelerometerX) {
        MetaData.accelerometerX = accelerometerX;
    }

    public static float getAccelerometerY() {
        return accelerometerY;
    }

    public static void setAccelerometerY(float accelerometerY) {
        MetaData.accelerometerY = accelerometerY;
    }

    public static float getAccelerometerZ() {
        return accelerometerZ;
    }

    public static void setAccelerometerZ(float accelerometerZ) {
        MetaData.accelerometerZ = accelerometerZ;
    }

    public static float getGyroscopeX() {
        return gyroscopeX;
    }

    public static void setGyroscopeX(float gyroscopeX) {
        MetaData.gyroscopeX = gyroscopeX;
    }

    public static float getGyroscopeY() {
        return gyroscopeY;
    }

    public static void setGyroscopeY(float gyroscopeY) {
        MetaData.gyroscopeY = gyroscopeY;
    }

    public static float getGyroscopeZ() {
        return gyroscopeZ;
    }

    public static void setGyroscopeZ(float gyroscopeZ) {
        MetaData.gyroscopeZ = gyroscopeZ;
    }
}
