package xyz.tanwb.airship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * dimen适配文件生成工具
 */
public final class DimenTool {

    private static final String DIMENPATH = "./library/src/main/res/values";
    private static final String DIMENNAME = "dimens.xml";
    private static final String DIMENVALUE = "%1$s-sw%2$sdp";
    private static final String LINEBREAK = "\n";
    private static final String LINEMARK = "\">";
    private static final float BASEWIDTH = 360;
    private static final int MAXDP = 1440;
    private static final int MAXSP = 128;

    private DimenTool() {
    }

    // public static void main(final String[] args) {
    // baseDim();
    // gen();
    // }

    public static void baseDim() {

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<resources>").append(LINEBREAK);
        for (int i = 0; i <= MAXDP; i++) {
            stringBuilder.append("    <dimen name=\"common_dp_" + i + LINEMARK + i + "dp</dimen>").append(LINEBREAK);
        }
        stringBuilder.append(LINEBREAK);
        for (int i = 0; i <= MAXSP; i++) {
            stringBuilder.append("    <dimen name=\"common_sp_" + i + LINEMARK + i + "sp</dimen>").append(LINEBREAK);
        }
        stringBuilder.append("</resources>").append(LINEBREAK);

        writeFile(DIMENPATH, stringBuilder.toString());
    }

    public static void gen() {

        final int[] widthDPs = new int[]{320, 360, 480};
        final List<StringBuilder> sbList = new ArrayList<>();
        BufferedReader reader = null;

        for (int widthDP : widthDPs) {
            sbList.add(new StringBuilder());
        }

        try {
            reader = new BufferedReader(new FileReader(new File(DIMENPATH + File.separator + DIMENNAME)));

            String tempString;

            while ((tempString = reader.readLine()) != null) {
                String dimenMatk = "</dimen>";
                if (tempString.contains(dimenMatk)) {
                    int valueStart = tempString.indexOf(">") + 1;
                    int valueEnd = tempString.lastIndexOf("<") - 2;
                    String start = tempString.substring(0, valueStart);
                    float num = Float.valueOf(tempString.substring(valueStart, valueEnd));
                    String end = tempString.substring(valueEnd);

                    for (int i = 0; i < widthDPs.length; i++) {
                        sbList.get(i).append(start).append(Math.round(num * widthDPs[i] / BASEWIDTH)).append(end).append(LINEBREAK);
                    }
                } else {
                    for (int i = 0; i < widthDPs.length; i++) {
                        sbList.get(i).append(tempString).append(LINEBREAK);
                    }
                }
            }
            reader.close();

            for (int i = 0; i < widthDPs.length; i++) {
                writeFile(String.format(DIMENVALUE, DIMENPATH, widthDPs[i]), sbList.get(i).toString());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }

    private static void writeFile(final String directory, final String text) {
        try {
            final File dfile = new File(directory);
            if (!dfile.exists()) {
                if (!dfile.mkdirs()) return;
            }
            final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(dfile, DIMENNAME))));
            out.println(text);
            out.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
