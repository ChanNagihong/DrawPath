package com.nagihong.drawpath.DataSet;

import com.nagihong.drawpath.ColorList;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 2016/10/16.
 */

public class BubbleChartDataSet {

    private String Tag = BubbleChartDataSet.class.getSimpleName();

    private List<List<Data>> mDatas;
    private Data mMaxData, mMinData;
    private int DATA_COUNT = 14;
    private int DATASET_COUNT = 3;
    private int randomFactor = 100;

    public BubbleChartDataSet() {
        generateData();
    }

    private void generateData() {
        mDatas = new LinkedList<>();
        Random random = new Random();
        float maxValue = Float.MIN_VALUE, minValue = Float.MAX_VALUE;
        for (int j = 0; j < DATASET_COUNT; j++) {
            List<Data> dataSet = new LinkedList<>();
            for (int i = 0; i < DATA_COUNT; i++) {
                Data data = new Data();
                data.setIndex(i);
                data.setValue(random.nextInt(randomFactor));
                data.setRadius(random.nextFloat() * 6 + 1);
                data.setColor(ColorList.getColors().get(j));
                dataSet.add(data);
                if (maxValue < data.getValue()) {
                    maxValue = data.getValue();
                    mMaxData = data;
                } else if (minValue > data.getValue()) {
                    minValue = data.getValue();
                    mMinData = data;
                }
            }
            mDatas.add(dataSet);
        }
    }

    public List<List<Data>> getDatas() {
        if (null == mDatas) {
            generateData();
        }
        return mDatas;
    }

    public int getCount() {
        return DATA_COUNT;
    }

    public int getDataSetCount() {
        return DATASET_COUNT;
    }

    public float calcMaxVal() {
        float maxVal = mMaxData.getValue() + calcPaddingValue();
        float left = Math.abs(maxVal % (randomFactor / 10));
        if (maxVal - left < mMaxData.getValue()) {
            maxVal += (randomFactor / 10 - left);
        } else {
            maxVal -= left;
        }
        return maxVal;
    }

    private float calcMinVal() {
        float minVal = mMinData.getValue() - calcPaddingValue();
        if(minVal < 0) {
            return 0;
        }
        float left = Math.abs(minVal % (randomFactor / 10));
        if (minVal + left < mMinData.getValue()) {
            minVal += left;
        } else {
            minVal -= (randomFactor / 10 - left);
            if (minVal < 0) {
                minVal = 0;
            }
        }
        return minVal;
    }

    private float calcPaddingValue() {
        return Math.abs((mMaxData.getValue() - mMinData.getValue()) * 0.3f);
    }

    private float calcBaseYAxisMarkIncrementVal() {
        return 20f;
    }

    private int calcBaseXAxisMarkIncrementVal() {
        return 5;
    }

    public float calcYAxisIncrementLength(int height) {
        return height / (calcMaxVal() - calcMinVal());
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
        if (scale > calcBaseXAxisMarkIncrementVal()) {
            scale = calcBaseXAxisMarkIncrementVal();
        }
        return (int) (calcBaseXAxisMarkIncrementVal() / scale);
    }

    private float calcYAxisMarkIncrementLength(int height, float scale) {
        return calcYAxisMarkIncrementVal(scale) * calcYAxisIncrementLength(height);
    }

    private float calcXAxisMarkIncrementLength(int width, float scale) {
        return calcXAxisMarkIncrementVal(scale) * calcXAxisIncrementLength(width);
    }

    private int calcXAxisGridLinesCount(int width, float scale) {
        return getCount() / calcXAxisMarkIncrementVal(scale);
    }

    private int calcYAxisGridLinesCount(int height, float scale) {
        return (int) ((calcMaxVal() - calcMinVal()) / calcYAxisMarkIncrementVal(scale));
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
        for (int j = 0; j < DATASET_COUNT; j++) {
            List<Data> dataSet = getDatas().get(j);
            for (int i = 0; i < DATA_COUNT; i++) {
                Data data = dataSet.get(i);
                data.setX(xIncrementLength * data.getIndex());
                data.setY(yIncrementLength * (maxVal - data.getValue()));
                data.setR(data.getRadius() * calcYAxisIncrementLength(height));
            }
        }
    }

}
