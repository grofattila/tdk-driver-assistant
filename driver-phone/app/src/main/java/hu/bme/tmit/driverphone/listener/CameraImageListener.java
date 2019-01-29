package hu.bme.tmit.driverphone.listener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.tensorflow.types.UInt8;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import hu.bme.tmit.driverphone.neuralnet.NeuralNet;
import hu.bme.tmit.driverphone.neuralnet.ssd.Detection;
import hu.bme.tmit.driverphone.neuralnet.ssd.DetectionList;
import hu.bme.tmit.driverphone.neuralnet.ssd.SsdMobileNet;
import hu.bme.tmit.driverphone.neuralnet.ssd.SsdResult;
import hu.bme.tmit.driverphone.util.camera.ImageProcessing;
import hu.bme.tmit.driverphone.util.device.DevicePreferences;

/**
 * Created by atig on 12/3/17.
 */

public class CameraImageListener implements ImageReader.OnImageAvailableListener {

    private static NeuralNet neuralNet;

    private static boolean isNeuralNetworkReady = false;
    private final String TAG = getClass().toString();
    int[] pixels = new int[SsdMobileNet.INPUT_HEIGHT * SsdMobileNet.INPUT_WIDTH * SsdMobileNet.INPUT_DEPTH];
    byte[] inputPicture;
    private Context context;
    private boolean isProcessing;
    private Thread loadNeuralNetworkThread = new Thread(new Runnable() {
        public void run() {
            loadNeuralNetwork();
            isNeuralNetworkReady = true;
        }
    });
    private ImageProcessing imageProcessing;

    private Handler detectionHandler;
    private HandlerThread detectionThread;

    private DevicePreferences devicePreferences;
    private String neuralnetPath= null;


    public CameraImageListener(Context context) {
        this.context = context;
        devicePreferences = new DevicePreferences(context);
        neuralnetPath = devicePreferences.getNeuralNetLocation();
        loadNeuralNetworkThread.start();
        startUp();
    }

    private void startUp() {
        detectionThread = new HandlerThread("Classifier Background Thread");
        detectionThread.start();
        detectionHandler = new Handler(detectionThread.getLooper());
    }

    public void shutDown() {
        detectionThread.quitSafely();
        try {
            detectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        detectionThread = null;
        detectionHandler = null;
    }

    public void loadNeuralNetwork() {
        neuralNet = new SsdMobileNet(neuralnetPath,context.getApplicationContext());
        isNeuralNetworkReady = true;
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {

        Image image = imageReader.acquireNextImage();
        if (isProcessing || !isNeuralNetworkReady) {
            image.close();
            return;
        }

        if (image == null || isProcessing || !isNeuralNetworkReady)
            return;

        if (!isProcessing && isNeuralNetworkReady) {
            isProcessing = true;
            detectionHandler.post(new InferenceThread(image));

        }
    }

    class InferenceThread implements Runnable {

        Image image;

        InferenceThread(Image image) {
            this.image = image;
        }

        public void run() {
            try{
                Log.d(TAG, "image->  width: " + image.getWidth() +
                        ", height: " + image.getHeight());
            }catch (Exception e) {

            }

            long timeStart = System.currentTimeMillis();
            Bitmap bitMapImage = null;
            try {
                bitMapImage = new ImageProcessing().YUV_420_888_toRGB(image, 2976, 2976, context);
                Log.d(TAG, "bitMapImage->  width: " + bitMapImage.getWidth() +
                        ", height: " + bitMapImage.getHeight() +
                        ", byteCount: " + bitMapImage.getByteCount());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitMapImage == null) {
                Log.e(TAG, "errrror");
                isProcessing = false;
                image.close();
                return;
            }

            Matrix matrix = new Matrix();

            matrix.postRotate(+180);
            matrix.preScale(-1,1);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitMapImage, SsdMobileNet.INPUT_WIDTH, SsdMobileNet.INPUT_HEIGHT, true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            pixels = new int[SsdMobileNet.INPUT_WIDTH * SsdMobileNet.INPUT_WIDTH * SsdMobileNet.INPUT_DEPTH];
            inputPicture = new byte[SsdMobileNet.INPUT_WIDTH * SsdMobileNet.INPUT_WIDTH * SsdMobileNet.INPUT_DEPTH];
            rotatedBitmap.getPixels(pixels, 0, SsdMobileNet.INPUT_WIDTH, 0, 0, SsdMobileNet.INPUT_WIDTH, SsdMobileNet.INPUT_HEIGHT);

            isProcessing = false;
            if (image != null)
                image.close();

            runNeuralNet(rotatedBitmap);
        }
    }


    private void runNeuralNet(Bitmap image) {
        //saveBitampToFile(image);
        Log.d(TAG, "bitmap:  " +
                "\n byteCount: " + image.getByteCount() +
                "\n getAllocationByteCount: " + image.getAllocationByteCount() +
                "\n getRowBytes: " + image.getRowBytes() +
                "\n, height: " + image.getHeight() +
                "\n, width:  " + image.getWidth());

        ByteBuffer byteBufferTmp = ByteBuffer.allocate(300 * 300 * 4);
        image.copyPixelsToBuffer(byteBufferTmp);
        byte[] byteArrayTmp = byteBufferTmp.array();
        byte[] byteArray = new byte[300*300*3];

        int k= 0;
        for(int i = 0; i< 300; i++){
            for(int j = 0; j< 300; j++){
                int color = image.getPixel(i, j);
                byteArray[k++] = (byte) Color.red(color);
                byteArray[k++] = (byte) Color.green(color);
                byteArray[k++] = (byte) Color.blue(color);
            }
        }

        image.recycle();

        Log.d(TAG, "run: --------------------->");

        SsdResult res = (SsdResult) neuralNet.executeGraph(byteArray);

        if(res != null){
            List<Detection> detectionList = res.getDetection();
            DetectionList detections = new DetectionList(detectionList);
            Gson gson = new Gson();
            String json = gson.toJson(detections);

            Intent intent = new Intent();
            intent.setAction("detection");
            intent.putExtra("detectionListData", json);
            context.sendBroadcast(intent);
        }

    }

    private void saveToTxt(byte[] data){
        String filename = "test.txt";
        File sd = Environment.getExternalStorageDirectory();
        Log.d(TAG, "savetxt: " + sd.getAbsolutePath());
        File dest = new File(sd, filename);

        try {
            FileWriter out = new FileWriter(dest);
            out.write(Arrays.toString(data));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveBitampToFile(Bitmap bitmap) {
        String filename = "asaaa.png";
        File sd = Environment.getExternalStorageDirectory();
        Log.d(TAG, "saveBitampToFile: " + sd.getAbsolutePath());
        File dest = new File(sd, filename);

        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
