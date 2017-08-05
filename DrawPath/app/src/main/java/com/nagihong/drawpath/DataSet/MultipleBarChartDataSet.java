package com.nagihong.drawpath.DataSet;

import android.util.Log;

import com.nagihong.drawpath.ColorList;
import com.nagihong.drawpath.Utils.FloatFormatter;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by channagihong on 2016/10/12.
 */

public class MultipleBarChartDataSet {

    private String Tag = MultipleBarChartDataSet.class.getSimpleName();

    private LinkedList<LinkedList<Data>> mDataSet;
    private int DATA_COUNT = 5, DATASET_COUNT = 3;
    private float mMaxValue = Float.MIN_VALUE;

    public MultipleBarChartDataSet() {
        generateData();
    }

    private void generateData() {
        mDataSet = new LinkedList<>();
        Random random = new Random();
        for (int i = 0; i < DATASET_COUNT; i++) {
            LinkedList<Data> datas = new LinkedList<>();
            for (int j = 0; j < DATA_COUNT; j++) {
                /**
                 * generate data
                 */
                Data data = new Data();
                data.setIndex(j);
                float value = random.nextFloat() * 100;
                data.setValue(value);
                datas.add(data);
            }
            mDataSet.add(datas);
        }
    }

    public LinkedList<LinkedList<Data>> getDataSet() {
        if (null == mDataSet) {
            generateData();
        }
        return mDataSet;
    }

    public int getCount() {
        return DATA_COUNT;
    }

    public int getDataSetCount() {
        return DATASET_COUNT;
    }

    public float calcMaxVal() {
        if (Float.MIN_VALUE == mMaxValue) {
            for (int i = 0; i < getDataSetCount(); i++) {
                for (int j = 0; j < getCount(); j++) {
                    float value = getDataSet().get(i).get(j).getValue();
                    if (mMaxValue < value) {
                        mMaxValue = value;
                    }
                }
            }
            mMaxValue *= 1.3f;
            if (mMaxValue > calcBaseYAxisMarkIncrementVal() * 2) {
                mMaxValue = mMaxValue - mMaxValue % calcBaseYAxisMarkIncrementVal();
            }
        }
        return mMaxValue;
    }

    public float calcMinVal() {
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

    public float calcXAxisDataSetIncrementLength(int width) {
        return calcXAxisIncrementLength(width) / getDataSetCount();
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
        if (scale > calcBaseXAxisMarkIncrementVal()) {
            scale = calcBaseXAxisMarkIncrementVal();
        }
        int incrementVal = (int) (calcBaseXAxisMarkIncrementVal() / scale);
        if(incrementVal > getCount()) {
            incrementVal = 1;
        }
        return incrementVal;
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
        int linesCount = calcXAxisGridLinesCount(width, scale);
        float incrementLength = calcXAxisMarkIncrementLength(width, scale);
        if(linesCount == 0) {
            linesCount = getCount();
            incrementLength = calcXAxisIncrementLength(width);
        }
        float[] xs = new float[linesCount];
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
        float xSetIncrementLength = xIncrementLength / getDataSetCount();
        float yIncrementLength = calcYAxisIncrementLength(height);
        float maxVal = calcMaxVal();
        for (int i = 0; i < getCount(); i++) {
            for (int j = 0; j < getDataSetCount(); j++) {
                Data data = getDataSet().get(j).get(i);
                data.setX(xIncrementLength * data.getIndex() + xSetIncrementLength * j);
                data.setY(yIncrementLength * (maxVal - data.getValue()));
                data.setFromY(height);
                data.setFormattedValue(FloatFormatter.format(data.getValue()));
                data.setColor(ColorList.getColors().get(j % ColorList.getColors().size()));
            }
        }
    }

    public float calcRectanglePaddingLength(int width) {
        return calcXAxisDataSetIncrementLength(width) * 0.1f;
    }

}
