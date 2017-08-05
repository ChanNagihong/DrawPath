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
public class HalfPieChartView extends View {

    private String Tag = HalfPieChartView.class.getSimpleName();
    public final static double DEG2RAD = (Math.PI / 180.0);
    public final static float FDEG2RAD = ((float) Math.PI / 180.f);

    private Paint mPaint, mValuePaint;
    private Path mPath;
    private float mBaseAngle = 0;
    private boolean mIsDrawingCenterHole = true;
    private int mLeftSelected = -1, mRightSelected = - 1, mTopSelected = -1, mBottomSelected = - 1;
    private int mSelectedPadding = 30;
    private PointF mDragStartPoint = new PointF();

    private int mDiameter = Integer.MIN_VALUE, mInnerRadius = Integer.MIN_VALUE, mOutterRadius = Integer.MIN_VALUE;
    private int mCanvasWidth, mCanvasHeight;
    private RectF mOutterBox, mInnerBox, mSelectedOutterBox;

    private List<Float> mData;
    private List<Float> mDataPercentage;
    private List<Float> mDataAngle;

    private enum Mode {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    public HalfPieChartView(Context context) {
        super(context);
        initVar();
    }

    public HalfPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public HalfPieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public HalfPieChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
//        mData.add(250f);
//        mData.add(8f);
//        mData.add(97f);
//        mData.add(48f);
//        mData.add(103f);
        toDataAngles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        drawData(canvas, Mode.LEFT);
        drawData(canvas, Mode.RIGHT);
        drawData(canvas, Mode.TOP);
        drawData(canvas, Mode.BOTTOM);
        drawDataValue(canvas, Mode.LEFT);
        drawDataValue(canvas, Mode.RIGHT);
        drawDataValue(canvas, Mode.TOP);
        drawDataValue(canvas, Mode.BOTTOM);
    }

    private void drawData(Canvas canvas, Mode mode) {
        float accumulateAngle = 0;
        float startOutterX, startOutterY, startInnerX, startInnerY, endInnerX, endInnerY;
        for (int i = 0, count = mDataAngle.size(); i < count; i++) {
            mPaint.setColor(ColorList.getColors().get(i));
            float sweepAngle = mDataAngle.get(i);
            /**
             * set selected slice radius
             */
            int outterRadius = getOutterRadius();
            if (i == getSelectedIndex(mode)) {
                outterRadius += mSelectedPadding;
            }

            /**
             * calculate path point
             */
            if (getStartAngle(mode) == 0 && accumulateAngle == 0) {
                startOutterX = getCenterX(mode) + outterRadius;
                startOutterY = getCenterY(mode);
            } else {
                startOutterX = (float) (getCenterX(mode) + outterRadius * Math.cos((getStartAngle(mode) + accumulateAngle) * DEG2RAD));
                startOutterY = (float) (getCenterY(mode) + outterRadius * Math.sin((getStartAngle(mode) + accumulateAngle) * DEG2RAD));
            }
            startInnerX = (float) (getCenterX(mode) + getInnerRadius() * Math.cos((getStartAngle(mode) + sweepAngle + accumulateAngle) * DEG2RAD));
            startInnerY = (float) (getCenterY(mode) + getInnerRadius() * Math.sin((getStartAngle(mode) + sweepAngle + accumulateAngle) * DEG2RAD));
            endInnerX = (float) (getCenterX(mode) + getInnerRadius() * Math.cos((getStartAngle(mode) + accumulateAngle) * DEG2RAD));
            endInnerY = (float) (getCenterY(mode) + getInnerRadius() * Math.sin((getStartAngle(mode) + accumulateAngle) * DEG2RAD));

            /**
             * draw slice data
             */
            mPath.reset();
            if (mIsDrawingCenterHole) {
                mPath.moveTo(endInnerX, endInnerY);
                mPath.lineTo(startOutterX, startOutterY);
            } else {
                mPath.moveTo(getCenterX(mode), getCenterY(mode));
            }
            mPath.arcTo(i == getSelectedIndex(mode) ? getSelectedOutterBox(mode) : getOutterBox(mode), getStartAngle(mode) + accumulateAngle, sweepAngle);
            if (mIsDrawingCenterHole) {
                mPath.lineTo(startInnerX, startInnerY);
                mPath.arcTo(getInnerBox(mode), getStartAngle(mode) + sweepAngle + accumulateAngle, -sweepAngle);
            }
            mPath.close();
            canvas.drawPath(mPath, mPaint);
            accumulateAngle += sweepAngle;
        }
    }

    private void drawDataValue(Canvas canvas, Mode mode) {
        int characterWidth = calcCharacterWidth(mValuePaint);
        int characterHeight = calcCharacterHeight(mValuePaint);
        float baseValueLineLength = 100;
        float accumulateAngle = 0f;
        for (int i = 0; i < mDataAngle.size(); i++) {
            float sweepAngle = mDataAngle.get(i);
            float outterRadius = getOutterRadius();
            if (i == getSelectedIndex(mode)) {
                outterRadius += mSelectedPadding;
            }
            String formattedValue = FloatFormatter.format(mDataPercentage.get(i) * 100);
            formattedValue += "%";
            float valueStringLength = formattedValue.length() * characterWidth;
            PointF point = getPointForAngle(getCenterX(mode), getCenterY(mode), (outterRadius + getInnerRadius()) / 2, getStartAngle(mode) + accumulateAngle + sweepAngle / 2);
            float valueLineLength = baseValueLineLength;

            /**
             * calculate valueline length and draw value with line
             */
            if (point.x < getCenterX(mode)) {
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
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick(event);
        }
        return true;
    }

    private void performClick(MotionEvent event) {
        Mode mode = getPartForPoint(event);
        if(null == mode) {
            return;
        }
        int angle = (int) getAngleForPoint(event, mode);
        float startAngle = getStartAngle(mode);
        if (angle < startAngle) {
            angle += 360 - startAngle;
        } else {
            angle -= startAngle;
        }
        float accumulateAngle = 0f;
        for (int i = 0; i < mDataAngle.size(); i++) {
            accumulateAngle += mDataAngle.get(i);
            if (angle < accumulateAngle) {
                setSelectedIndex(mode, i);
                invalidate();
                break;
            }
        }
    }

    private Mode getPartForPoint(MotionEvent event) {
        for(Mode mode : Mode.values()) {
            float centerX = getCenterX(mode);
            float centerY = getCenterY(mode);
            if(spacing(centerX, centerY, event.getX(), event.getY()) < getOutterRadius()) {
                return mode;
            }
        }
        return null;
    }

    private float getAngleForPoint(MotionEvent event, Mode mode) {
        int centerX = getCenterX(mode);
        int centerY = getCenterY(mode);
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
            mDataAngle.add(mDataPercentage.get(i) * 180);
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

    private int getCenterX(Mode mode) {
        if(mode == Mode.LEFT) {
            return 0;
        } else if(mode == Mode.RIGHT) {
            return getWidth();
        } else {
            return getWidth() / 2;
        }
    }

    private int getCenterY(Mode mode) {
        if(mode == Mode.TOP) {
            return 0;
        } else if(mode == Mode.BOTTOM) {
            return getHeight();
        } else {
            return getHeight() / 2;
        }
    }

    private RectF getOutterBox(Mode mode) {
        if (null == mOutterBox) {
            mOutterBox = new RectF();
        }
        mOutterBox.left = getCenterX(mode) - getOutterRadius();
        mOutterBox.top = getCenterY(mode) - getOutterRadius();
        mOutterBox.right = getCenterX(mode) + getOutterRadius();
        mOutterBox.bottom = getCenterY(mode) + getOutterRadius();
        return mOutterBox;
    }

    private RectF getSelectedOutterBox(Mode mode) {
        if (null == mSelectedOutterBox) {
            mSelectedOutterBox = new RectF();
        }
        mSelectedOutterBox.left = getCenterX(mode) - getOutterRadius() - mSelectedPadding;
        mSelectedOutterBox.top = getCenterY(mode) - getOutterRadius() - mSelectedPadding;
        mSelectedOutterBox.right = getCenterX(mode) + getOutterRadius() + mSelectedPadding;
        mSelectedOutterBox.bottom = getCenterY(mode) + getOutterRadius() + mSelectedPadding;
        return mSelectedOutterBox;
    }

    private RectF getInnerBox(Mode mode) {
        if (null == mInnerBox) {
            mInnerBox = new RectF();
        }
        mInnerBox.left = getCenterX(mode) - getInnerRadius();
        mInnerBox.top = getCenterY(mode) - getInnerRadius();
        mInnerBox.right = getCenterX(mode) + getInnerRadius();
        mInnerBox.bottom = getCenterY(mode) + getInnerRadius();
        return mInnerBox;
    }

    private int getSelectedIndex(Mode mode) {
        if(mode == Mode.LEFT) {
            return mLeftSelected;
        } else if(mode == Mode.RIGHT) {
            return mRightSelected;
        } else if(mode == Mode.TOP) {
            return mTopSelected;
        } else {
            return mBottomSelected;
        }
    }

    private void setSelectedIndex(Mode mode, int index) {
        if(mode == Mode.LEFT) {
            mLeftSelected = index;
        } else if(mode == Mode.RIGHT) {
            mRightSelected = index;
        } else if(mode == Mode.TOP) {
            mTopSelected = index;
        } else {
            mBottomSelected = index;
        }
    }

    private float getStartAngle(Mode mode) {
        if(mode == Mode.LEFT) {
            return 270;
        } else if(mode == Mode.RIGHT) {
            return 90;
        } else if(mode == Mode.TOP) {
            return 0;
        } else if(mode == Mode.BOTTOM) {
            return 180;
        }
        return 0;
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
