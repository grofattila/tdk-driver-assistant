package hu.bme.tmit.driverphone.neuralnet;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by atig on 05/03/18.
 */

public class ConvNetLite {

    private String modelFile = "mobilenet.tflite";
    private Interpreter tflite;

    private static final int INPUT_WIDTH = 128;
    private static final int INPUT_HEIGHT = 128;
    private static final int INPUT_DEEP = 3;
    private static final String INPUT_NODE = "input";
    private static final String OUTPUT_NODE = "MobilenetV1/Predictions/Reshape_1";
    private static final long[] INPUT_SIZE = {1, 128, 128, 3};


    public void init(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void runInference() {

        float[][][] in = new float[128][128][3];
        float out = 1;

        tflite.run(in, out);
    }


}
