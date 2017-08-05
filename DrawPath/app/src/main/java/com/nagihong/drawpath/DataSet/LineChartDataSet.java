package com.nagihong.drawpath.DataSet;

import android.graphics.Canvas;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by channagihong on 16/8/17.
 */
public class LineChartDataSet {

    private List<Data> mDatas;
    private Data mMaxData, mMinData;
    private float mBaseScale = 1.3f;
    private float mBaseVal = Float.MIN_VALUE;
    private CacheCalcResults mCalcResults;

    public LineChartDataSet() {
        generateData();
        mCalcResults = new CacheCalcResults();
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

    public void calc(Canvas canvas, Canvas drawableCanvas, int yAxisGridLength, int xAxisGridLength, float yAxisScaleFactor, float yAxisZoomFactor, float xAxisScaleFactor, float xAxisZoomFactor) {
        float realtimeYAxisScale = yAxisScaleFactor * yAxisZoomFactor;
        float realtimeXAxisScale = xAxisScaleFactor * xAxisZoomFactor;
        float maxVal = getMaxData().getValue() * mBaseScale;
        float minVal = getMaxData().getValue() * mBaseScale;
        float baseVal = calcBaseVal();
        float midVal = (maxVal + minVal) / 2;
        float baseValOffset = baseVal - midVal;
        maxVal -= baseValOffset;
        minVal += baseValOffset;
        mCalcResults.yAxisBase = (int) (drawableCanvas.getHeight() * realtimeYAxisScale / 2);
        mCalcResults.yAxisGraduated = (drawableCanvas.getHeight() * realtimeYAxisScale) / (Math.abs(maxVal) + Math.abs(minVal));
        mCalcResults.yAxisZero = (int) (mCalcResults.yAxisBase + baseVal * mCalcResults.yAxisGraduated);
        mCalcResults.maxMarkVal = (int) (mCalcResults.yAxisZero / mCalcResults.yAxisGraduated);
        mCalcResults.minMarkVal = (int) ((drawableCanvas.getHeight() * realtimeYAxisScale - mCalcResults.yAxisZero) / (-mCalcResults.yAxisGraduated));
        mCalcResults.yAxisGridLengthGraduatedVal = yAxisGridLength / mCalcResults.yAxisGraduated;
        mCalcResults.xAxisGraduated = drawableCanvas.getWidth() * realtimeXAxisScale / getCount();
    }

    private class CacheCalcResults {
        int yAxisZero;
        int yAxisBase;
        int maxMarkVal;
        int minMarkVal;
        float yAxisGraduated;
        float yAxisGridLengthGraduatedVal;
        float xAxisGraduated;
    }

    public int getYAxisZero() {
        return mCalcResults.yAxisZero;
    }

    public int getYAxisBase() {
        return mCalcResults.yAxisBase;
    }

    public int getMaxMarkVal() {
        return mCalcResults.maxMarkVal;
    }

    public int getMinMarkVal() {
        return mCalcResults.minMarkVal;
    }

    public float getYAxisGraduated() {
        return mCalcResults.yAxisGraduated;
    }

    public float getYAxisGridLengthGraduatedVal() {
        return mCalcResults.yAxisGridLengthGraduatedVal;
    }

    public float getXAxisGraduated() {
        return mCalcResults.xAxisGraduated;
    }

}
