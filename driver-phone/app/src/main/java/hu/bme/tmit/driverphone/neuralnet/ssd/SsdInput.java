package hu.bme.tmit.driverphone.neuralnet.ssd;

public class SsdInput {

    private byte[] pic;

    public SsdInput(int width, int height, int depth) {
        pic = new byte[width*height*depth];
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }
}
