package hu.bme.tmit.driverphone.util.Usage;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import hu.bme.tmit.driverphone.util.datacollection.CSVWriter;

/**
 * Created by AtiG on 2017. 03. 22..
 */

public class UsageInfo {

    private static final String TAG = UsageInfo.class.getName();
    private static long startTime = 0;
    private long time;
    public static List<Float> inferenceList = new ArrayList<>();
    private static float inferenceTime = 0;
    private static float inferenceAverageTime = 0;
    private boolean topRowWritten = false;

    public UsageInfo() {
        startTime = System.currentTimeMillis();
    }

    public void calcInfo(String fileName) {
        calcTime();
        calcAverageInference();
        try {
            writeCSV(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calcAverageInference() {
        Log.d(TAG, "calcAverageInference: size: " + inferenceList.size());
        float sum = 0;
        for(float f: inferenceList) {
            sum+=f;
        }
        float average = sum/inferenceList.size();
        inferenceAverageTime = average;
    }

    private void calcTime() {
        time = System.currentTimeMillis() - startTime;
        time /= 1000;
    }

    public static float getInferenceTime() {
        return inferenceTime;
    }

    public static void setInferenceTime(float inferenceTimeF) {
        inferenceTime = inferenceTimeF;
    }

    public static float getInferenceAverageTime() {
        return inferenceAverageTime;
    }

    public static void setInferenceAverageTime(float inferenceAverageTimeF) {
        inferenceAverageTime = inferenceAverageTimeF;
    }

    public void writeCSV(String fileName) throws IOException {
        Log.d(TAG, "writeCSV: top ->" + fileName);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + fileName);
        if (!topRowWritten){
            CSVUtil.writeLine(fileName,Arrays.asList(CSVUtil.getCSVHeader()), ';');
            Log.d(TAG, "writeCSV: writing header");
            topRowWritten = true;
        }

        CSVUtil.writeLine(fileName,Arrays.asList(
                Long.toString(time),
                Float.toString(inferenceTime),
                Float.toString(inferenceAverageTime)
        ), ';');

        Log.d(TAG, Long.toString(time) + " , "
                + Float.toString(inferenceTime)+ " , "
                + Float.toString(inferenceAverageTime));
    }

}