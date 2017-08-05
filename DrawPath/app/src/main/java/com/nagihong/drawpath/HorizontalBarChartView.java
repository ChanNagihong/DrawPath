package com.nagihong.drawpath;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nagihong.drawpath.DataSet.Data;
import com.nagihong.drawpath.DataSet.HorizontalBarChartDataSet;
import com.nagihong.drawpath.Utils.FloatFormatter;

import java.lang.ref.WeakReference;

/**
 * Created by channagihong on 2016/10/10.
 */

public class HorizontalBarChartView extends View {

    private String Tag = HorizontalBarChartView.class.getSimpleName();

    /**
     * normal staffs
     */
    private int mCanvasWidth;
    private int mCanvasHeight;

    /**
     * all staff relative to background framelines
     */
    private int mPaddingLeft = 100;
    private int mPaddingRight = 100;
    private int mPaddingTop = 50;
    private int mPaddingBottom = 100;
    private float mFramePaintStrokeWidth = 2.5f;
    private Paint mFramePaint;

    /**
     * all staffs relative to background gridlines
     */
    private int mGridLineColor = Color.parseColor("#D4D4D4");
    private Paint mGridPaint, mMarkPaint;
    private float mGridPaintStrokeWidth = 2.5f;
    private Path mGridPath;

    /**
     * all staff relative to draw datalines
     */
    private Paint mRectanglePaint, mBitmapPaint, mValuePaint;
    private WeakReference<Bitmap> mDrawBitmap;
    private Canvas mBitmapCanvas;

    /**
     * all staffs relative to data
     */
    private HorizontalBarChartDataSet mDataSet = new HorizontalBarChartDataSet();
    private int mSelectedDataIndex = -1;

    /**
     * all staffs relative to drawing but not belong to any categories mentioned above.
     */
    private enum MODE {
        NONE,
        ZOOM_MODE,
        DRAG_MODE
    }

    private MODE mMode = MODE.NONE;
    private Matrix mGlobalMatrix, mSavedMatrix;
    private float[] mMatrixValues, mMatrixPoint, mBound, mMappedBound;
    private PointF mDragStartPoint = new PointF(0, 0), mZoomCenterPoint = new PointF(0, 0);
    private float mDistCache = 0f;
    private float mDragTriggerDist = 9;
    private float mZoomTriggerDist = 9;
    private boolean mIsJustClick = true;

    public HorizontalBarChartView(Context context) {
        super(context);
        initVar();
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVar();
    }

    private void initVar() {
        mFramePaint = new Paint();
        mGridPaint = new Paint();
        mMarkPaint = new Paint();
        mRectanglePaint = new Paint();
        mBitmapPaint = new Paint();
        mValuePaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        drawFrame(canvas);
        drawGridLines(canvas);
    }

    private void drawFrame(Canvas canvas) {
        mFramePaint.reset();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(mFramePaintStrokeWidth);
        mFramePaint.setColor(mGridLineColor);
        canvas.drawLine(mPaddingLeft, mCanvasHeight - mPaddingBottom, canvas.getWidth() - mPaddingRight, mCanvasHeight - mPaddingBottom, mFramePaint);
        canvas.drawLine(mPaddingLeft, mPaddingTop, mPaddingLeft, canvas.getHeight() - mPaddingBottom, mFramePaint);
    }

    private void drawGridLines(Canvas canvas) {
        DashPathEffect effect = new DashPathEffect(new float[]{10.0f, 10.0f}, 0);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(mGridPaintStrokeWidth);
        mGridPaint.setPathEffect(effect);
        mGridPaint.setColor(mGridLineColor);
        mGridPath = new Path();
        mMarkPaint = new Paint();
        mMarkPaint.setStyle(Paint.Style.STROKE);
        mMarkPaint.setTextSize(30f);
        drawXAxisGridLines(canvas);
//        drawYAxisGridLines(canvas);
        drawYAxisMark(canvas);
        drawData(canvas);
    }

    private void drawXAxisGridLines(Canvas canvas) {
        float[] xs = mDataSet.calcXAxisGridLinesX(getFrameWidth(), getXAxisScale());
        for (int i = 0; i < xs.length; i++) {
            getMatrixPoint()[0] = xs[i];
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] < 0) {
                continue;
            }
            if (getMatrixPoint()[0] > getFrameWidth()) {
                break;
            }
            canvas.drawLine(mPaddingLeft + getMatrixPoint()[0], mPaddingTop, mPaddingLeft + getMatrixPoint()[0], mCanvasHeight - mPaddingBottom, mGridPaint);
        }
        drawXAxisMark(xs, canvas);
    }

    private void drawYAxisGridLines(Canvas canvas) {
        mGridPaint.setColor(Color.BLACK);
        float ys[] = mDataSet.calcYAxisGridLinesY(getFrameHeight(), getYAxisScale());
        for (int i = 0; i < ys.length; i++) {
            getMatrixPoint()[1] = ys[i];
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[1] < 0) {
                continue;
            }
            if (getMatrixPoint()[1] > getFrameHeight()) {
                break;
            }
            canvas.drawLine(mPaddingLeft, mPaddingTop + getMatrixPoint()[1], mCanvasWidth - mPaddingRight, mPaddingTop + getMatrixPoint()[1], mGridPaint);
        }
        mGridPaint.setColor(mGridLineColor);
    }

    private void drawXAxisMark(float[] xs, Canvas canvas) {
        mMarkPaint.setColor(Color.BLACK);
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        float xAxisIncrementVal = mDataSet.calcXAxisMarkIncrementVal(getXAxisScale());
        boolean showDecimal = false;
        if (mDataSet.calcXAxisMarkIncrementVal(getYAxisScale()) < 1f) {
            showDecimal = true;
        }
        for (int i = -1; i < xs.length; i++) {
            if (i == -1) {
                getMatrixPoint()[0] = 0f;
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[0] < 0) {
                    continue;
                }
                canvas.drawText("0", mPaddingLeft + getMatrixPoint()[0] - characterWidth / 2, mCanvasHeight - mPaddingBottom + characterHeight + 10, mMarkPaint);
            } else {
                getMatrixPoint()[0] = xs[i];
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[0] < 0) {
                    continue;
                }
                if (getMatrixPoint()[0] > getFrameWidth()) {
                    break;
                }
                float mark = xAxisIncrementVal * (i + 1);
                if (!showDecimal) {
                    mark = (float) Math.floor(mark);
                }
                String markS = FloatFormatter.format(mark);
                float characterXOffset = markS.length() * characterWidth / 2;
                canvas.drawText(markS, mPaddingLeft + getMatrixPoint()[0] - characterXOffset, mCanvasHeight - mPaddingBottom + characterHeight + 10, mMarkPaint);
            }
        }
    }

    private void drawYAxisMark(Canvas canvas) {
        float[] ys = mDataSet.calcYAxisGridLinesY(getFrameHeight(), getYAxisScale());
        mMarkPaint.setColor(Color.BLACK);
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        int yAxisIncrementVal = mDataSet.calcYAxisMarkIncrementVal(getYAxisScale());
        float length = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        float halfLength = length / 2;
        int maxVal = mDataSet.getCount();
        for (int i = -1; i < ys.length; i++) {
            if (i == -1) {
                getMatrixPoint()[1] = 0f - halfLength;
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[1] < 0) {
                    continue;
                }
                String maxValS = String.valueOf(maxVal);
                float characterXOffset = maxValS.length() * characterWidth;
                canvas.drawText(maxValS, mPaddingLeft - characterXOffset, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
            } else {
                getMatrixPoint()[1] = ys[i] - halfLength;
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[1] < 0) {
                    continue;
                }
                if (getMatrixPoint()[1] > getFrameHeight()) {
                    break;
                }
                int mark = maxVal - yAxisIncrementVal * (i + 1);
                String markS = String.valueOf(mark);
                float characterXOffset = markS.length() * characterWidth;
                canvas.drawText(markS, mPaddingLeft - characterXOffset - 10, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
            }
        }
    }

    private void drawData(Canvas canvas) {
        mBitmapPaint.setStyle(Paint.Style.FILL);
        if (mDrawBitmap == null) {
            if (getFrameWidth() > 0 && getFrameHeight() > 0) {
                mDrawBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }
        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
        mDataSet.calcDataPoint(getFrameWidth(), getFrameHeight());
        drawRectangles(mBitmapCanvas);
        canvas.drawBitmap(mDrawBitmap.get(), mPaddingLeft, mPaddingTop, mBitmapPaint);
        mValuePaint.setStyle(Paint.Style.STROKE);
        mValuePaint.setColor(Color.BLACK);
        mValuePaint.setTextSize(30f);
        if (isNeedToDrawDataValues()) {
            drawDataValues(canvas);
        }
    }

    private void drawRectangles(Canvas bitmapCanvas) {
        mRectanglePaint.setStyle(Paint.Style.FILL);
        float length = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        float padding = mDataSet.calcRectanglePaddingLength(getFrameHeight());
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            getMatrixPoint()[1] = data.getY() - length + padding;
            getMatrixPoint()[2] = data.getX();
            getMatrixPoint()[3] = data.getY() - padding;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[1] > getFrameHeight()
                    || getMatrixPoint()[2] < 0) {
                continue;
            }
            if (getMatrixPoint()[3] < 0) {
                break;
            }
            if (i == mSelectedDataIndex) {
                mRectanglePaint.setColor(Color.BLACK);
            } else {
                mRectanglePaint.setColor(data.getColor());
            }
            bitmapCanvas.drawRect(0, getMatrixPoint()[1], getMatrixPoint()[2], getMatrixPoint()[3], mRectanglePaint);
        }
    }

    private void drawDataValues(Canvas canvas) {
        float characterWidth = calcCharacterWidth(mValuePaint);
        int characterHeight = calcCharacterHeight(mValuePaint);
        float length = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        float halfLength = length / 2;
        length *= getXAxisScale();
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            getMatrixPoint()[0] = data.getX();
            getMatrixPoint()[1] = data.getY() - halfLength;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] < 0
                    || getMatrixPoint()[0] > getFrameWidth()
                    || getMatrixPoint()[1] + length / 2 > getFrameHeight()) {
                continue;
            }
            if (getMatrixPoint()[1] + length / 2 < 0) {
                break;
            }
            float characterXOffset = characterWidth * data.getFormattedValue().length() / 2;
            Log.d(Tag, i + " , " + data.getFormattedValue());
            float startX = getMatrixPoint()[0] + mPaddingLeft + characterXOffset;
            float startY = getMatrixPoint()[1] + characterHeight / 2 + mPaddingTop;
            canvas.drawText(data.getFormattedValue(), startX, startY, mValuePaint);
        }
    }

    private boolean isNeedToDrawDataValues() {
        float y0 = 0f, y1 = 0f;
        getMatrixPoint()[1] = 0f;
        getGlobalMatrix().mapPoints(getMatrixPoint());
        y0 = getMatrixPoint()[1];
        getMatrixPoint()[1] = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        getGlobalMatrix().mapPoints(getMatrixPoint());
        y1 = getMatrixPoint()[1];
        if (Math.abs(y1 - y0) >= calcCharacterHeight(mValuePaint)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() < 2) {
                    startDragMode(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    startZoomMode(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mMode == MODE.ZOOM_MODE) {
                    endZoomMode();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    if (mMode == MODE.ZOOM_MODE) {
                        performZoom(event);
                    }
                } else {
                    if (mMode == MODE.DRAG_MODE) {
                        performDrag(event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerCount() < 2) {
                    if (mMode == MODE.DRAG_MODE) {
                        endDragMode(event);
                    }
                }
                if (mIsJustClick) {
                    performClick(event);
                }
                mIsJustClick = true;
                break;
        }
        return true;
    }

    private void startDragMode(MotionEvent event) {
        Log.d(Tag, "startDragMode()");
        mMode = MODE.DRAG_MODE;
        mSavedMatrix = new Matrix();
        mSavedMatrix.set(getGlobalMatrix());
        mDragStartPoint = new PointF(event.getX(), event.getY());
    }

    private void endDragMode(MotionEvent event) {
        Log.d(Tag, "endDragMode()");
        mMode = MODE.NONE;
        mDragStartPoint.set(0, 0);
        invalidate();
    }

    private void startZoomMode(MotionEvent event) {
        Log.d(Tag, "startZoomMode()");
        mMode = MODE.ZOOM_MODE;
        mDistCache = spacing(event);
        mSavedMatrix = new Matrix();
        mSavedMatrix.set(getGlobalMatrix());
        setZoomCenter(event);
    }

    private void endZoomMode() {
        Log.d(Tag, "endZoomMode()");
        mMode = MODE.NONE;
        invalidate();
    }

    private void performZoom(MotionEvent event) {
        Log.d(Tag, "performZoom()");
        float dist = spacing(event);
        if (Math.abs(mDistCache - dist) >= mZoomTriggerDist) {
            mIsJustClick = false;
            float ratio = dist / mDistCache;
            getGlobalMatrix().set(mSavedMatrix);
            getGlobalMatrix().postScale(ratio, ratio, mZoomCenterPoint.x, mZoomCenterPoint.y);
            if (getXAxisScale() < 1f) {
                mGlobalMatrix.setScale(1f, 1f);
                mGlobalMatrix.setTranslate(0f, 0f);
            }
            invalidate();
        }
    }

    private void performDrag(MotionEvent event) {
        Log.d(Tag, "performDrag()");
        float dist = spacing(event.getX(), event.getY(), mDragStartPoint.x, mDragStartPoint.y);
        if (dist >= mDragTriggerDist) {
            mIsJustClick = false;
            float xTrans = event.getX() - mDragStartPoint.x;
            float yTrans = event.getY() - mDragStartPoint.y;
            getGlobalMatrix().set(mSavedMatrix);
            getGlobalMatrix().postTranslate(xTrans, yTrans);
            if (!isOutOfBounds()) {
                invalidate();
            }
        }
    }

    private void performClick(MotionEvent event) {
        Log.d(Tag, "performClick()");
        float[] realPoint = calcRealPoint(event);
        float yAxisIncrement = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        mSelectedDataIndex = mDataSet.getCount() - (int) (realPoint[1] / yAxisIncrement) - 1;
        invalidate();
    }

    private boolean isOutOfBounds() {
        boolean isOutOfBoundsX = true, isOutOfBoundsY = true;
        getGlobalMatrix().mapPoints(getMappedBound(), getBound());
        if (getMappedBound()[0] > 0f) {
            getGlobalMatrix().postTranslate(-getMappedBound()[0], 0f);
        } else if (getMappedBound()[2] < getFrameWidth()) {
            float relative = getFrameWidth() - getMappedBound()[2];
            getGlobalMatrix().postTranslate(relative, 0f);
        } else {
            isOutOfBoundsX = false;
        }
        if (getMappedBound()[1] > 0f) {
            getGlobalMatrix().postTranslate(0f, -getMappedBound()[1]);
        } else if (getMappedBound()[3] < getFrameHeight()) {
            float relative = getFrameHeight() - getMappedBound()[3];
            getGlobalMatrix().postTranslate(0f, relative);
        } else {
            isOutOfBoundsY = false;
        }
        return isOutOfBoundsX & isOutOfBoundsY;
    }

    private void setZoomCenter(MotionEvent event) {
        float[] realPoint = calcRealPoint(event);
        mZoomCenterPoint = new PointF(realPoint[0], realPoint[1]);
    }

    private float[] calcRealPoint(MotionEvent event) {
        float centerX, centerY;
        try {
            centerX = (event.getX(0) + event.getX(1)) / 2 - mPaddingLeft;
            centerY = (event.getY(0) + event.getY(1)) / 2 - mPaddingTop;
        } catch (IllegalArgumentException e) {
            centerX = event.getX() - mPaddingLeft;
            centerY = event.getY() - mPaddingTop;
        }
        float visibleX = -getXAxisTrans() + centerX;
        float visibleY = -getYAxisTrans() + centerY;
        float realX = visibleX / getXAxisScale();
        float realY = visibleY / getYAxisScale();
        return new float[]{realX, realY};
    }

    private int calcCharacterHeight(Paint paint) {
        Rect r = new Rect();
        paint.getTextBounds("0", 0, 1, r);
        return r.height();
    }

    private float calcCharacterWidth(Paint paint) {
        return paint.measureText("0");
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    private Matrix getGlobalMatrix() {
        if (null == mGlobalMatrix) {
            mGlobalMatrix = new Matrix();
        }
        return mGlobalMatrix;
    }

    private float[] getMatrixValues() {
        if (null == mMatrixValues) {
            mMatrixValues = new float[9];
        }
        return mMatrixValues;
    }

    private float[] getMatrixPoint() {
        if (null == mMatrixPoint) {
            mMatrixPoint = new float[4];
        }
        return mMatrixPoint;
    }

    private float[] getBound() {
        if (null == mBound) {
            mBound = new float[]{0f, 0f, getFrameWidth(), getFrameHeight()};
        }
        return mBound;
    }

    private float[] getMappedBound() {
        if (null == mMappedBound) {
            mMappedBound = new float[4];
        }
        return mMappedBound;
    }

    private float getXAxisScale() {
        getGlobalMatrix().getValues(getMatrixValues());
        return getMatrixValues()[0];
    }

    private float getYAxisScale() {
        getGlobalMatrix().getValues(getMatrixValues());
        return getMatrixValues()[4];
    }

    private float getXAxisTrans() {
        getGlobalMatrix().getValues(getMatrixValues());
        return getMatrixValues()[2];
    }

    private float getYAxisTrans() {
        getGlobalMatrix().getValues(getMatrixValues());
        return getMatrixValues()[5];
    }

    private int getFrameWidth() {
        return mCanvasWidth - mPaddingLeft - mPaddingRight;
    }

    private int getFrameHeight() {
        return mCanvasHeight - mPaddingTop - mPaddingBottom;
    }

}
