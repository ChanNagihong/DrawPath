package com.nagihong.drawpath.DataSet;

import android.graphics.RectF;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 2016/10/13.
 */

public class ComparisonBarChartDataSet {

    private String Tag = ComparisonBarChartDataSet.class.getSimpleName();
    private int[] mLeftDatas, mRightDatas;
    private List<RectF> mLeftRects, mRightRects;
    private List<String> mYAxisMarks;
    private int[] mXAxisMark;
    private int DATA_COUNT = 100;

    public ComparisonBarChartDataSet() {
        generateData();
    }

    private void generateData() {
        mYAxisMarks = new LinkedList<>();
        Random random = new Random();
        int count = DATA_COUNT;
        int splitIncrement = 10;
        int randomMax = 100;
        for (int i = splitIncrement; i <= randomMax; i += splitIncrement) {
            mYAxisMarks.add((i - splitIncrement) + "-" + i);
        }
        mLeftDatas = new int[mYAxisMarks.size()];
        mRightDatas = new int[mYAxisMarks.size()];
        for (int i = 0; i < count; i++) {
            int leftValue = random.nextInt(randomMax);
            int rightValue = random.nextInt(randomMax);
            int leftIndex = leftValue / splitIncrement;
            int rightIndex = rightValue / splitIncrement;
            mLeftDatas[leftIndex]++;
            mRightDatas[rightIndex]++;
        }
    }

    public int[] getLeftDatas() {
        if (null == mLeftDatas) {
            generateData();
        }
        return mLeftDatas;
    }

    public int[] getRightDatas() {
        if (null == mRightDatas) {
            generateData();
        }
        return mRightDatas;
    }

    public List<RectF> getLeftRects() {
        return mLeftRects;
    }

    public List<RectF> getRightRects() {
        return mRightRects;
    }

    public List<String> getYAxisMarks() {
        if (null == mYAxisMarks) {
            generateData();
        }
        return mYAxisMarks;
    }

    public int[] getXAxisMarks() {
        return mXAxisMark;
    }

    public int getCount() {
        return getLeftDatas().length;
    }

    public int calcMaxVal() {
        int maxValue = 0;
        for (int i = 0; i < getCount(); i++) {
            if (maxValue < getLeftDatas()[i]) {
                maxValue = getLeftDatas()[i];
            }
            if (maxValue < getRightDatas()[i]) {
                maxValue = getRightDatas()[i];
            }
        }
        maxValue += 5;
        if (maxValue > 2 * calcBaseXAxisMarkIncrementVal()) {
            maxValue -= maxValue % calcBaseXAxisMarkIncrementVal();
        }
        return maxValue;
    }

    private float calcBaseYAxisMarkIncrementVal() {
        return 1;
    }

    private int calcBaseXAxisMarkIncrementVal() {
        int incrementVal = DATA_COUNT / 10;
        if (incrementVal < 10) {
            incrementVal = 10;
        }
        if (incrementVal > 2 * 10) {
            incrementVal -= incrementVal % 10;
        }
        return incrementVal;
    }

    public float calcYAxisIncrementLength(int height) {
        return height / getYAxisMarks().size();
    }

    public float calcXAxisIncrementLength(int width) {
        return width / (float) (2 * calcMaxVal());
    }

    private float calcBaseYAxisMarkIncrementLength(int height) {
        return calcBaseYAxisMarkIncrementVal() * calcYAxisIncrementLength(height);
    }

    private float calcBaseXAxisMarkIncrementLength(int width) {
        return calcBaseXAxisMarkIncrementVal() * calcXAxisIncrementLength(width);
    }

    public int calcYAxisMarkIncrementVal(float scale) {
//        if(scale > calcBaseYAxisMarkIncrementVal()) {
//            scale = calcBaseYAxisMarkIncrementVal();
//        }
//        return (int) (calcBaseYAxisMarkIncrementVal() / scale);
        return 1;
    }

    public int calcXAxisMarkIncrementVal(float scale) {
        scale = convertXAxisScale(scale);
        int incrementVal = (int) (calcBaseXAxisMarkIncrementVal() / scale);
        if (incrementVal < 1) {
            return 1;
        }
        return incrementVal;
    }

    private int convertXAxisScale(float scale) {
        int intScale = (int) scale;
        if (intScale == 1) {
            return intScale;
        }
        if(intScale > calcBaseXAxisMarkIncrementVal()) {
            return calcBaseXAxisMarkIncrementVal();
        }
        int rest = intScale % 2;
        return intScale - rest;
    }

    private float calcYAxisMarkIncrementLength(int height, float scale) {
        return calcBaseYAxisMarkIncrementVal() * calcYAxisIncrementLength(height);
    }

    private float calcXAxisMarkIncrementLength(int width, float scale) {
        return calcBaseXAxisMarkIncrementVal() * calcXAxisIncrementLength(width);
    }

    private int calcXAxisGridLinesCount(int width, float scale) {
        return (int) (width / (2 * calcXAxisMarkIncrementLength(width, scale)));
    }

    private int calcYAxisGridLinesCount(int height, float scale) {
        return (int) (getYAxisMarks().size() / calcBaseYAxisMarkIncrementVal());
    }

    public float[] calcXAxisGridLinesX(int width, float scale) {
        int sideXAxisGridLinesCount = calcXAxisGridLinesCount(width, scale);
        float[] xs = new float[sideXAxisGridLinesCount * 2];
        float incrementLength = calcXAxisMarkIncrementLength(width, scale);
        float incrementVal = calcXAxisMarkIncrementVal(scale);
        float current = width / 2;
        int currentVal = 0;
        mXAxisMark = new int[xs.length];
        int i;
        for (i = sideXAxisGridLinesCount - 1; i >= 0; i--) {
            current -= incrementLength;
            currentVal += incrementVal;
            xs[i] = current;
            mXAxisMark[i] = currentVal;
            Log.d(Tag, "calcXAxisGridLinesX() -- left i : " + i);
        }
        current = width / 2;
        currentVal = 0;
        for (i = sideXAxisGridLinesCount; i < xs.length; i++) {
            current += incrementLength;
            currentVal += incrementVal;
            xs[i] = current;
            mXAxisMark[i] = currentVal;
            Log.d(Tag, "calcXAxisGridLinesX() -- right i : " + i);
        }
        return xs;
    }

    public float[] calcYAxisGridLinesY(int height, float scale) {
        float[] ys = new float[calcYAxisGridLinesCount(height, scale)];
        float incrementLength = calcYAxisMarkIncrementLength(height, scale);
        float current = height;
        for (int i = 0; i < ys.length; i++) {
            current -= incrementLength;
            ys[i] = current;
        }
        return ys;
    }

    public void calcDataPoint(int width, int height) {
        float xIncrementLength = calcXAxisIncrementLength(width);
        float yIncrementLength = calcYAxisIncrementLength(height);
        float current = height;
        float padding = calcRectanglePaddingLength(height);
        if (null == mLeftRects || null == mRightRects) {
            mLeftRects = new LinkedList<>();
            mRightRects = new LinkedList<>();
            for (int i = 0; i < getCount(); i++) {
                current -= yIncrementLength;
                RectF leftRect = new RectF();
                leftRect.left = width / 2 - getLeftDatas()[i] * xIncrementLength;
                leftRect.top = current + padding;
                leftRect.right = width / 2;
                leftRect.bottom = current + yIncrementLength - padding;
                mLeftRects.add(leftRect);
                RectF rightRect = new RectF();
                rightRect.left = width / 2;
                rightRect.top = current + padding;
                rightRect.right = width / 2 + getRightDatas()[i] * xIncrementLength;
                rightRect.bottom = current + yIncrementLength - padding;
                mRightRects.add(rightRect);
            }
        }
    }

    public float calcRectanglePaddingLength(int height) {
        return calcYAxisIncrementLength(height) * 0.15f;
    }

}
