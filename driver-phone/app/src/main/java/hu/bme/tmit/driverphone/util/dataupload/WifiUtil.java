package hu.bme.tmit.driverphone.util.dataupload;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtil {


    /**
     * Chexksa wether the devcice is connected to the WIFI newtwork.
     *
     * @param context Application Context.
     * @return true if the device is connecxted to the  WIFI
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo == null) //error getting wifiInfo
                return false;

            return wifiInfo.getNetworkId() != -1;
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }
}
