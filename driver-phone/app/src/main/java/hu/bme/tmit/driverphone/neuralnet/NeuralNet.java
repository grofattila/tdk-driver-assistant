package hu.bme.tmit.driverphone.neuralnet;

/**
 * Neurális hálózat alapértelemezett metódusait gyűjtő interfész.
 * @param <T> Result Predikált objektum.
 * @param <E> Data Nyers bemeneti adat.
 */
public interface NeuralNet<T, E> {


    Integer getInputSizeWidth();
    Integer getInputSizeHeight();
    Integer getInputSizeDepth();

    /**
     * Bemeneti kép mérete.
     *
     * @return Bemeneti kép mérete.
     */
    long[] getInputSize();

    /**
     * Predikció számolása.
     * @param byteImage Bemeneti kép.
     * @return Predikált objektum.
     */
    T executeGraph(final byte[] byteImage);
}
