package hu.bme.tmit.driverphone.util.Usage;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by AtiG on 2017. 03. 22..
 */

public class CSVUtil {

    private static final char DEFAULT_SEPARATOR = ';';
    private static final String CSVHeader= "Time;" +
            "Inference Time(ms);Inference Time average(ms)";
    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    private static final String TAG = CSVUtil.class.getName();

    public static void writeLine(String fileName, List<String> values) throws IOException {
        writeLine(fileName, values, DEFAULT_SEPARATOR , ' ');
    }

    public static void writeLine(String fileName,List<String> values, char separators) throws IOException {
        writeLine(fileName,values, separators, ' ');
    }

    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static void writeLine(String fileName, List<String> values, char separators, char customQuote) throws IOException {

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
            Log.d(TAG, "writeLine: " + path+fileName);
            fw = new FileWriter(path+fileName, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            out.append(sb.toString());
            out.close();

        } catch (IOException e) {
        }
        finally {
            if(out != null)
                out.close();
            try {
                if(bw != null)
                    bw.close();
            } catch (IOException e) {
            }
            try {
                if(fw != null)
                    fw.close();
            } catch (IOException e) {
            }
        }
    }

    public static String getPath() {
        return path;
    }

    public static String getCSVHeader() {
        return CSVHeader;
    }
}