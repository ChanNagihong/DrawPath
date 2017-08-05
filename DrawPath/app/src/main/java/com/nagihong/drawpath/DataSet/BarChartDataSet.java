package com.nagihong.drawpath.DataSet;

import com.nagihong.drawpath.ColorList;
import com.nagihong.drawpath.Utils.FloatFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 2016/10/10.
 */

public class BarChartDataSet {

    private String Tag = BarChartDataSet.class.getSimpleName();

    private List<Data> mDatas;
    private Data mMaxData, mMinData;
    private float mBaseValue = Float.MIN_VALUE;

    public BarChartDataSet() {
        generateData();
    }

    private void generateData() {
        mDatas = new LinkedList<>();
        Random random = new Random();
        float maxValue = Float.MIN_VALUE, minValue = Float.MAX_VALUE;
        for (int i = 0; i < 50; i++) {
            Data data = new Data();
            data.setIndex(i);
            float value = random.nextFloat() * 100;
            data.setValue(value);
            int colorIndex = i % 25;
            data.setColor(ColorList.getColors().get(colorIndex));
            data.setSelectedColor(ColorList.getSelectedColors().get(colorIndex));
            mDatas.add(data);
            if (value > maxValue) {
                maxValue = value;
                mMaxData = data;
            } else if (value < minValue) {
                minValue = value;
                mMinData = data;
            }
        }
    }

    public List<Data> getDatas() {
        if (null == mDatas) {
            generateData();
        }
        return mDatas;
    }

    public int getCount() {
        return getDatas().size();
    }

    private Data getMaxData() {
        if (null == mMaxData) {
            generateData();
        }
        return mMaxData;
    }

    private Data getmMinData() {
        if (null == mMinData) {
            generateData();
        }
        return mMinData;
    }

    private float calcBaseValue() {
        if (Float.MIN_VALUE == mBaseValue) {
            float total = 0f;
            for (int i = 0; i < getCount(); i++) {
                total += getDatas().get(i).getValue();
            }
            mBaseValue = total / getCount();
        }
        return mBaseValue;
    }

    public float calcMaxVal() {
        return 120f;
    }

    private float calcMinVal() {
        return 0f;
    }

    private float calcBaseYAxisMarkIncrementVal() {
        return 20f;
    }

    private int calcBaseXAxisMarkIncrementVal() {
        return 10;
    }

    public float calcYAxisIncrementLength(int height) {
        return height / calcMaxVal();
    }

    public float calcXAxisIncrementLength(int width) {
        return width / (float) getCount();
    }

    private float calcBaseYAxisMarkIncrementLength(int height) {
        return calcBaseYAxisMarkIncrementVal() * calcYAxisIncrementLength(height);
    }

    private float calcBaseXAxisMarkIncrementLength(int width) {
        return calcBaseXAxisMarkIncrementVal() * calcXAxisIncrementLength(width);
    }

    public float calcYAxisMarkIncrementVal(float scale) {
        return calcBaseYAxisMarkIncrementVal() / convertYAxisScale(scale);
    }

    public int calcXAxisMarkIncrementVal(float scale) {
        if(scale > calcBaseXAxisMarkIncrementVal()) {
            scale = calcBaseXAxisMarkIncrementVal();
        }
        return (int) (calcBaseXAxisMarkIncrementVal() / scale);
    }

    private float calcYAxisMarkIncrementLength(int height, float scale) {
        return calcBaseYAxisMarkIncrementLength(height) / convertYAxisScale(scale);
    }

    private float calcXAxisMarkIncrementLength(int width, float scale) {
        return calcXAxisMarkIncrementVal(scale) * calcXAxisIncrementLength(width);
    }

    private int calcXAxisGridLinesCount(int width, float scale) {
        return getCount() / calcXAxisMarkIncrementVal(scale);
    }

    private int calcYAxisGridLinesCount(int height, float scale) {
        return (int) (height / calcYAxisMarkIncrementLength(height, scale));
    }

    private int convertYAxisScale(float scale) {
        int intScale = (int) scale;
        if (intScale == 1) {
            return intScale;
        }
        int rest = intScale % 2;
        return intScale - rest;
    }

    public float[] calcXAxisGridLinesX(int width, float scale) {
        float[] xs = new float[calcXAxisGridLinesCount(width, scale)];
        float incrementLength = calcXAxisMarkIncrementLength(width, scale);
        float current = 0f;
        for (int i = 0; i < xs.length; i++) {
            current = (i + 1) * incrementLength;
            xs[i] = current;
        }
        return xs;
    }

    public float[] calcYAxisGridLinesY(int height, float scale) {
        float[] ys = new float[calcYAxisGridLinesCount(height, scale)];
        float incrementLength = calcYAxisMarkIncrementLength(height, scale);
        float current = 0f;
        for (int i = 0; i < ys.length; i++) {
            current = (i + 1) * incrementLength;
            ys[i] = current;
        }
        return ys;
    }

    public void calcDataPoint(int width, int height) {
        float xIncrementLength = calcXAxisIncrementLength(width);
        float yIncrementLength = calcYAxisIncrementLength(height);
        float maxVal = calcMaxVal();
        for(int i=0; i<getCount(); i++) {
            Data data = getDatas().get(i);
            data.setX(xIncrementLength * data.getIndex());
            data.setY(yIncrementLength * (maxVal - data.getValue()));
            data.setFormattedValue(FloatFormatter.format(data.getValue()));
        }
    }

    public float calcRectanglePaddingLength(int width) {
        return calcXAxisIncrementLength(width) * 0.1f;
    }

}
