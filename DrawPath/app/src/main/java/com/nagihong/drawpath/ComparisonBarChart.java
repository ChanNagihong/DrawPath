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
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nagihong.drawpath.DataSet.ComparisonBarChartDataSet;

import java.lang.ref.WeakReference;


/**
 * Created by channagihong on 2016/10/10.
 */

public class ComparisonBarChart extends View {

    private String Tag = ComparisonBarChart.class.getSimpleName();

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
    private ComparisonBarChartDataSet mDataSet = new ComparisonBarChartDataSet();
    private int mSelectedDataIndex = -1;

    private enum DATA_PART {
        LEFT,
        RIGHT,
        NONE
    }

    private DATA_PART mSelectedDataPart = DATA_PART.NONE;

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

    public ComparisonBarChart(Context context) {
        super(context);
        initVar();
    }

    public ComparisonBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public ComparisonBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public ComparisonBarChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        canvas.drawLine(mCanvasWidth - mPaddingRight, mPaddingTop, mCanvasWidth - mPaddingRight, mCanvasHeight - mPaddingBottom, mFramePaint);
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
        drawYAxisMark(canvas);
        drawXAxisMark(canvas);
        drawData(canvas);
    }

    private void drawXAxisMark(Canvas canvas) {
        float[] xs = mDataSet.calcXAxisGridLinesX(getFrameWidth(), getXAxisScale());
        int[] xMarks = mDataSet.getXAxisMarks();
        mMarkPaint.setColor(Color.BLACK);
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        getMatrixPoint()[0] = getFrameWidth() / 2;
        getGlobalMatrix().mapPoints(getMatrixPoint());
        if (getMatrixPoint()[0] > 0 && getMatrixPoint()[0] < getFrameWidth()) {
            canvas.drawText("0", mPaddingLeft + getMatrixPoint()[0] - characterWidth / 2, mCanvasHeight - mPaddingBottom + characterHeight + 10, mMarkPaint);
        }
        for (int i = 0; i < xs.length; i++) {
            float characterXOffset = String.valueOf(xMarks[i]).length() * characterWidth / 2;
            getMatrixPoint()[0] = xs[i];
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] < 0) {
                continue;
            }
            if (getMatrixPoint()[0] > getFrameWidth()) {
                break;
            }
            canvas.drawText(String.valueOf(xMarks[i]), mPaddingLeft + getMatrixPoint()[0] - characterXOffset, mCanvasHeight - mPaddingBottom + characterHeight + 10, mMarkPaint);
        }
    }

    private void drawYAxisMark(Canvas canvas) {
        mMarkPaint.setColor(Color.BLACK);
        float[] ys = mDataSet.calcYAxisGridLinesY(getFrameHeight(), getYAxisScale());
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        float yAxisIncrementLength = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        for (int i = 0; i < ys.length; i++) {
            getMatrixPoint()[1] = ys[i] + yAxisIncrementLength / 2;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[1] < 0) {
                continue;
            }
            if (getMatrixPoint()[1] > getFrameHeight()) {
                break;
            }
            String yMark = mDataSet.getYAxisMarks().get(i);
            float characterXOffset = yMark.length() * characterWidth;
            canvas.drawText(yMark, mPaddingLeft - characterXOffset - 10, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
            canvas.drawText(yMark, mCanvasWidth - mPaddingRight + 10, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
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

    private boolean isNeedToDrawDataValues() {
        float characterHeight = calcCharacterHeight(mValuePaint);
        float yAxisIncrementLength = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        getMatrixPoint()[1] = 0;
        getMatrixPoint()[3] = yAxisIncrementLength;
        getGlobalMatrix().mapPoints(getMatrixPoint());
        if (Math.abs(getMatrixPoint()[3] - getMatrixPoint()[1]) >= characterHeight) {
            return true;
        }
        return false;
    }

    private void drawRectangles(Canvas bitmapCanvas) {
        mRectanglePaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mDataSet.getCount(); i++) {
            RectF leftRect = mDataSet.getLeftRects().get(i);
            getMatrixPoint()[0] = leftRect.left;
            getMatrixPoint()[1] = leftRect.top;
            getMatrixPoint()[2] = leftRect.right;
            getMatrixPoint()[3] = leftRect.bottom;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] > getFrameWidth()
                    || getMatrixPoint()[2] < 0
                    || getMatrixPoint()[1] > getFrameHeight()) {
                continue;
            }
            if (getMatrixPoint()[3] < 0) {
                break;
            }
            mRectanglePaint.setColor(ColorList.getColors().get(0));
            bitmapCanvas.drawRect(getMatrixPoint()[0], getMatrixPoint()[1], getMatrixPoint()[2], getMatrixPoint()[3], mRectanglePaint);
            if (mSelectedDataPart == DATA_PART.LEFT
                    && i == mSelectedDataIndex) {
                mRectanglePaint.setColor(Color.BLACK);
                mRectanglePaint.setAlpha(70);
                bitmapCanvas.drawRect(getMatrixPoint()[0], getMatrixPoint()[1], getMatrixPoint()[2], getMatrixPoint()[3], mRectanglePaint);
                mRectanglePaint.setAlpha(255);
            }
        }
        for (int i = 0; i < mDataSet.getCount(); i++) {
            RectF rightRect = mDataSet.getRightRects().get(i);
            getMatrixPoint()[0] = rightRect.left;
            getMatrixPoint()[1] = rightRect.top;
            getMatrixPoint()[2] = rightRect.right;
            getMatrixPoint()[3] = rightRect.bottom;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] > getFrameWidth()
                    || getMatrixPoint()[2] < 0
                    || getMatrixPoint()[1] > getFrameHeight()) {
                continue;
            }
            if (getMatrixPoint()[3] < 0) {
                break;
            }
            mRectanglePaint.setColor(ColorList.getColors().get(1));
            bitmapCanvas.drawRect(getMatrixPoint()[0], getMatrixPoint()[1], getMatrixPoint()[2], getMatrixPoint()[3], mRectanglePaint);
            if (mSelectedDataPart == DATA_PART.RIGHT
                    && i == mSelectedDataIndex) {
                mRectanglePaint.setColor(Color.BLACK);
                mRectanglePaint.setAlpha(70);
                bitmapCanvas.drawRect(getMatrixPoint()[0], getMatrixPoint()[1], getMatrixPoint()[2], getMatrixPoint()[3], mRectanglePaint);
                mRectanglePaint.setAlpha(255);
            }
        }
    }

    private void drawDataValues(Canvas canvas) {
        int characterWidth = calcCharacterWidth(mValuePaint);
        int characterHeight = calcCharacterHeight(mValuePaint);
        float yAxisIncrementLength = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        for (int i = 0; i < mDataSet.getCount(); i++) {
            RectF leftRect = mDataSet.getLeftRects().get(i);
            getMatrixPoint()[0] = leftRect.left;
            getMatrixPoint()[1] = leftRect.top + yAxisIncrementLength / 2;
            getMatrixPoint()[2] = leftRect.right;
            getMatrixPoint()[3] = leftRect.bottom;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            String value = String.valueOf(mDataSet.getLeftDatas()[i]);
            float valueXOffset = value.length() * characterWidth;
            float startX = getMatrixPoint()[0] - valueXOffset;
            float startY = getMatrixPoint()[1] + characterHeight / 2;
            if (startX < 0
                    || getMatrixPoint()[0] > getFrameWidth()
                    || getMatrixPoint()[1] > getFrameHeight()) {
                continue;
            }
            if (getMatrixPoint()[3] < 0) {
                break;
            }
            canvas.drawText(value, startX + mPaddingLeft, startY + mPaddingTop, mValuePaint);
        }
        for (int i = 0; i < mDataSet.getCount(); i++) {
            RectF rightRect = mDataSet.getRightRects().get(i);
            getMatrixPoint()[0] = rightRect.left;
            getMatrixPoint()[1] = rightRect.top + yAxisIncrementLength / 2;
            getMatrixPoint()[2] = rightRect.right;
            getMatrixPoint()[3] = rightRect.bottom;
            getGlobalMatrix().mapPoints(getMatrixPoint());
            String value = String.valueOf(mDataSet.getRightDatas()[i]);
            float valueXOffset = value.length() * characterWidth;
            float startX = getMatrixPoint()[2];
            float startY = getMatrixPoint()[1] + characterHeight / 2;
            if (startX < 0
                    || getMatrixPoint()[2] > getFrameWidth()
                    || getMatrixPoint()[1] > getFrameHeight()) {
                continue;
            }
            if (getMatrixPoint()[3] < 0) {
                break;
            }
            canvas.drawText(value, startX + mPaddingLeft, startY + mPaddingTop, mValuePaint);
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
                mGlobalMatrix.setTranslate(1f, 1f);
            }
            checkBounds();
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
            checkBounds();
            invalidate();
        }
    }

    private void performClick(MotionEvent event) {
        Log.d(Tag, "performClick()");
        float[] realPoint = calcRealPoint(event);
        float yAxisIncrement = mDataSet.calcYAxisIncrementLength(getFrameHeight());
        mSelectedDataPart = DATA_PART.NONE;
        mSelectedDataIndex = (int) ((getFrameHeight() - realPoint[1]) / yAxisIncrement);
        RectF leftRect = mDataSet.getLeftRects().get(mSelectedDataIndex);
        getMatrixPoint()[0] = leftRect.left;
        getMatrixPoint()[2] = leftRect.right;
        getGlobalMatrix().mapPoints(getMatrixPoint());
        if (realPoint[0] > getMatrixPoint()[0]
                && realPoint[0] < getMatrixPoint()[2]) {
            mSelectedDataPart = DATA_PART.LEFT;
        }
        if (mSelectedDataPart != DATA_PART.NONE) {
            invalidate();
            return;
        }
        RectF rightRect = mDataSet.getRightRects().get(mSelectedDataIndex);
        getMatrixPoint()[0] = rightRect.left;
        getMatrixPoint()[2] = rightRect.right;
        if (realPoint[0] > getMatrixPoint()[0]
                && realPoint[0] < getMatrixPoint()[2]) {
            mSelectedDataPart = DATA_PART.RIGHT;
        }
        invalidate();
    }

    private void checkBounds() {
        getGlobalMatrix().mapPoints(getMappedBound(), getBound());
        if (getMappedBound()[0] > 0f) {
            getGlobalMatrix().postTranslate(-getMappedBound()[0], 0f);
        } else if (getMappedBound()[2] < getFrameWidth()) {
            float relative = getFrameWidth() - getMappedBound()[2];
            getGlobalMatrix().postTranslate(relative, 0f);
        }
        if (getMappedBound()[1] > 0f) {
            getGlobalMatrix().postTranslate(0f, -getMappedBound()[1]);
        } else if (getMappedBound()[3] < getFrameHeight()) {
            float relative = getFrameHeight() - getMappedBound()[3];
            getGlobalMatrix().postTranslate(0f, relative);
        }
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

    private int calcCharacterWidth(Paint paint) {
        return (int) paint.measureText("0");
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
