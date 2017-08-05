package com.nagihong.drawpath.DataSet;

/**
 * Created by channagihong on 16/8/18.
 */
public class Data {

    private int index;
    private float value;
    private float radius;
    private String formattedValue;
    private float x;
    private float y;
    private float r;
    private float fromY = 0;
    private int color;
    private int selectedColor;

    /**
     * candlestick chart
     */
    private float startValue;
    private float endValue;
    private float upperShadow;
    private float lowerShadow;
    private float startValueY;
    private float endValueY;
    private float upperShadowY;
    private float lowerShadowY;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getFromY() {
        return fromY;
    }

    public void setFromY(float fromY) {
        this.fromY = fromY;
    }

    public String getFormattedValue() {
        return formattedValue;
    }

    public void setFormattedValue(String formattedValue) {
        this.formattedValue = formattedValue;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public float getStartValue() {
        return startValue;
    }

    public void setStartValue(float startValue) {
        this.startValue = startValue;
    }

    public float getEndValue() {
        return endValue;
    }

    public void setEndValue(float endValue) {
        this.endValue = endValue;
    }

    public float getUpperShadow() {
        return upperShadow;
    }

    public void setUpperShadow(float upperShadow) {
        this.upperShadow = upperShadow;
    }

    public float getLowerShadow() {
        return lowerShadow;
    }

    public void setLowerShadow(float lowerShadow) {
        this.lowerShadow = lowerShadow;
    }

    public float getStartValueY() {
        return startValueY;
    }

    public void setStartValueY(float startValueY) {
        this.startValueY = startValueY;
    }

    public float getEndValueY() {
        return endValueY;
    }

    public void setEndValueY(float endValueY) {
        this.endValueY = endValueY;
    }

    public float getUpperShadowY() {
        return upperShadowY;
    }

    public void setUpperShadowY(float upperShadowY) {
        this.upperShadowY = upperShadowY;
    }

    public float getLowerShadowY() {
        return lowerShadowY;
    }

    public void setLowerShadowY(float lowerShadowY) {
        this.lowerShadowY = lowerShadowY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }
}
