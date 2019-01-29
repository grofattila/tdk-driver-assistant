package hu.bme.tmit.driverphone.neuralnet.ssd;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SsdResult {

    private static final String TAG = SsdResult.class.getName();

    private Context context;

    private static final Integer BOX_NUM_VALUES = 4;
    private static final Double THRESHOLD = 0.25;

    private int numberOfDetections;
    private float[] detectionScores;
    private int[] detectionClasses;
    private float[][] detectionBoxes;

    public SsdResult(Context context) {
        numberOfDetections = 100;
        detectionScores = new float[numberOfDetections];
        detectionClasses = new int[numberOfDetections];
        detectionBoxes = new float[numberOfDetections][BOX_NUM_VALUES];
        this.context = context;
    }

    public int getNumberOfDetections() {
        return numberOfDetections;
    }

    public void setNumberOfDetections(int numberOfDetections) {
        this.numberOfDetections = numberOfDetections;
    }

    public float[] getDetectionScores() {
        return detectionScores;
    }

    public void setDetectionScores(float[] detectionScores) {
        this.detectionScores = detectionScores;
    }

    public int[] getDetectionClasses() {
        return detectionClasses;
    }

    public void setDetectionClasses(int[] detectionClasses) {
        this.detectionClasses = detectionClasses;
    }

    public float[][] getDetectionBoxes() {
        return detectionBoxes;
    }

    public void setDetectionBoxes(float[][] detectionBoxes) {
        this.detectionBoxes = detectionBoxes;
    }

    public String getLabelForClassId(Integer id, Context context) {
        Log.d(TAG, "getLabelForClassId: looking for: " + id);
        Properties properties = new Properties();
        try {
            properties.load(context.getAssets().open("label_map.properties"));
            return (String) properties.getProperty(String.valueOf(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Detection> getDetection() {
        List<Detection> detectionsList = new ArrayList<>();
        Detection detection;
        for (int i = 0; i < 100; i++) {
            if (detectionScores[i] > THRESHOLD) {
                Log.d(TAG, "Detection found!!!: index: " + i);
                detection = new Detection();
                detection.setClassName(getLabelForClassId(detectionClasses[i],context));
                detection.setConfidence(detectionScores[i]);
                detection.setYmin(detectionBoxes[i][0]);
                detection.setXmin(detectionBoxes[i][1]);
                detection.setYmax(detectionBoxes[i][2]);
                detection.setXmax(detectionBoxes[i][3]);
                detectionsList.add(detection);
                Log.d(TAG, "getDetection: detection: " + detection.toString());
            }
        }
        return detectionsList;
    }

    @Override
    public String toString() {
        return "SsdResult{" +
                "context=" + context +
                ", numberOfDetections=" + numberOfDetections +
                ", detectionScores=" + Arrays.toString(detectionScores) +
                ", detectionClasses=" + Arrays.toString(detectionClasses) +
                ", detectionBoxes=" + Arrays.toString(detectionBoxes) +
                '}';
    }
}
