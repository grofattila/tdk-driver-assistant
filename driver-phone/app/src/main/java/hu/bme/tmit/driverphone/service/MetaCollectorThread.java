package hu.bme.tmit.driverphone.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import hu.bme.tmit.driverphone.model.MetaData;
import hu.bme.tmit.driverphone.util.datacollection.CSVWriter;

import static android.content.Context.SENSOR_SERVICE;
import static java.lang.Thread.sleep;

/**
 * Háttérben az adatgyűjtésért felelős szál.
 */
public class MetaCollectorThread implements Runnable, SensorEventListener {

    private final String TAG = this.getClass().toString();

    private Context context;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private boolean isRunning;
    private CSVWriter csvWriter;
    private LocationManager locationManager;
    private LocationListener locationListenerNetwork;
    private LocationListener locationListenerGPS;

    /**
     * Ennny időközönként gyűjt adatot (másodperc).
     */
    private int collectionInterval = 1;


    public MetaCollectorThread(Context context, String filePath) {
        this.context = context;
        isRunning = true;
        csvWriter = new CSVWriter(filePath);
        initLocationListener();
        initSensorListener();
    }

    private void initSensorListener() {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("MissingPermission")
    private void initLocationListener() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MetaData.setLocationGPS(location);
                Log.d(TAG, "gps: " + location.toString());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationListenerNetwork = new LocationListener() {

            public void onLocationChanged(Location location) {
                MetaData.setLocationNetwork(location);
                Log.d(TAG, "network: " + location.toString());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        MetaData.setLocationNetwork(locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        MetaData.setLocationGPS(locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        if (!running) {
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            sensorManager.unregisterListener(this);
        }
        isRunning = running;
    }

    public int getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(int collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public void run() {

        while (isRunning) {
            try {
                sleep(collectionInterval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Calc data for csv
            MetaData.setTime(System.currentTimeMillis());


            try {
                csvWriter.writeCSVLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            MetaData.setAccelerometerX(event.values[0]);
            MetaData.setAccelerometerY(event.values[1]);
            MetaData.setAccelerometerZ(event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            MetaData.setGyroscopeX(event.values[0]);
            MetaData.setGyroscopeY(event.values[1]);
            MetaData.setGyroscopeZ(event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
