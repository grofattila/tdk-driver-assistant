package hu.bme.tmit.driverphone.neuralnet.ssd;

public class Detection {
    private String className;
    private float confidence;
    private float xmin;
    private float ymin;
    private float xmax;
    private float ymax;

    public Detection() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float getXmin() {
        return xmin;
    }

    public void setXmin(float xmin) {
        this.xmin = xmin;
    }

    public float getYmin() {
        return ymin;
    }

    public void setYmin(float ymin) {
        this.ymin = ymin;
    }

    public float getXmax() {
        return xmax;
    }

    public void setXmax(float xmax) {
        this.xmax = xmax;
    }

    public float getYmax() {
        return ymax;
    }

    public void setYmax(float ymax) {
        this.ymax = ymax;
    }

    @Override
    public String toString() {
        return "Detection{" +
                "className='" + className + '\'' +
                ", confidence=" + confidence +
                ", xmin=" + xmin +
                ", ymin=" + ymin +
                ", xmax=" + xmax +
                ", ymax=" + ymax +
                '}';
    }
}
