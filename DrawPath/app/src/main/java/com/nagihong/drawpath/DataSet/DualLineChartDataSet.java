package com.nagihong.drawpath.DataSet;

import android.util.Log;

import com.nagihong.drawpath.Utils.FloatFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 2016/10/9.
 */

public class DualLineChartDataSet {

    private String Tag = DualLineChartDataSet.class.getSimpleName();

    private List<Data> mUpperDatas, mLowerDatas;
    private Data mUpperMaxData, mUpperMinData, mLowerMaxData, mLowerMinData;
    private float mUpperBaseValue = Float.MAX_VALUE, mLowerBaseValue = Float.MAX_VALUE;

    public DualLineChartDataSet() {
        generateData();
    }

    private void generateData() {
        mUpperDatas = new LinkedList<>();
        mLowerDatas = new LinkedList<>();
        float upperMaxVal = 0f, upperMinVal = Float.MAX_VALUE, lowerMaxVal = 0f, lowerMinVal = Float.MAX_VALUE;
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            /**
             *generate upper values
             */
            {
                Data data = new Data();
                data.setIndex(i);
                /**
                 * 400 ~ 600
                 */
                int upperYVal = random.nextInt(200) + 400;
                data.setValue(upperYVal);
                mUpperDatas.add(data);
                if (upperMaxVal < upperYVal) {
                    upperMaxVal = upperYVal;
                    mUpperMaxData = data;
                } else if (upperMinVal > upperYVal) {
                    upperMinVal = upperYVal;
                    mUpperMinData = data;
                }
            }
            /**
             * generate lower values
             */
            {
                Data data = new Data();
                data.setIndex(i);
                /**
                 * 50 ~ 150
                 */
                int lowerYVal = random.nextInt(100) + 50;
                data.setValue(lowerYVal);
                mLowerDatas.add(data);
                if (lowerMaxVal < lowerYVal) {
                    lowerMaxVal = lowerYVal;
                    mLowerMaxData = data;
                } else if (lowerMinVal > lowerYVal) {
                    lowerMinVal = lowerYVal;
                    mLowerMinData = data;
                }
            }
        }
    }

    public float calcUpperBaseVal() {
        if (Float.MAX_VALUE == mUpperBaseValue) {
            float total = 0f;
            for (int i = 0; i < getUpperDatas().size(); i++) {
                total += getUpperDatas().get(i).getValue();
            }
            mUpperBaseValue = total / getUpperDatas().size();
            Log.d(Tag, "calcUpperBaseVal() -- baseVal : " + mUpperBaseValue);
        }
        return mUpperBaseValue;
    }

    public float calcLowerBaseVal() {
        if (Float.MAX_VALUE == mLowerBaseValue) {
            float total = 0f;
            for (int i = 0; i < getLowerDatas().size(); i++) {
                total += getLowerDatas().get(i).getValue();
            }
            mLowerBaseValue = total / getLowerDatas().size();
            Log.d(Tag, "calcLowerBaseVal() -- baseVal : " + mLowerBaseValue);
        }
        return mLowerBaseValue;
    }

    public int getCount() {
        return Math.max(getUpperDatas().size(), getLowerDatas().size());
    }

    public int getUpperDatasCount() {
        return getUpperDatas().size();
    }

    public int getLowerDatasCount() {
        return getLowerDatas().size();
    }

    public float calcUpperMaxVal(int height) {
        float baseValue = calcUpperBaseVal();
        float length = height / 3;
        float maxVal = baseValue + length / calcUpperYAxisIncrementLength(height);
        maxVal /= 10;
        maxVal = Math.round(maxVal);
        maxVal *= 10;
        return maxVal;
    }

    private float calcUpperMinVal(int height) {
        float baseValue = calcUpperBaseVal();
        float length = height * 2 / 3;
        return baseValue - length / calcUpperYAxisIncrementLength(height);
    }

    public float calcLowerMaxVal(int height) {
        float baseValue = calcLowerBaseVal();
        float length = height * 2 / 3;
        float maxVal = baseValue + length / calcLowerYAxisIncrementLength(height);
        maxVal /= 10;
        maxVal = Math.round(maxVal);
        maxVal *= 10;
        return maxVal;
    }

    private float calcLowerMinVal(int height) {
        float baseValue = calcLowerBaseVal();
        float length = height / 3;
        return baseValue - length / calcLowerYAxisIncrementLength(height);
    }

    private float calcLowerBaseYAxisMarkIncrementVal() {
        return 40;
    }

    private float calcUpperBaseYAxisMarkIncrementVal() {
        return 100;
    }

    private float calcBaseXAxisMarkIncrementVal() {
        return 10;
    }

    private float calcLowerYAxisIncrementLength(int height) {
        return height / 7 / calcLowerBaseYAxisMarkIncrementVal();
    }

    private float calcUpperYAxisIncrementLength(int height) {
        return height / 7 / calcUpperBaseYAxisMarkIncrementVal();
    }

    public float calcXAxisIncrementLength(int width) {
        return width / (getCount() - 1);
    }

    public float calcUpperBaseYAxisMarkIncrementLength(int height) {
        return calcUpperYAxisIncrementLength(height) * calcUpperBaseYAxisMarkIncrementVal();
    }

    public float calcLowerBaseYAxisMarkIncrementLength(int height) {
        return calcLowerYAxisIncrementLength(height) * calcLowerBaseYAxisMarkIncrementVal();
    }

    public float calcBaseXAxisMarkIncrementLength(int width) {
        return calcXAxisIncrementLength(width) * calcBaseXAxisMarkIncrementVal();
    }

    public int calcXAxisMarkIncrementVal(float scale) {
        int val = (int) (calcBaseXAxisMarkIncrementVal() / scale);
        if(val < 1) {
            val = 1;
        }
        return val;
    }

    public float calcLowerYAxisMarkIncrementVal(float scale) {
        return calcLowerBaseYAxisMarkIncrementVal() / convertYAxisScale(scale);
    }

    public float calcUpperYAxisMarkIncrementVal(float scale) {
        return calcUpperBaseYAxisMarkIncrementVal() / convertYAxisScale(scale);
    }

    private float calcLowerYAxisMarkIncrementLength(int height, float scale) {
        return calcLowerBaseYAxisMarkIncrementLength(height) / convertYAxisScale(scale);
    }

    private float calcUpperYAxisMarkIncrementLength(int height, float scale) {
        return calcUpperBaseYAxisMarkIncrementLength(height) / convertYAxisScale(scale);
    }

    private float calcXAxisMarkIncrementLength(int width, float scale) {
        return calcXAxisMarkIncrementVal(scale) * calcXAxisIncrementLength(width);
    }

    private int calcLowerYAxisGridLinesCount(int height, float scale) {
        return (int) (height / calcLowerYAxisMarkIncrementLength(height, scale));
    }

    private int calcUpperYAxisGridLinesCount(int height, float scale) {
        return (int) (height / calcUpperYAxisMarkIncrementLength(height, scale));
    }

    private int calcXAxisGridLinesCount(int width, float scale) {
        return (getCount() - 1) / calcXAxisMarkIncrementVal(scale);
    }

    private int convertYAxisScale(float scale) {
        int intScale = (int) scale;
        if(intScale == 1) {
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

    public float[] calcUpperYAxisGridLinesY(int height, float scale) {
        float[] ys = new float[calcUpperYAxisGridLinesCount(height, scale)];
        float incrementLength = calcUpperYAxisMarkIncrementLength(height, scale);
        float current = 0f;
        for (int i = 0; i < ys.length; i++) {
            current = (i + 1) * incrementLength;
            ys[i] = current;
        }
        return ys;
    }

    public float[] calcLowerYAxisGridLinesY(int height, float scale) {
        float[] ys = new float[calcLowerYAxisGridLinesCount(height, scale)];
        float incrementLength = calcLowerYAxisMarkIncrementLength(height, scale);
        float current = 0f;
        for (int i = 0; i < ys.length; i++) {
            current = (i + 1) * incrementLength;
            ys[i] = current;
        }
        return ys;
    }

    public void calcDataPoint(int width, int height) {
        float xAxisIncrementLength = calcXAxisIncrementLength(width);
        float upperYAxisIncrementLength = calcUpperYAxisIncrementLength(height);
        float lowerYAxisIncrementLength = calcLowerYAxisIncrementLength(height);
        float upperMaxVal = calcUpperMaxVal(height);
        float lowerMaxVal = calcLowerMaxVal(height);
        for (int i = 0; i < getUpperDatas().size(); i++) {
            Data data = getUpperDatas().get(i);
            data.setX(xAxisIncrementLength * data.getIndex());
            data.setY(upperYAxisIncrementLength * (upperMaxVal - data.getValue()));
            data.setFormattedValue(FloatFormatter.format(data.getValue()));
        }
        for (int i = 0; i < getLowerDatas().size(); i++) {
            Data data = getLowerDatas().get(i);
            data.setX(xAxisIncrementLength * data.getIndex());
            data.setY(lowerYAxisIncrementLength * (lowerMaxVal - data.getValue()));
            data.setFormattedValue(FloatFormatter.format(data.getValue()));
        }
    }

    public List<Data> getUpperDatas() {
        if (null == mUpperDatas) {
            generateData();
        }
        return mUpperDatas;
    }

    public List<Data> getLowerDatas() {
        if (null == mLowerDatas) {
            generateData();
        }
        return mLowerDatas;
    }

    public Data getUpperMaxData() {
        return mUpperMaxData;
    }

    public Data getUpperMinData() {
        return mUpperMinData;
    }

    public Data getLowerMaxdata() {
        return mLowerMaxData;
    }

    public Data getLowerMinData() {
        return mLowerMinData;
    }

}
