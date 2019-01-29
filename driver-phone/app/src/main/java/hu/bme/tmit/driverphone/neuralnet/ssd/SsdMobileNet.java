package hu.bme.tmit.driverphone.neuralnet.ssd;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import hu.bme.tmit.driverphone.neuralnet.NeuralNet;
import hu.bme.tmit.driverphone.util.Usage.UsageInfo;

public class SsdMobileNet implements NeuralNet<SsdResult, SsdInput> {

    private static final String TAG = SsdMobileNet.class.getName();
    private Context context;


    public static final int INPUT_WIDTH = 300;
    public static final int INPUT_HEIGHT = 300;
    public static final int INPUT_DEPTH = 3;
    private static final long[] INPUT_SIZE = {1, 300, 300, 3};
    //private static final String SSD_MODEL_FILE = "file:///android_asset/ssd.pb";
    private static final String SSD_INPUT_NAME = "image_tensor";
    private static final String[] SSD_OUTPUT_NAMES = {"num_detections:0", "detection_classes:0", "detection_scores:0", "detection_boxes:0"};

    private SsdResult result;

    // TensorFlow natív függvények betöltése
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static byte[] MODEL_FILE;
    private static Graph graph = new Graph();
    private static Session session;

    public SsdMobileNet(String path, Context context) {
        this.context = context;
        InputStream in = null;
        Log.d(TAG, "SsdMobileNet: " + path);

        try {
            if (path.equals("ssd.pb")) {
                AssetManager am = context.getAssets();
                in = am.open(path);
            } else {
                in = new FileInputStream(new File(path));
            }

            if(in != null){
                graph = new Graph();
                MODEL_FILE = IOUtils.toByteArray(in);
                graph.importGraphDef(MODEL_FILE);
                session = new Session(graph);
            }else {
                Log.d(TAG, "SsdMobileNet: FATAL ERORR, NO MODEL FILE");
            }
            
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getInputSizeWidth() {
        return INPUT_WIDTH;
    }

    @Override
    public Integer getInputSizeHeight() {
        return INPUT_HEIGHT;
    }

    @Override
    public Integer getInputSizeDepth() {
        return INPUT_DEPTH;
    }

    @Override
    public long[] getInputSize() {
        return INPUT_SIZE;
    }


    @Override
    public SsdResult executeGraph(final byte[] byteImage) {
        if (session != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteImage);
            final Tensor image = Tensor.create(UInt8.class, new long[]{1, INPUT_WIDTH, INPUT_HEIGHT, INPUT_DEPTH}, byteBuffer);
            Log.d(TAG, "Tensor shape: " + Arrays.toString(image.shape()));

            long timeStart = System.currentTimeMillis();

            List<Tensor<?>> resultList = session.runner()
                    .feed(SSD_INPUT_NAME, image)
                    .fetch(SSD_OUTPUT_NAMES[0])
                    .fetch(SSD_OUTPUT_NAMES[1])
                    .fetch(SSD_OUTPUT_NAMES[2])
                    .fetch(SSD_OUTPUT_NAMES[3])
                    .run();

            calcSsdResult(resultList);

            final long inferenceTime = System.currentTimeMillis() - timeStart;
            UsageInfo.setInferenceTime(inferenceTime);
            UsageInfo.inferenceList.add((float)inferenceTime);
            Log.d(TAG, "Inference time: " + inferenceTime + " ms");
        }
        return result;
    }

    private void calcSsdResult(List<Tensor<?>> tensorList) {
        for (Tensor<?> t : tensorList) {
            Log.d(TAG, "Tensor shape: " + Arrays.toString(t.shape()));
        }

        result = new SsdResult(context);
        //num_detections
        float[] numberOfDetections;
        //detection_classes
        float[][] detectionClasses;
        //detection_scores
        float[][] detectionScores;
        //detection_boxes
        float[][][] detectionBoxes;

        numberOfDetections = tensorList.get(0).copyTo(new float[1]);
        detectionClasses = tensorList.get(1).copyTo(new float[1][100]);
        detectionScores = tensorList.get(2).copyTo(new float[1][100]);
        detectionBoxes = tensorList.get(3).copyTo(new float[1][100][4]);

        //num_detections
        result.setNumberOfDetections((int) numberOfDetections[0]);
        int[] detectionClassesTmp = new int[100];
        for (int i = 0; i < 100; i++) {
            detectionClassesTmp[i] = (int) detectionClasses[0][i];
        }

        //detection_classes
        result.setDetectionClasses(detectionClassesTmp);
        float[] detectionScoresTmp = new float[100];
        for (int i = 0; i < 100; i++) {
            detectionScoresTmp[i] = detectionScores[0][i];
        }

        //detection_scores
        result.setDetectionScores(detectionScoresTmp);

        //detection_boxes
        float[][] detectionBoxesTmp = new float[100][4];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 4; j++) {
                detectionBoxesTmp[i][j] = detectionBoxes[0][i][j];
            }
        }
        result.setDetectionBoxes(detectionBoxesTmp);

        Log.d(TAG, "calcSsdResult: scores: " + Arrays.toString(detectionScoresTmp));

    }
}
