package hu.bme.tmit.driverphone.util.datadownload;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Background Async Task to download file
 */
public class DownloadAsyncTask extends AsyncTask<Context, String, String> {

    private static final String TAG = DownloadAsyncTask.class.getName();

    private ProgressDialog progressDialog;
    private Context context;
    private boolean success = false;

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(Context... contexts) {
        int count;
        this.context = contexts[0];
        String urlString = null;
        Properties properties = new Properties();
        try {
            properties.load(contexts[0].getAssets().open("ftp.properties"));
            urlString = properties.getProperty("new_model");

            Log.d(TAG, "doInBackground: " + urlString);
        } catch (IOException e) {
            e.printStackTrace();
        }


        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(urlString);
        System.out.println(httpget.toString());
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpget);

            InputStream input = response.getEntity().getContent();
            OutputStream output = null;

            output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/model.pb");
            Log.d(TAG, "doInBackground: " + Environment.getExternalStorageDirectory().toString() + "/model.pb");
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            success = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(String file_url) {
        if (success)
            Toast.makeText(((Activity) context), "Download finished ->" + Environment.getExternalStorageDirectory().toString() + "/model.pb", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(((Activity) context), "Error", Toast.LENGTH_LONG).show();
    }
}