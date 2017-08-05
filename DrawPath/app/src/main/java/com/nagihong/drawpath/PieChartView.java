package com.nagihong.drawpath;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nagihong.drawpath.Utils.FloatFormatter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by channagihong on 16/7/27.
 */
public class PieChartView extends View {

    private String Tag = PieChartView.class.getSimpleName();
    public final static double DEG2RAD = (Math.PI / 180.0);
    public final static float FDEG2RAD = ((float) Math.PI / 180.f);

    private Paint mPaint, mValuePaint;
    private Path mPath;
    private float mBaseAngle = 0;
    private float mRotateAngle = 0;
    private int mDragStartAngle = 0;
    private float mDragTriggerDist = 9;
    private boolean mIsDrawingCenterHole = true;
    private boolean mIsJustClick = true;
    private int mSelectedIndex = -1;
    private int mSelectedPadding = 30;
    private PointF mDragStartPoint = new PointF();

    private int mDiameter = Integer.MIN_VALUE, mInnerRadius = Integer.MIN_VALUE, mOutterRadius = Integer.MIN_VALUE;
    private int mCanvasWidth, mCanvasHeight;
    private RectF mOutterBox, mInnerBox, mSelectedOutterBox;

    private List<Float> mData;
    private List<Float> mDataPercentage;
    private List<Float> mDataAngle;

    public PieChartView(Context context) {
        super(context);
        initVar();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVar();
    }

    private void initVar() {
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mValuePaint = new Paint();
        mValuePaint.setStyle(Paint.Style.STROKE);
        mValuePaint.setTextSize(30f);
        mValuePaint.setColor(Color.BLACK);
        mPath = new Path();
        mData = new LinkedList<>();
        mData.add(10f);
        mData.add(20f);
        mData.add(15f);
        mData.add(90f);
        mData.add(250f);
        mData.add(8f);
        mData.add(97f);
        mData.add(48f);
        mData.add(103f);
        toDataAngles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        drawData(canvas);
        drawDataValue(canvas);
    }

    private void drawData(Canvas canvas) {
        float accumulateAngle = 0;
        float startOutterX, startOutterY, startInnerX, startInnerY, endInnerX, endInnerY;
        for (int i = 0, count = mDataAngle.size(); i < count; i++) {
            mPaint.setColor(ColorList.getColors().get(i));
            float sweepAngle = mDataAngle.get(i);
            /**
             * set selected slice radius
             */
            int outterRadius = getOutterRadius();
            if (i == mSelectedIndex) {
                outterRadius += mSelectedPadding;
            }

            /**
             * calculate path point
             */
            if (getStartAngle() == 0 && accumulateAngle == 0) {
                startOutterX = getCenterX() + outterRadius;
                startOutterY = getCenterY();
            } else {
                startOutterX = (float) (getCenterX() + outterRadius * Math.cos((getStartAngle() + accumulateAngle) * DEG2RAD));
                startOutterY = (float) (getCenterY() + outterRadius * Math.sin((getStartAngle() + accumulateAngle) * DEG2RAD));
            }
            startInnerX = (float) (getCenterX() + getInnerRadius() * Math.cos((getStartAngle() + sweepAngle + accumulateAngle) * DEG2RAD));
            startInnerY = (float) (getCenterY() + getInnerRadius() * Math.sin((getStartAngle() + sweepAngle + accumulateAngle) * DEG2RAD));
            endInnerX = (float) (getCenterX() + getInnerRadius() * Math.cos((getStartAngle() + accumulateAngle) * DEG2RAD));
            endInnerY = (float) (getCenterY() + getInnerRadius() * Math.sin((getStartAngle() + accumulateAngle) * DEG2RAD));

            /**
             * draw slice data
             */
            mPath.reset();
            if (mIsDrawingCenterHole) {
                mPath.moveTo(endInnerX, endInnerY);
                mPath.lineTo(startOutterX, startOutterY);
            } else {
                mPath.moveTo(getCenterX(), getCenterY());
            }
            mPath.arcTo(i == mSelectedIndex ? getSelectedOutterBox() : getOutterBox(), getStartAngle() + accumulateAngle, sweepAngle);
            if (mIsDrawingCenterHole) {
                mPath.lineTo(startInnerX, startInnerY);
                mPath.arcTo(getInnerBox(), getStartAngle() + sweepAngle + accumulateAngle, -sweepAngle);
            }
            mPath.close();
            canvas.drawPath(mPath, mPaint);
            accumulateAngle += sweepAngle;
        }
    }

    private void drawDataValue(Canvas canvas) {
        int characterWidth = calcCharacterWidth(mValuePaint);
        int characterHeight = calcCharacterHeight(mValuePaint);
        float baseValueLineLength = 100;
        float accumulateAngle = 0f;
        for (int i = 0; i < mDataAngle.size(); i++) {
            float sweepAngle = mDataAngle.get(i);
            float outterRadius = getOutterRadius();
            if (i == mSelectedIndex) {
                outterRadius += mSelectedPadding;
            }
            String formattedValue = FloatFormatter.format(mDataPercentage.get(i) * 100);
            formattedValue += "%";
            float valueStringLength = formattedValue.length() * characterWidth;
            PointF point = getPointForAngle(getCenterX(), getCenterY(), (outterRadius + getInnerRadius()) / 2, getStartAngle() + accumulateAngle + sweepAngle / 2);
            float valueLineLength = baseValueLineLength;

            /**
             * calculate valueline length and draw value with line
             */
            if (point.x < getCenterX()) {
                float distLeft = point.x - baseValueLineLength - valueStringLength;
                if (distLeft < 0) {
                    valueLineLength -= Math.abs(distLeft);
                }
                canvas.drawLine(point.x, point.y, point.x - valueLineLength, point.y, mValuePaint);
                canvas.drawText(formattedValue, point.x - valueLineLength - valueStringLength, point.y + characterHeight / 2, mValuePaint);
            } else {
                float distLeft = getWidth() - point.x - valueStringLength - baseValueLineLength;
                if (distLeft < 0) {
                    valueLineLength -= Math.abs(distLeft);
                }
                canvas.drawLine(point.x, point.y, point.x + valueLineLength, point.y, mValuePaint);
                canvas.drawText(formattedValue, point.x + valueLineLength, point.y + characterHeight / 2, mValuePaint);
            }
            accumulateAngle += sweepAngle;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(Tag, "onTouchEvent()");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startAction(event);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            performRotate(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsJustClick) {
                performClick(event);
            } else {
                endAction();
            }
            mIsJustClick = true;
        }
        return true;
    }

    private void startAction(MotionEvent event) {
        mDragStartPoint.set(event.getX(), event.getY());
        mDragStartAngle = (int) getAngleForPoint(event);
    }

    private void endAction() {
        mBaseAngle += mRotateAngle;
        mBaseAngle = fixAngle(mBaseAngle);
        mRotateAngle = 0;
    }

    private void performRotate(MotionEvent event) {
        float dist = spacing(mDragStartPoint.x, mDragStartPoint.y, event.getX(), event.getY());
        if (dist > mDragTriggerDist) {
            mIsJustClick = false;
            int moveAngle = (int) getAngleForPoint(event);
            mRotateAngle = moveAngle - mDragStartAngle;
            if (mRotateAngle < 0) {
                mRotateAngle += 360;
            }
            mRotateAngle = fixAngle(mRotateAngle);
        }
        invalidate();
    }

    private void performClick(MotionEvent event) {
        int angle = (int) getAngleForPoint(event);
        float startAngle = getStartAngle();
        if (angle < startAngle) {
            angle += 360 - startAngle;
        } else {
            angle -= startAngle;
        }
        Log.d(Tag, "performClick() -- angle : " + angle + " startAngle : " + getStartAngle());
        float accumulateAngle = 0f;
        for (int i = 0; i < mDataAngle.size(); i++) {
            accumulateAngle += mDataAngle.get(i);
            if (angle < accumulateAngle) {
                mSelectedIndex = i;
                invalidate();
                break;
            }
        }
    }

    private float getAngleForPoint(MotionEvent event) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        double tx = event.getX() - centerX, ty = event.getY() - centerY;
        double length = Math.sqrt(tx * tx + ty * ty);
        double r = Math.acos(ty / length);
        float angle = (float) Math.toDegrees(r);
        if (event.getX() > centerX) {
            angle = 360 - angle;
        }
        angle += 90;
        angle = fixAngle(angle);
        return angle;
    }

    private PointF getPointForAngle(float centerX, float centerY, float dist, float angle) {
        float x = (float) (centerX + dist * Math.cos(Math.toRadians(angle)));
        float y = (float) (centerY + dist * Math.sin(Math.toRadians(angle)));
        PointF p = new PointF(x, y);
        return p;
    }

    private void toDataAngles() {
        float total = 0;
        if (null != mData) {
            for (float data : mData) {
                total += data;
            }
        }
        if (total == 0) {
            return;
        }
        mDataAngle = new LinkedList<>();
        mDataPercentage = new LinkedList<>();
        for (int i = 0, count = mData.size(); i < count; i++) {
            mDataPercentage.add(mData.get(i) / total);
        }
        for (int i = 0, count = mDataPercentage.size(); i < count; i++) {
            mDataAngle.add(mDataPercentage.get(i) * 360);
        }
        return;
    }

    private int getDiameter() {
        if (Integer.MIN_VALUE == mDiameter) {
            mDiameter = Math.min(mCanvasWidth, mCanvasHeight);
        }
        return mDiameter;
    }

    private int getInnerRadius() {
        if (Integer.MIN_VALUE == mInnerRadius) {
            mInnerRadius = getDiameter() / 5;
        }
        return mInnerRadius;
    }

    private int getOutterRadius() {
        if (Integer.MIN_VALUE == mOutterRadius) {
            mOutterRadius = getDiameter() / 2 - 80;
        }
        return mOutterRadius;
    }

    private int getCenterX() {
        return getWidth() / 2;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    private RectF getOutterBox() {
        if (null == mOutterBox) {
            mOutterBox = new RectF(
                    getCenterX() - getOutterRadius(),
                    getCenterY() - getOutterRadius(),
                    getCenterX() + getOutterRadius(),
                    getCenterY() + getOutterRadius());
        }
        return mOutterBox;
    }

    private RectF getSelectedOutterBox() {
        if (null == mSelectedOutterBox) {
            mSelectedOutterBox = new RectF(
                    getCenterX() - getOutterRadius() - mSelectedPadding,
                    getCenterY() - getOutterRadius() - mSelectedPadding,
                    getCenterX() + getOutterRadius() + mSelectedPadding,
                    getCenterY() + getOutterRadius() + mSelectedPadding);
        }
        return mSelectedOutterBox;
    }

    private RectF getInnerBox() {
        if (null == mInnerBox) {
            mInnerBox = new RectF(
                    getCenterX() - getInnerRadius(),
                    getCenterY() - getInnerRadius(),
                    getCenterX() + getInnerRadius(),
                    getCenterY() + getInnerRadius());
        }
        return mInnerBox;
    }

    private float getStartAngle() {
        float startAngle = mBaseAngle + mRotateAngle;
        startAngle = fixAngle(startAngle);
        return startAngle;
    }

    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    private float fixAngle(float angle) {
        if (angle >= 360) {
            return angle % 360;
        }
        return angle;
    }

    private int calcCharacterHeight(Paint paint) {
        Rect r = new Rect();
        paint.getTextBounds("0", 0, 1, r);
        return r.height();
    }

    private int calcCharacterWidth(Paint paint) {
        return (int) paint.measureText("0");
    }

}
