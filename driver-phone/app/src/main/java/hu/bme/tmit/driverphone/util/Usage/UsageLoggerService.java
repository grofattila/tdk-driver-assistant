package hu.bme.tmit.driverphone.util.Usage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class UsageLoggerService extends Service {

    private boolean isRunning  = false;

    private SensorManager sensorManager;
    private Sensor sensor;
    private String TAG = "UsageInfoLogger";
    private UsageInfo ui;
    private String fileName = "usage_data_" + System.currentTimeMillis() ;
    private final String  extension = ".csv";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        isRunning = true;
        ui = new UsageInfo();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart");
        isRunning = true;


        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning) {
                    Log.d(TAG, "run: new row");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ui.calcInfo( fileName+extension);
                }
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        isRunning = false;
    }


}
