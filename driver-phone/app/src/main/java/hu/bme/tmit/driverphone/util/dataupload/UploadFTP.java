package hu.bme.tmit.driverphone.util.dataupload;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class UploadFTP {

    private static final String TAG = UploadFTP.class.getName();

    private Context context;
    private FTPClient connection;
    private File dir;

    public UploadFTP(Context context){
        this.context = context;
        dir = context.getExternalFilesDir(null);
        File[] filesList = dir.listFiles();
        for(File f: filesList){
            Log.d(TAG, "file name: " + f.getName());
        }
    }

    public boolean uploadVideo(File f) throws IOException {
        connection = new FTPClient();

        Properties properties = new Properties();
        properties.load(context.getAssets().open("ftp.properties"));

        try {
            connection = new FTPClient();
            connection.connect(properties.getProperty("url"));

            if (connection.login(properties.getProperty("username"), properties.getProperty("password"))) {

                connection.enterLocalPassiveMode(); // important!
                connection.setFileType(FTP.BINARY_FILE_TYPE);

                FileInputStream in = new FileInputStream(f);
                Log.d(TAG, "uploadVideo: "+ f.getName());
                boolean result = connection.storeFile("/"+f.getName(), in);
                in.close();
                if (result) {
                    Log.v("upload result", "succeeded");
                    return true;
                }


                connection.logout();
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;

    }
}
