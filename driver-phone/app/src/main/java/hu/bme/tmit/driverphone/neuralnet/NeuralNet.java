package hu.bme.tmit.driverphone.neuralnet;

import hu.bme.tmit.driverphone.neuralnet.ssd.SsdResult;

/**
 *
 * @param <T> Result
 * @param <E> Data
 */
public interface NeuralNet<T, E> {

    Integer getInputSizeWidth();
    Integer getInputSizeHeight();
    Integer getInputSizeDepth();

    long[] getInputSize();

    T executeGraph(final byte[] byteImage);
}
