package hu.bme.tmit.driverphone.neuralnet.ssd;

import java.util.List;

public class DetectionList {

    private List<Detection> detectionList = null;

    public DetectionList(List<Detection> detectionList) {
        this.detectionList = detectionList;
    }

    public List<Detection> getDetectionList() {
        return detectionList;
    }

    public void setDetectionList(List<Detection> detectionList) {
        this.detectionList = detectionList;
    }
}
