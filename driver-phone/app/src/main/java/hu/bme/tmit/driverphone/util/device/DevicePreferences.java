package hu.bme.tmit.driverphone.util.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class DevicePreferences {

    private static final String TAG = DevicePreferences.class.getName();

    private static final String NEURAL_NET_LOCATION = "hu.bme.tmit.driverphone.neural_net_location";
    public static final String DEFAULT_NEURAL_NET_LOCATION = "ssd.pb";

    private SharedPreferences sharedPreferences;

    public DevicePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("hu.bme.tmit.driverphone", Context.MODE_PRIVATE);
    }

    public void setNeuralNetLocation(String neuralNetLocation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NEURAL_NET_LOCATION, neuralNetLocation);
        editor.apply();
    }

    public String getNeuralNetLocation() {
        String neuralNetLocation = sharedPreferences.getString(NEURAL_NET_LOCATION, "");
        Log.d(TAG, "getNeuralNetLocation: " + neuralNetLocation);
        if (neuralNetLocation == null || neuralNetLocation.equals("")){
            setNeuralNetLocation(DEFAULT_NEURAL_NET_LOCATION);
            neuralNetLocation = getNeuralNetLocation();
        }
        return neuralNetLocation;
    }
}
