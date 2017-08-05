package com.nagihong.drawpath.DataSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 16/8/17.
 */
public class LineChartDataSet2 {

    private List<Data> mDatas;
    private Data mMaxData, mMinData;
    private float mBaseVal = Float.MIN_VALUE;
    private float mPaddingVal = Float.MIN_VALUE;

    public LineChartDataSet2() {
        generateData();
    }

    private void generateData() {
        mDatas = new LinkedList<>();
        float maxVal = 0f, minVal = Float.MAX_VALUE;
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            Data data = new Data();
            data.setIndex(i);
            int yVal = random.nextInt(300) - 100;
            data.setValue(yVal);
            mDatas.add(data);
            if (maxVal < yVal) {
                maxVal = yVal;
                mMaxData = data;
            } else if (minVal > yVal) {
                minVal = yVal;
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

    public Data getMaxData() {
        return mMaxData;
    }

    public Data getMinData() {
        return mMinData;
    }

    public Data getLastData() {
        return mDatas.get(mDatas.size() - 1);
    }

    public int getCount() {
        return getDatas().size();
    }

    public float calcMaxVal() {
        return getMaxData().getValue() + calcPaddingVal();
    }

    public float calcMinVal() {
        return getMinData().getValue() - calcPaddingVal();
    }

    public float calcPaddingVal() {
        if (Float.MIN_VALUE == mPaddingVal) {
            mPaddingVal = (getMaxData().getValue() - getMinData().getValue()) * 0.3f;
        }
        return mPaddingVal;
    }

    public float calcBaseVal() {
        if (Float.MIN_VALUE == mBaseVal) {
            float total = 0f;
            for (int i = 0; i < getDatas().size(); i++) {
                total += getDatas().get(i).getValue();
            }
            mBaseVal = total / getDatas().size();
        }
        return mBaseVal;
    }

//    public float minXAxisMarkIncrementVal() {
//        return 1;
//    }

    private float calcBaseYAxisMarkIncrementVal() {
        return 20;
    }

    private float calcBaseXAxisMarkIncrementVal() {
        return 10;
    }

    private float calcBaseYAxisMarkIncrementLength(int height) {
        return calcYAxisIncrementLength(height) * calcBaseYAxisMarkIncrementVal();
    }

    private float calcBaseXAxisMarkIncrementLength(int width) {
        return calcXAxisIncrementLength(width) * calcBaseXAxisMarkIncrementVal();
    }

    public float calcYAxisIncrementLength(int height) {
        return height / (calcMaxVal() - calcMinVal());
    }

    public float calcXAxisIncrementLength(int width) {
        return width / (float) (getCount() - 1);
    }

    public int calcXAxisMarkIncrementVal(float scale) {
        return (int) Math.ceil(calcBaseXAxisMarkIncrementVal() / scale);
    }

    public float calcYAxisMarkIncrementVal(float scale) {
        return calcBaseYAxisMarkIncrementVal() / scale;
    }

    public int calcXAxisGridLinesCount(float scale) {
        return (getCount() - 1) / calcXAxisMarkIncrementVal(scale);
    }

    public int calcYAxisGridLinesCount(float scale) {
        return (int) Math.floor((calcMaxVal() - calcMinVal()) / calcYAxisMarkIncrementVal(scale));
    }

    public float calcXAxisMarkIncrementLength(int width, float scale) {
        return calcBaseXAxisMarkIncrementLength(width) / scale;
    }

    public float calcYAxisMarkIncrementLength(int height, float scale) {
        return calcBaseYAxisMarkIncrementLength(height) / scale;
    }

    public boolean isYAxisMarkShowInDecimal(float scale) {
        if (calcYAxisMarkIncrementVal(scale) > 1.0f) {
            return false;
        }
        return true;
    }

    public float[] calcXAxisGridLinesX(float scale, int width) {
        float[] xs = new float[calcXAxisGridLinesCount(scale)];
        float incrementLength = calcXAxisMarkIncrementLength(width, scale);
        float current = 0f;
        for (int i = 0; i < xs.length; i++) {
            current = (i + 1) * incrementLength;
            xs[i] = current;
        }
        return xs;
    }

    public float[] calcYAxisGridLinesY(float scale, int height) {
        float[] ys = new float[calcYAxisGridLinesCount(scale)];
        float incrementLength = calcYAxisMarkIncrementLength(height, scale);
        float current = 0f;
        for (int i = 0; i < ys.length; i++) {
            current = (i + 1) * incrementLength;
            ys[i] = current;
        }
        return ys;
    }

    public void calcDataPoint(int width, int height) {
        float xAxisIncrementLength = calcXAxisIncrementLength(width);
        float yAxisIncrementLength = calcYAxisIncrementLength(height);
        float maxVal = calcMaxVal();
        for (int i = 0; i < getCount(); i++) {
            Data data = getDatas().get(i);
            data.setX(xAxisIncrementLength * data.getIndex());
            data.setY(yAxisIncrementLength * (maxVal - data.getValue()));
        }
    }

}
