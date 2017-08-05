package com.nagihong.drawpath;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nagihong.drawpath.DataSet.Data;
import com.nagihong.drawpath.DataSet.LineChartDataSet;
import com.nagihong.drawpath.Utils.FloatFormatter;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by channagihong on 16/7/29.
 */
public class LineChartView extends View {

    private String Tag = LineChartView.class.getSimpleName();

    private enum MODE {
        NONE,
        ZOOM_MODE,
        DRAG_MODE
    }

    /**
     * all staffs relative to drawing canvas
     */
    private Paint mGridPaint, mFramePaint, mMarkPaint, mLinePaint, mDrawablePaint, mDataPointPaint, mValMarksPaint;
    private Path mGridPath, mFramePath, mMarkPath, mLinePath;
    private int mPaddingLeft = 80;
    private int mPaddingRight = 100;
    private int mPaddingTop = 50;
    private int mPaddingBottom = 100;
    private int mGridLineColor = Color.parseColor("#D4D4D4");
    private float mFramePaintStrokeWidth = 2.5f;
    private float mGridPaintStrokeWidth = 2.5f;

    /**
     * interval of background grid's line
     */
    private int mXAxisGridLength = 150;
    private int mYAxisGridLength = 200;
    private int mXAxisMaxGridLength = 100;
    private int mYAxisMaxGridLength = 100;

    /**
     * constants scale factor of the legend
     */
    private float mXAxisScaleFactor = 1.0f;
    private float mYAxisScaleFactor = 1.0f;

    /**
     * dynamic scale factor in  zoom mode
     */
    private float mXAxisZoomFactor = 1.0f;
    private float mYAxisZoomFactor = 1.0f;

    /**
     * cache the real legend size, and visible part's size of the legend
     */
    private RectF mVisibleRect = null;
    private RectF mRealRect = null;

    /**
     * when start zoom mode, mark down the distance of two fingers point
     */
    private float mDistCache;

    /**
     * mode trigger condition
     */
    private float mDragTriggerDist = 9;
    private float mScaleTriggerDist = 9;

    /**
     * zoom mode center position of two fingers
     */
    private PointF mZoomCenterPoint = new PointF(-1, -1);

    /**
     * zoom mode dynamic zoom center axis position in percentage of the real legend axis length
     */
    private float mZoomCenterXRatio = 0.5f;
    private float mZoomCenterYRatio = 0.5f;

    /**
     * cache canvas size
     */
    private int mCanvasWidth = 0, mCanvasHeight = 0;

    /**
     * legend operation mode
     */
    private MODE mMode = MODE.NONE;

    /**
     * mark grid line for test
     */
    private int mMarkLineX = -1;
    private int mMarkLineY = -1;

    /**
     * drag mode last touch position
     */
    private PointF mDragLastPoint = new PointF(0, 0);

    private WeakReference<Bitmap> mDrawBitmap;
    private Canvas mBitmapCanvas;
    private float[] mLineBuffer;

    private LineChartDataSet mDataSet = new LineChartDataSet();

    public LineChartView(Context context) {
        super(context);
        initVar();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVar();
    }

    private void initVar() {
        setWillNotDraw(false);
        mFramePaint = new Paint();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mGridPaint = new Paint();
        mGridPaint.setStyle(Paint.Style.STROKE);
        mMarkPaint = new Paint();
        mMarkPaint.setStyle(Paint.Style.STROKE);
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mDrawablePaint = new Paint();
        mDrawablePaint.setStyle(Paint.Style.FILL);
        mDataPointPaint = new Paint();
        mDataPointPaint.setStyle(Paint.Style.FILL);
        mValMarksPaint = new Paint();
        mValMarksPaint.setStyle(Paint.Style.STROKE);

        mFramePath = new Path();
        mGridPath = new Path();
        mMarkPath = new Path();
        mLinePath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        calcRealRect();
        calcVisibleRect();
        drawFrame(canvas);
        drawGridLine(canvas);
        drawData(canvas);
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

    private void drawFrame(Canvas canvas) {
        mFramePath.reset();
        mFramePath.moveTo(mPaddingLeft, mPaddingTop);
        mFramePath.lineTo(canvas.getWidth() - mPaddingRight, mPaddingTop);
        mFramePath.moveTo(mPaddingLeft, mPaddingTop);
        mFramePath.lineTo(mPaddingLeft, canvas.getHeight() - mPaddingBottom);
        mFramePath.close();
        mFramePaint.setStrokeWidth(mFramePaintStrokeWidth);
        mFramePaint.setColor(mGridLineColor);
        mFramePaint.setPathEffect(null);
        canvas.drawPath(mFramePath, mFramePaint);
    }

    private void drawGridLine(Canvas canvas) {
        DashPathEffect effect = new DashPathEffect(new float[]{10.0f, 10.0f}, 0);
        mGridPaint.setStrokeWidth(mGridPaintStrokeWidth);
        mGridPaint.setPathEffect(effect);
        mGridPaint.setColor(mGridLineColor);
        mMarkPaint.setColor(Color.RED);
        mGridPath = new Path();
        mMarkPath = new Path();
        drawVerticalGridLine(canvas);
        drawHorizontalGridLine(canvas);
    }

    private void drawHorizontalGridLine(Canvas canvas) {
        int yAxisGridLength = getYAxisGridLength();
        for (int y = yAxisGridLength; y < mRealRect.bottom; y += yAxisGridLength) {
            if (y > mVisibleRect.top && y < mVisibleRect.bottom) {
                float relativeY = y - mVisibleRect.top;
                mGridPath.moveTo(mPaddingLeft, relativeY + mPaddingTop);
                mGridPath.lineTo(canvas.getWidth() - mPaddingRight, relativeY + mPaddingTop);
                canvas.drawPath(mGridPath, mGridPaint);
            }
        }
        if (mMarkLineY > mVisibleRect.top && mMarkLineY < mVisibleRect.bottom) {
            mMarkPath.moveTo(mPaddingLeft, mMarkLineY - mVisibleRect.top);
            mMarkPath.lineTo(canvas.getWidth() - mPaddingRight, mMarkLineY - mVisibleRect.top);
            canvas.drawPath(mMarkPath, mMarkPaint);
        }
    }

    private void drawVerticalGridLine(Canvas canvas) {
        int xAxisGridLength = getXAxisGridLength();
        for (int x = xAxisGridLength; x < mRealRect.right; x += xAxisGridLength) {
            if (x > mVisibleRect.left && x < mVisibleRect.right) {
                float relativeX = x - mVisibleRect.left;
                mGridPath.moveTo(relativeX + mPaddingLeft, mPaddingTop);
                mGridPath.lineTo(relativeX + mPaddingLeft, canvas.getHeight() - mPaddingBottom);
                canvas.drawPath(mGridPath, mGridPaint);
            }
        }
        if (mMarkLineX > mVisibleRect.left && mMarkLineX < mVisibleRect.right) {
            mMarkPath.moveTo(mMarkLineX - mVisibleRect.left, mPaddingTop);
            mMarkPath.lineTo(mMarkLineX - mVisibleRect.left, canvas.getHeight() - mPaddingBottom);
            canvas.drawPath(mMarkPath, mMarkPaint);
        }
    }

    private void drawHorizontalValMarks(Canvas canvas) {
        int xAxisGridLength = getXAxisGridLength();
        int maxXAxisVal = mDataSet.getCount();
        float xAxisGraduated = mRealRect.right / maxXAxisVal;
        float xAxisStepGraduatedVal = xAxisGridLength / xAxisGraduated;

        mValMarksPaint.setColor(Color.BLACK);
        mValMarksPaint.setTextSize(30f);
        /**
         * calculate text y-axis offset
         */
        int characterHeight = calcCharacterHeight(mValMarksPaint);
        int characterWidth = calcCharacterWidth(mValMarksPaint);

        for (int x = 0, i = 0; x <= mRealRect.right; x += xAxisGridLength) {
            if (x >= mVisibleRect.left && x <= mVisibleRect.right) {
                float val = i * xAxisStepGraduatedVal;
                float relativeX = x - mVisibleRect.left;
                float characterXOffset = FloatFormatter.format(val).length() * characterWidth / 2;
                canvas.drawText(FloatFormatter.format(val), relativeX + mPaddingLeft - characterXOffset, mPaddingTop - characterHeight, mValMarksPaint);
            }
            i++;
        }
    }

    private void drawVerticalValMarks(Canvas canvas) {
        int yAxisGridLength = getYAxisGridLength();
        int yAxisGraduated = (int) (mRealRect.bottom / (mDataSet.getMaxMarkVal() - mDataSet.getMinMarkVal()));
        int yAxisStepGraduatedVal = yAxisGridLength / yAxisGraduated;

        mValMarksPaint.setColor(Color.BLACK);
        mValMarksPaint.setTextSize(30f);
        int characterHeight = calcCharacterHeight(mValMarksPaint);
        int characterWidth = calcCharacterWidth(mValMarksPaint);

        for (int y = 0, i = 0; y <= mRealRect.bottom; y += yAxisGridLength) {
            if (y >= mVisibleRect.top && y <= mVisibleRect.bottom) {
                float relativeY = y - mVisibleRect.top;
                int val = mDataSet.getMaxMarkVal() - i * yAxisStepGraduatedVal;
                float characterXOffset = String.valueOf(val).length() * characterWidth;
                canvas.drawText(String.valueOf(val), mPaddingLeft - characterXOffset, relativeY + mPaddingTop + characterHeight / 2, mValMarksPaint);
            }
            i++;
        }
    }

    private void drawData(Canvas canvas) {
        DashPathEffect effect = new DashPathEffect(new float[]{10.0f, 10.0f}, 0);
        mLinePaint.setPathEffect(effect);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(2);
        mDataPointPaint.setColor(Color.BLACK);
        int width = mCanvasWidth - mPaddingLeft - mPaddingRight;
        int height = mCanvasHeight - mPaddingTop - mPaddingBottom;
        if (mDrawBitmap == null) {
            if (width > 0 && height > 0) {
                mDrawBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }
        mDataSet.calc(canvas, mBitmapCanvas, mYAxisGridLength, mXAxisGridLength, mYAxisScaleFactor, mYAxisZoomFactor, mXAxisScaleFactor, mXAxisZoomFactor);
        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
        drawZeroLineOnBitmap(canvas, mBitmapCanvas);
        drawDataLinesOnBitmap(mBitmapCanvas);
        drawDataPointsOnBitmap(mBitmapCanvas);
        drawColorShadowsOnBitmap(mBitmapCanvas);
        drawVerticalValMarks(canvas);
        drawHorizontalValMarks(canvas);
        canvas.drawBitmap(mDrawBitmap.get(), mPaddingLeft, mPaddingTop, mDrawablePaint);
    }

    private void drawZeroLineOnBitmap(Canvas canvas, Canvas drawableCanvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        canvas.drawLine(mPaddingLeft, mDataSet.getYAxisZero() + mPaddingTop, canvas.getWidth() - mPaddingRight, mDataSet.getYAxisZero() + mPaddingTop, paint);
        paint.setColor(Color.GREEN);
        canvas.drawLine(mPaddingLeft, mDataSet.getYAxisBase() + mPaddingTop, canvas.getWidth() - mPaddingRight, mDataSet.getYAxisBase() + mPaddingTop, paint);
    }

    private void drawDataLinesOnBitmap(Canvas drawableCanvas) {
        LinkedList<PointF> list = new LinkedList<>();

        if (0 >= mVisibleRect.left && drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor <= Math.ceil(mVisibleRect.bottom) && drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor >= Math.floor(mVisibleRect.top)) {
            PointF point = new PointF();
            point.x = 0f;
            point.y = drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor;
            list.add(point);
        }
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            float x = data.getIndex() * mDataSet.getXAxisGraduated();
            float y = mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated();
            if (x >= mVisibleRect.left && x <= mVisibleRect.right && y >= mVisibleRect.top && y <= mVisibleRect.bottom) {
                list.add(new PointF(x, y));
            }
        }
        Data lastData = mDataSet.getLastData();
        float lastX = drawableCanvas.getWidth() * mXAxisScaleFactor * mXAxisZoomFactor;
        float lastY = mDataSet.getYAxisZero() - lastData.getValue() * mDataSet.getYAxisGraduated();
        if (lastX <= mVisibleRect.right && lastY <= mVisibleRect.bottom && lastY >= mVisibleRect.top) {
            list.add(new PointF(lastX, lastY));
        }
        Log.d(Tag, "darwDataLinesOnBitmap() -- point count : " + list.size());
        int pointCount = (list.size() - 1) * 2;
        int arrayLength = pointCount * 2 + 6;
        if (mLineBuffer == null || mLineBuffer.length != arrayLength) {
            mLineBuffer = new float[arrayLength];
        }
        PointF cachePoint = null;
        for (int i = 0, index = 0; i < list.size(); i++) {
            Log.d(Tag, "--------------------------------------");
            Log.d(Tag, "index : " + index);
            if(0 == i) {
                mLineBuffer[index++] = 0;
                mLineBuffer[index++] = drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor;
            }
            if(null != cachePoint && i != list.size() - 1) {
                Log.d(Tag, "last x : " +  + cachePoint.x);
                mLineBuffer[index++] = cachePoint.x;
                Log.d(Tag, "last y : " +  + cachePoint.y);
                mLineBuffer[index++] = cachePoint.y;
            }
            Log.d(Tag, "this x : " + list.get(i).x);
            mLineBuffer[index++] = list.get(i).x;
            Log.d(Tag, "this y : " + list.get(i).y);
            mLineBuffer[index++] = list.get(i).y;
            cachePoint = list.get(i);
            if(i == list.size() - 1) {
                Log.d(Tag, "the last x : " + list.get(i).x);
                Log.d(Tag, "the last y : " + list.get(i).y);
                mLineBuffer[index++] = list.get(i).x;
                mLineBuffer[index++] = list.get(i).y;
                Log.d(Tag, "the end x : " + drawableCanvas.getWidth() * mXAxisScaleFactor * mXAxisZoomFactor);
                Log.d(Tag, "the end y : " + drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor);
                mLineBuffer[index++] = drawableCanvas.getWidth() * mXAxisScaleFactor * mXAxisZoomFactor;
                mLineBuffer[index++] = drawableCanvas.getHeight() * mYAxisScaleFactor * mYAxisZoomFactor;
            }
        }
//        mLineBuffer[0] = 0;
//        mLineBuffer[1] = drawableCanvas.getHeight();
//        int mark = 2;
//        /**
//         * this line buffer array is stores point to point data,
//         * like [X1, Y1, X2, Y2, X2, Y2, X3, Y3, X3, Y3, X4, Y4……]
//         */
//        for (int i = 0; i < mDataSet.getCount(); i++) {
//            Data data = mDataSet.getDatas().get(i);
//            if (i != 0) {
//                int currentmark = mark;
//                mLineBuffer[mark++] = mLineBuffer[currentmark - 2];
//                mLineBuffer[mark++] = mLineBuffer[currentmark - 1];
//            }
//            mLineBuffer[mark++] = data.getIndex() * mDataSet.getXAxisGraduated();
//            mLineBuffer[mark++] = mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated();
//        }
//        Data data = mDataSet.getLastData();
//        mLineBuffer[mark++] = data.getIndex() * mDataSet.getXAxisGraduated();
//        mLineBuffer[mark++] = mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated();
//        mLineBuffer[mark++] = drawableCanvas.getWidth();
//        mLineBuffer[mark++] = mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated();
        drawableCanvas.drawLines(mLineBuffer, 0, mLineBuffer.length, mLinePaint);
    }

    private void drawDataPointsOnBitmap(Canvas drawableCanvas) {
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            drawableCanvas.drawCircle(data.getIndex() * mDataSet.getXAxisGraduated(), mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated(), 6, mDataPointPaint);
        }
    }

    private void drawColorShadowsOnBitmap(Canvas drawableCanvas) {
        Path path = new Path();
        path.moveTo(0, drawableCanvas.getHeight());
        for (int i = 0; i < mDataSet.getCount(); i++) {
            Data data = mDataSet.getDatas().get(i);
            path.lineTo(data.getIndex() * mDataSet.getXAxisGraduated(), mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated());
        }
        Data data = mDataSet.getLastData();
        path.lineTo(drawableCanvas.getWidth(), mDataSet.getYAxisZero() - data.getValue() * mDataSet.getYAxisGraduated());
        path.lineTo(drawableCanvas.getWidth(), drawableCanvas.getHeight());
        path.close();
        int color = (100 << 24) | (Color.RED & 0xffffff);
        drawableCanvas.save();
        drawableCanvas.clipPath(path);
        drawableCanvas.drawColor(color);
        drawableCanvas.restore();
    }

    private int getXAxisGridLength() {
        float scale = 1 + mXAxisScaleFactor * mXAxisZoomFactor % 1;
        return (int) (mXAxisGridLength * scale);
    }

    private int getYAxisGridLength() {
        float scale = 1 + mYAxisScaleFactor * mYAxisZoomFactor % 1;
        return (int) (mYAxisGridLength * scale);
    }

    private int getMarkGridLineX() {
        float scale = 1 + mXAxisScaleFactor * mXAxisZoomFactor;
        return (int) (mMarkLineX * scale);
    }

    private int getMarkGridLineY() {
        float scale = 1 + mYAxisScaleFactor * mYAxisZoomFactor;
        return (int) (mMarkLineY * scale);
    }

    private float getXDist(MotionEvent e) {
        float x = Math.abs(e.getX(0) - e.getX(1));
        return x;
    }

    private float getYDist(MotionEvent e) {
        float y = Math.abs(e.getY(0) - e.getY(1));
        return y;
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

    private void performZoom(MotionEvent event) {
        float dist = spacing(event);
        if (Math.abs(mDistCache - dist) >= mScaleTriggerDist) {
            float scale = dist / mDistCache;
            mXAxisZoomFactor = scale;
            mYAxisZoomFactor = scale;
            if (mXAxisScaleFactor * mXAxisZoomFactor < 1.0f) {
                mXAxisZoomFactor = 1.0f / mXAxisScaleFactor;
            }
            if (mYAxisScaleFactor * mYAxisZoomFactor < 1.0f) {
                mYAxisZoomFactor = 1.0f / mYAxisScaleFactor;
            }
            invalidate();
        }
    }

    private void performDrag(MotionEvent event) {
        float dist = spacing(event.getX(), event.getY(), mDragLastPoint.x, mDragLastPoint.y);
        if (dist >= mDragTriggerDist) {
            int visibleRectWidth = mCanvasWidth - mPaddingLeft - mPaddingRight;
            int visibleRectHeight = mCanvasHeight - mPaddingTop - mPaddingBottom;
            float dealtaX = mDragLastPoint.x - event.getX();
            float dealtaY = mDragLastPoint.y - event.getY();
            mDragLastPoint.set(event.getX(), event.getY());
            mVisibleRect.left += dealtaX;
            mVisibleRect.top += dealtaY;
            mVisibleRect.right = mVisibleRect.left + visibleRectWidth;
            mVisibleRect.bottom = mVisibleRect.top + visibleRectHeight;
            checkVisibleRect(visibleRectWidth, visibleRectHeight);
            invalidate();
        }
    }

    private void startZoomMode(MotionEvent event) {
        Log.d(Tag, "startZoomMode()");
        mMode = MODE.ZOOM_MODE;
        mDistCache = spacing(event);
        setZoomCenter(event);
    }

    private void endZoomMode() {
        Log.d(Tag, "endZoomMode()");
        mMode = MODE.NONE;
        mXAxisScaleFactor *= mXAxisZoomFactor;
        mYAxisScaleFactor *= mYAxisZoomFactor;
        mXAxisZoomFactor = 1.0f;
        mYAxisZoomFactor = 1.0f;
        mZoomCenterXRatio = 0.5f;
        mZoomCenterYRatio = 0.5f;
        mZoomCenterPoint.set(-1, -1);
        invalidate();
    }

    private void startDragMode(MotionEvent event) {
        Log.d(Tag, "startDragMode()");
        mMode = MODE.DRAG_MODE;
        mDragLastPoint.set(event.getX(), event.getY());
    }

    private void endDragMode(MotionEvent event) {
        Log.d(Tag, "endDragMode()");
        mMode = MODE.NONE;
        mDragLastPoint.set(0, 0);
        invalidate();
    }

    private void setZoomCenter(MotionEvent event) {
        mZoomCenterPoint.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
        int realX = (int) (mZoomCenterPoint.x + mVisibleRect.left);
        int realY = (int) (mZoomCenterPoint.y + mVisibleRect.top);
        mZoomCenterXRatio = realX / mRealRect.right;
        mZoomCenterYRatio = realY / mRealRect.bottom;
    }

    private void calcRealRect() {
        int visibleRectWidth = mCanvasWidth - mPaddingLeft - mPaddingRight;
        int visibleRectHeight = mCanvasHeight - mPaddingTop - mPaddingBottom;
        int realRectWidth = (int) (visibleRectWidth * mXAxisScaleFactor * mXAxisZoomFactor);
        int realRectHeight = (int) (visibleRectHeight * mYAxisScaleFactor * mYAxisZoomFactor);
        mMarkLineX = (int) (realRectWidth / 2 + 100 * mXAxisScaleFactor * mXAxisZoomFactor);
        mMarkLineY = (int) (realRectHeight / 2 + 100 * mYAxisScaleFactor * mYAxisZoomFactor);
        if (null == mRealRect) {
            mRealRect = new RectF(0, 0, realRectWidth, realRectHeight);
        } else {
            mRealRect.set(0, 0, realRectWidth, realRectHeight);
        }
    }

    private void calcVisibleRect() {
        int visibleRectWidth = mCanvasWidth - mPaddingLeft - mPaddingRight;
        int visibleRectHeight = mCanvasHeight - mPaddingTop - mPaddingBottom;
        if (null == mVisibleRect) {
            float realRectCenterX = mRealRect.right / 2;
            float realRectCenterY = mRealRect.bottom / 2;
            mVisibleRect = new RectF(
                    realRectCenterX - visibleRectWidth / 2,
                    realRectCenterY - visibleRectHeight / 2,
                    realRectCenterX + visibleRectWidth / 2,
                    realRectCenterY + visibleRectHeight / 2
            );
        } else {
            if (mMode == MODE.ZOOM_MODE) {
                float realX = mRealRect.right * mZoomCenterXRatio;
                float realY = mRealRect.bottom * mZoomCenterYRatio;
                mVisibleRect.left = realX - mZoomCenterPoint.x;
                mVisibleRect.top = realY - mZoomCenterPoint.y;
                mVisibleRect.right = mVisibleRect.left + visibleRectWidth;
                mVisibleRect.bottom = mVisibleRect.top + visibleRectHeight;
                checkVisibleRect(visibleRectWidth, visibleRectHeight);
            }
        }
    }

    private void checkVisibleRect(int visibleRectWidth, int visibleRectHeight) {
        if (mVisibleRect.top < mRealRect.top) {
            mVisibleRect.top = mRealRect.top;
            mVisibleRect.bottom = mVisibleRect.top + visibleRectHeight;
        }
        if (mVisibleRect.bottom > mRealRect.bottom) {
            mVisibleRect.bottom = mRealRect.bottom;
            mVisibleRect.top = mVisibleRect.bottom - visibleRectHeight;
        }
        if (mVisibleRect.left < mRealRect.left) {
            mVisibleRect.left = mRealRect.left;
            mVisibleRect.right = mVisibleRect.left + visibleRectWidth;
        }
        if (mVisibleRect.right > mRealRect.right) {
            mVisibleRect.right = mRealRect.right;
            mVisibleRect.left = mVisibleRect.right - visibleRectWidth;
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

}
