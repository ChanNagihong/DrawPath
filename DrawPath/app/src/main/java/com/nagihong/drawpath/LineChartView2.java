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
import com.nagihong.drawpath.DataSet.LineChartDataSet2;
import com.nagihong.drawpath.Utils.FloatFormatter;

import java.lang.ref.WeakReference;

/**
 * Created by channagihong on 2016/9/29.
 */

public class LineChartView2 extends View {

    private String Tag = LineChartView2.class.getSimpleName();

    /**
     * normal staffs
     */
    private int mCanvasWidth;
    private int mCanvasHeight;

    /**
     * all staffs relative to background gridlines
     */
    private int mGridLineColor = Color.parseColor("#D4D4D4");
    private Paint mGridPaint, mMarkPaint;
    private float mGridPaintStrokeWidth = 2.5f;
    private Path mGridPath;

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
     * all staff relative to draw datalines
     */
    private Paint mLinePaint, mBitmapPaint, mPointPaint;
    private WeakReference<Bitmap> mDrawBitmap;
    private Canvas mBitmapCanvas;

    /**
     * all staffs relative to data
     */
    private LineChartDataSet2 mDataSet = new LineChartDataSet2();

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

    public LineChartView2(Context context) {
        super(context);
        initVar();
    }

    public LineChartView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public LineChartView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public LineChartView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVar();
    }

    private void initVar() {
        setWillNotDraw(false);
        mFramePaint = new Paint();
        mGridPaint = new Paint();
        mMarkPaint = new Paint();
        mLinePaint = new Paint();
        mBitmapPaint = new Paint();
        mPointPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        drawFrame(canvas);
        drawGridLines(canvas);
        drawData(canvas);
    }

    private void drawFrame(Canvas canvas) {
        mFramePaint.reset();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(mFramePaintStrokeWidth);
        mFramePaint.setColor(mGridLineColor);
        canvas.drawLine(mPaddingLeft, mPaddingTop, canvas.getWidth() - mPaddingRight, mPaddingTop, mFramePaint);
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
        drawYAxisGridLines(canvas);
    }

    private void drawXAxisGridLines(Canvas canvas) {
        float[] xs = mDataSet.calcXAxisGridLinesX(getXAxisScale(), getFrameWidth());
        for (int i = 0; i < xs.length; i++) {
            getMatrixPoint()[0] = xs[i];
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[0] > getFrameWidth() || getMatrixPoint()[0] < 0) {
                continue;
            }
            mGridPath.moveTo(getMatrixPoint()[0] + mPaddingLeft, mPaddingTop);
            mGridPath.lineTo(getMatrixPoint()[0] + mPaddingLeft, mCanvasHeight - mPaddingBottom);
            canvas.drawPath(mGridPath, mGridPaint);
            mGridPath.reset();
        }
        drawXAxisMark(canvas, xs);
    }

    private void drawYAxisGridLines(Canvas canvas) {
        float[] ys = mDataSet.calcYAxisGridLinesY(getYAxisScale(), getFrameHeight());
        for (int i = 0; i < ys.length; i++) {
            getMatrixPoint()[1] = ys[i];
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if (getMatrixPoint()[1] > getFrameHeight() || getMatrixPoint()[1] < 0) {
                continue;
            }
            canvas.drawLine(mPaddingLeft, getMatrixPoint()[1] + mPaddingTop, canvas.getWidth() - mPaddingRight, getMatrixPoint()[1] + mPaddingTop, mGridPaint);
        }
        drawYAxisMark(canvas, ys);
    }

    private void drawXAxisMark(Canvas canvas, float[] xs) {
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        int xAxisIncrementVal = mDataSet.calcXAxisMarkIncrementVal(getXAxisScale());
        for (int i = -1; i < xs.length; i++) {
            if (i == -1) {
                getMatrixPoint()[0] = 0f;
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[0] < 0) {
                    continue;
                }
                canvas.drawText("0", mPaddingLeft + getMatrixPoint()[0] - characterWidth / 2, mPaddingTop - characterHeight, mMarkPaint);
            } else {
                int mark = xAxisIncrementVal * (i + 1);
                float characterXOffset = FloatFormatter.format(mark).length() * characterWidth / 2;
                getMatrixPoint()[0] = xs[i];
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[0] < 0 || getMatrixPoint()[0] > getFrameWidth()) {
                    continue;
                }
                canvas.drawText(String.valueOf(mark), mPaddingLeft + getMatrixPoint()[0] - characterXOffset, mPaddingTop - characterHeight, mMarkPaint);
            }
        }
    }

    private void drawYAxisMark(Canvas canvas, float[] ys) {
        float characterHeight = calcCharacterHeight(mMarkPaint);
        float characterWidth = calcCharacterWidth(mMarkPaint);
        float yAxisIncrementVal = mDataSet.calcYAxisMarkIncrementVal(getYAxisScale());
        float maxVal = (int) mDataSet.calcMaxVal();
        boolean showDecimal = mDataSet.isYAxisMarkShowInDecimal(getYAxisScale());
        if (!showDecimal) {
            maxVal = Math.round(maxVal);
        }
        for (int i = -1; i < ys.length; i++) {
            if (i == -1) {
                getMatrixPoint()[1] = 0f;
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[1] < 0) {
                    continue;
                }
                float characterXOffset = FloatFormatter.format(maxVal).length() * characterWidth;
                canvas.drawText(FloatFormatter.format(maxVal), mPaddingLeft - characterXOffset, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
            } else {
                getMatrixPoint()[1] = ys[i];
                getGlobalMatrix().mapPoints(getMatrixPoint());
                if (getMatrixPoint()[1] > getFrameHeight() || getMatrixPoint()[1] < 0) {
                    continue;
                }
                if (showDecimal) {
                    float mark = maxVal - yAxisIncrementVal * (i + 1);
                    float characterXOffset = FloatFormatter.format(mark).length() * characterWidth;
                    canvas.drawText(String.valueOf(mark), mPaddingLeft - characterXOffset, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
                } else {
                    int mark = Math.round(maxVal - yAxisIncrementVal * (i + 1));
                    float characterXOffset = String.valueOf(mark).length() * characterWidth;
                    canvas.drawText(String.valueOf(mark), mPaddingLeft - characterXOffset, mPaddingTop + getMatrixPoint()[1] + characterHeight / 2, mMarkPaint);
                }
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
        drawDataLines(mBitmapCanvas);
        drawDataPoints(mBitmapCanvas);
        drawColorShadows(mBitmapCanvas);
        canvas.drawBitmap(mDrawBitmap.get(), mPaddingLeft, mPaddingTop, mBitmapPaint);
    }

    private void drawDataLines(Canvas bitmapCanvas) {
        DashPathEffect effect = new DashPathEffect(new float[]{10.0f, 10.0f}, 0);
        mLinePaint.setPathEffect(effect);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(2);
        int pointCount = (mDataSet.getCount() - 1) * 2;
        int pointArrLength = pointCount * 2;
        float[] mLineBuffer = new float[pointArrLength];
        int index = 0;
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            if (i == 0 || i == mDataSet.getCount() - 1) {
                mLineBuffer[index++] = data.getX();
                mLineBuffer[index++] = data.getY();
            } else {
                mLineBuffer[index++] = data.getX();
                mLineBuffer[index++] = data.getY();
                mLineBuffer[index++] = data.getX();
                mLineBuffer[index++] = data.getY();
            }
        }
        getGlobalMatrix().mapPoints(mLineBuffer);
        bitmapCanvas.drawLines(mLineBuffer, 0, mLineBuffer.length, mLinePaint);
    }

    private void drawDataPoints(Canvas bitmapCanvas) {
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setColor(Color.BLACK);
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            getMatrixPoint()[0] = data.getX();
            getMatrixPoint()[1] = data.getY();
            getGlobalMatrix().mapPoints(getMatrixPoint());
            bitmapCanvas.drawCircle(getMatrixPoint()[0], getMatrixPoint()[1], 6, mPointPaint);
        }
    }

    private void drawColorShadows(Canvas bitmapCanvas) {
        Path path = new Path();
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            getMatrixPoint()[0] = data.getX();
            getMatrixPoint()[1] = data.getY();
            getGlobalMatrix().mapPoints(getMatrixPoint());
            if(i == 0) {
                path.moveTo(getMatrixPoint()[0], getFrameHeight());
            }
            path.lineTo(getMatrixPoint()[0], getMatrixPoint()[1]);
            if (i == mDataSet.getCount() - 1) {
                path.lineTo(getMatrixPoint()[0], getFrameHeight());
            }
        }
        path.close();
        int color = (80 << 24) | (Color.RED & 0xffffff);
        bitmapCanvas.save();
        bitmapCanvas.clipPath(path);
        bitmapCanvas.drawColor(color);
        bitmapCanvas.restore();
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
            float ratio = dist / mDistCache;
            getGlobalMatrix().set(mSavedMatrix);
            getGlobalMatrix().postScale(ratio, ratio, mZoomCenterPoint.x, mZoomCenterPoint.y);
            if(getXAxisScale() < 1f) {
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
            float xTrans = event.getX() - mDragStartPoint.x;
            float yTrans = event.getY() - mDragStartPoint.y;
            getGlobalMatrix().set(mSavedMatrix);
            getGlobalMatrix().postTranslate(xTrans, yTrans);
            checkBounds();
            invalidate();
        } else {
            Log.d(Tag, "performDrag() -- dist < mDragTriggerDist");
        }
    }

    private void checkBounds() {
        getGlobalMatrix().mapPoints(getMappedBound(), getBound());
        if(getMappedBound()[0] > 0f) {
            getGlobalMatrix().postTranslate(-getMappedBound()[0], 0f);
        } else if(getMappedBound()[2] < getFrameWidth()) {
            float relative = getFrameWidth() - getMappedBound()[2];
            getGlobalMatrix().postTranslate(relative, 0f);
        }
        if(getMappedBound()[1] > 0f) {
            getGlobalMatrix().postTranslate(0f, -getMappedBound()[1]);
        } else if(getMappedBound()[3] < getFrameHeight()){
            float relative = getFrameHeight() - getMappedBound()[3];
            getGlobalMatrix().postTranslate(0f, relative);
        }
    }

    private void setZoomCenter(MotionEvent event) {
        float centerX = (event.getX(0) + event.getX(1)) / 2 - mPaddingLeft;
        float centerY = (event.getY(0) + event.getY(1)) / 2 - mPaddingTop;
        float visibleX = -getXAxisTrans() + centerX;
        float visibleY = -getYAxisTrans() + centerY;
        float realX = visibleX / getXAxisScale();
        float realY = visibleY / getYAxisScale();
        mZoomCenterPoint = new PointF(realX, realY);
    }

    @Override
    protected void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();
    }

    private void release() {
        if(mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if(mDrawBitmap != null) {
            mDrawBitmap.get().recycle();
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
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
        if(null == mMatrixPoint) {
            mMatrixPoint = new float[2];
        }
        return mMatrixPoint;
    }

    private float[] getBound() {
        if(null == mBound) {
            mBound = new float[] {0f, 0f, getFrameWidth(), getFrameHeight()};
        }
        return mBound;
    }

    private float[] getMappedBound() {
        if(null == mMappedBound) {
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
