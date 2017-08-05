package com.nagihong.drawpath.Utils;

import java.text.DecimalFormat;

/**
 * Created by channagihong on 16/8/18.
 */
public class FloatFormatter {

    private float round(float before, int decimalCount) {
        String after = String.format("%.2f");
        return Float.valueOf(after).floatValue();
    }

    public static String format(float before) {
        DecimalFormat df = new DecimalFormat("#######.##");
        return df.format(before);
    }

}
