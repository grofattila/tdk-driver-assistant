package hu.bme.tmit.driverphone.util.datacollection;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import hu.bme.tmit.driverphone.model.MetaData;

public class CSVWriter {
    private static final char DEFAULT_SEPARATOR = ';';
    private static String path;


    public CSVWriter(String filePath) {
        path = filePath;
    }

    public static void writeLine(List<String> values) {
        writeLine(values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(List<String> values, char separators) {
        writeLine(values, separators, ' ');
    }

    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static void writeLine(List<String> values, char separators, char customQuote) {

        boolean first = true;

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }

        sb.append("\n");

        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter out = null;
        try {

            fw = new FileWriter(path, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            out.append(sb.toString());
            out.close();

        } catch (IOException e) {
        } finally {
            if (out != null)
                out.close();
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
            }
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
            }
        }
    }

    public static String getPath() {
        return path;
    }

    public void writeCSVLine() throws IOException {
        File file = new File(getPath());
        if (!file.exists())
            writeLine(Arrays.asList(MetaData.CSV_HEADER), ';');

        writeLine(Arrays.asList(
                convertToString(MetaData.getTime()),
                convertToString(MetaData.getLocationNetwork() != null ? MetaData.getLocationNetwork().getLatitude() : ""),
                convertToString(MetaData.getLocationNetwork() != null ? MetaData.getLocationNetwork().getLongitude() : ""),
                convertToString(MetaData.getLocationGPS() != null ? MetaData.getLocationGPS().getLatitude() : ""),
                convertToString(MetaData.getLocationGPS() != null ? MetaData.getLocationGPS().getLongitude() : ""),
                convertToString(MetaData.getAccelerometerX()),
                convertToString(MetaData.getAccelerometerY()),
                convertToString(MetaData.getAccelerometerZ()),
                convertToString(MetaData.getGyroscopeX()),
                convertToString(MetaData.getGyroscopeY()),
                convertToString(MetaData.getGyroscopeZ())
        ), ';');

        Log.d("CSV", "csv is being written...");
    }

    private String convertToString(Object o) {
        if (o == null)
            return "";
        return String.valueOf(o);
    }
}
