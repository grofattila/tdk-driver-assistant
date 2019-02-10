package hu.bme.tmit.driverphone.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import hu.bme.tmit.driverphone.util.dataupload.UploadFTP;
import hu.bme.tmit.driverphone.util.dataupload.WifiUtil;

/**
 * Videó és CSV FTP-re való feltöltéséért felelős szál.
 */
public class FtpUploadThread extends Thread {


    private Context context;

    private static final String TAG = FtpUploadThread.class.getName();
    private boolean isRunning;
    private File dir;

    public FtpUploadThread(Context context) {
        this.context = context;
        isRunning = true;
        dir = context.getExternalFilesDir(null);
    }

    @Override
    public void run() {
        for (File f : dir.listFiles()) {
            String name = f.getName();
            Log.d(TAG, name);

            try {
                if (WifiUtil.isWifiConnected(context)){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(((Activity) context), "Upload started...!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    boolean success = new UploadFTP(context).uploadVideo(f);
                    if(success){
                        final String fileName = f.getName();
                        Log.d(TAG, "deleting: " + f.getName());
                        f.delete();
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(((Activity) context), "Video uploaded and deleted locally: " + fileName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else
                        Log.d(TAG, f.getName() + " upload failed!");
                } else {
                    Log.d(TAG, "run: No wifi!!!");
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(((Activity) context), "No wifi connection!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(((Activity) context), "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, f.getName() + " upload failed!");
            }

        }


        isRunning = false;
    }

}
