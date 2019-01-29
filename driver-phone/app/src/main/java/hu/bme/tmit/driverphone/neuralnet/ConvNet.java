package hu.bme.tmit.driverphone.neuralnet;


import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by atig on 11/5/17.
 */

public class ConvNet extends TensorFlowInferenceInterface {

    public static final int INPUT_WIDTH = 400;
    public static final int INPUT_HEIGHT = 400;
    public static final int INPUT_DEEP = 3;
    private static final String MODEL_FILE = "file:///android_asset/ssd.pb";
    private static final String INPUT_NODE = "image_tensor";
    private static final String OUTPUT_NODE = "detection_classes";
    private static final long[] INPUT_SIZE = {1, 400, 400, 3};

    // TensorFlow natív függvények betöltése
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private byte[] OUTPUT_TENSOR = new byte[2];

    // Konstruktor
    public ConvNet(Context context) {
        super(context.getAssets(), MODEL_FILE);
    }

    // Predikció futtatása
    public byte[] runInference(byte[] pic) {
        OUTPUT_TENSOR = new byte[100000];

        long timeStart = System.currentTimeMillis();

        try {
            OUTPUT_TENSOR = new byte[100000];

            // Hálózat bemenetére adatok betöltése
            feed(ConvNet.INPUT_NODE, pic, INPUT_SIZE);
            // Predikció futtatása
            run(new String[]{ConvNet.OUTPUT_NODE}, true);
            // A predikció eredményének elkérése a hálózattól
            fetch(ConvNet.OUTPUT_NODE, OUTPUT_TENSOR);

            // Eredmények kiírása konzolra
            System.out.println("Result: " + OUTPUT_TENSOR[0] + " - " + OUTPUT_TENSOR[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final long inferenceTime = System.currentTimeMillis() - timeStart;
        System.out.println("ConvNet time: " + inferenceTime + " ms");

        return OUTPUT_TENSOR;
    }

}