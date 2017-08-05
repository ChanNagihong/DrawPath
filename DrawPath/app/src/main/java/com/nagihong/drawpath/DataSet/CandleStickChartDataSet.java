package com.nagihong.drawpath.DataSet;

import android.graphics.Color;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by channagihong on 2016/10/13.
 */

public class CandleStickChartDataSet {

    private String Tag = CandleStickChartDataSet.class.getSimpleName();
    private Data mMaxData, mMinData;
    private List<Data> mDatas;
    private float mPaddingVal = Float.MIN_VALUE;

    public CandleStickChartDataSet() {
        generateData();
    }

    private void generateData() {
        mDatas = new LinkedList<>();
        Data data0 = new Data();
        data0.setStartValue(4000);
        data0.setEndValue(4050);
        data0.setLowerShadow(3980);
        data0.setUpperShadow(4100);
        data0.setIndex(0);
        mDatas.add(data0);

        Data data1 = new Data();
        data1.setStartValue(4030);
        data1.setEndValue(4100);
        data1.setUpperShadow(4150);
        data1.setLowerShadow(4030);
        data1.setIndex(1);
        mDatas.add(data1);

        Data data2 = new Data();
        data2.setStartValue(4100);
        data2.setEndValue(4230);
        data2.setLowerShadow(4100);
        data2.setUpperShadow(4280);
        data2.setIndex(2);
        mDatas.add(data2);

        Data data3 = new Data();
        data3.setStartValue(4120);
        data3.setEndValue(4260);
        data3.setLowerShadow(4100);
        data3.setUpperShadow(4280);
        data3.setIndex(3);
        mDatas.add(data3);

        Data data4 = new Data();
        data4.setStartValue(4150);
        data4.setEndValue(4230);
        data4.setUpperShadow(4250);
        data4.setLowerShadow(4075);
        data4.setIndex(4);
        mDatas.add(data4);

        Data data5 = new Data();
        data5.setStartValue(4140);
        data5.setEndValue(4000);
        data5.setUpperShadow(4300);
        data5.setLowerShadow(4010);
        data5.setIndex(5);
        mDatas.add(data5);

        Data data6 = new Data();
        data6.setStartValue(4075);
        data6.setEndValue(3970);
        data6.setUpperShadow(4090);
        data6.setLowerShadow(3900);
        data6.setIndex(6);
        mDatas.add(data6);

        Data data7 = new Data();
        data7.setStartValue(4030);
        data7.setEndValue(3900);
        data7.setUpperShadow(4040);
        data7.setLowerShadow(3800);
        data7.setIndex(7);
        mDatas.add(data7);

        Data data8 = new Data();
        data8.setStartValue(3980);
        data8.setEndValue(3830);
        data8.setUpperShadow(4000);
        data8.setLowerShadow(3745);
        data8.setIndex(8);
        mDatas.add(data8);

        Data data9 = new Data();
        data9.setStartValue(3840);
        data9.setEndValue(3825);
        data9.setUpperShadow(3860);
        data9.setLowerShadow(3800);
        data9.setIndex(9);
        mDatas.add(data9);

        Data data10 = new Data();
        data10.setStartValue(3830);
        data10.setEndValue(3850);
        data10.setUpperShadow(3880);
        data10.setLowerShadow(3830);
        data10.setIndex(10);
        mDatas.add(data10);

        Data data11 = new Data();
        data11.setStartValue(3860);
        data11.setEndValue(3875);
        data11.setUpperShadow(3880);
        data11.setLowerShadow(3860);
        data11.setIndex(11);
        mDatas.add(data11);

        Data data12 = new Data();
        data12.setStartValue(3880);
        data12.setEndValue(3945);
        data12.setUpperShadow(3990);
        data12.setLowerShadow(3860);
        data12.setIndex(12);
        mDatas.add(data12);

        Data data13 = new Data();
        data13.setStartValue(4000);
        data13.setEndValue(4050);
        data13.setUpperShadow(4085);
        data13.setLowerShadow(3990);
        data13.setIndex(13);
        mDatas.add(data13);

        Data data14 = new Data();
        data14.setStartValue(4050);
        data14.setEndValue(3930);
        data14.setUpperShadow(4090);
        data14.setLowerShadow(3900);
        data14.setIndex(14);
        mDatas.add(data14);

        float mMaxValue = Float.MIN_VALUE, mMinValue = Float.MAX_VALUE;
        for (int i = 0; i < mDatas.size(); i++) {
            Data data = mDatas.get(i);
            if (mMaxValue < data.getUpperShadow()) {
                mMaxValue = data.getUpperShadow();
                mMaxData = data;
            }
            if (mMinValue > data.getLowerShadow()) {
                mMinValue = data.getLowerShadow();
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

    private Data getMinData() {
        if (null == mMinData) {
            generateData();
        }
        return mMinData;
    }

    public float calcPaddingVal() {
        if (Float.MIN_VALUE == mPaddingVal) {
            mPaddingVal = (getMaxData().getUpperShadow() - getMinData().getLowerShadow()) * 0.2f;
        }
        return mPaddingVal;
    }

    public float calcMaxVal() {
        float maxVal = getMaxData().getUpperShadow() + calcPaddingVal();
        if (Math.abs(maxVal) > 2 * calcBaseYAxisMarkIncrementVal()) {
            maxVal -= (Math.abs(maxVal) % 20);
        }
        return maxVal;
    }

    public float calcMinVal() {
        float minVal = getMinData().getLowerShadow() - calcPaddingVal();
        if (Math.abs(minVal) > 2 * calcBaseYAxisMarkIncrementVal()) {
            minVal -= (Math.abs(minVal) % 20);
        }
        return minVal;
    }

    private float calcBaseYAxisMarkIncrementVal() {
        return 20;
    }

    private float calcBaseXAxisMarkIncrementVal() {
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
        int incrementVal = (int) (calcBaseXAxisMarkIncrementVal() / scale);
        if (incrementVal > getCount()) {
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
        if (linesCount == 0) {
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
        float yIncrementLength = calcYAxisIncrementLength(height);
        float maxVal = calcMaxVal();
        for (int i = 0; i < getCount(); i++) {
            Data data = getDatas().get(i);
            data.setX(xIncrementLength * data.getIndex());
            data.setStartValueY(yIncrementLength * (maxVal - data.getStartValue()));
            data.setEndValueY(yIncrementLength * (maxVal - data.getEndValue()));
            data.setUpperShadowY(yIncrementLength * (maxVal - data.getUpperShadow()));
            data.setLowerShadowY(yIncrementLength * (maxVal - data.getLowerShadow()));
            if (data.getEndValue() > data.getStartValue()) {
                data.setColor(Color.RED);
            } else {
                data.setColor(Color.GREEN);
            }
        }
    }

    public float calcRectanglePaddingLength(int width) {
        return calcXAxisIncrementLength(width) * 0.1f;
    }

}
