package com.nagihong.drawpath;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by channagihong on 16/7/29.
 */
public class GridFrameView extends View {

    private String Tag = GridFrameView.class.getSimpleName();

    private enum MODE {
        NONE,
        ZOOM_MODE,
        DRAG_MODE
    }

    /**
     * all staffs relative to drawing canvas
     */
    private Paint mGridPaint, mFramePaint, mMarkPaint;
    private Path mGridPath, mFramePath, mMarkPath;
    private int mPaddingLeft = 50;
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

    public GridFrameView(Context context) {
        super(context);
        initVar();
    }

    public GridFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVar();
    }

    public GridFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
    }

    public GridFrameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        mFramePath = new Path();
        mGridPath = new Path();
        mMarkPath = new Path();
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
            int realRectCenterX = (int) (mRealRect.right / 2);
            int realRectCenterY = (int) (mRealRect.bottom / 2);
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

}
