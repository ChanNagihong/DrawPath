package com.nagihong.drawpath;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by channagihong on 2016/9/23.
 */
public class TestView extends View {

    private String Tag = TestView.class.getSimpleName();

    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    long countDraw = 0;
    WeakReference<Bitmap> mDrawBitmap = null;
    Canvas mBitmapCanvas = null;
    Matrix mMatrix = null;

    public static float scale = 1f;
    public static float xTrans = 0f;
    public static float yTrans = 0f;
    float mLastX = -1f;
    float mLastY = -1f;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBitmap == null) {
            if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
                mDrawBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(Color.BLACK);
        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
        LinkedList<PointF> points = new LinkedList<>();
        points.add(new PointF(100, 200));
        points.add(new PointF(200, 600));
        points.add(new PointF(300, 400));
        points.add(new PointF(400, 100));
        points.add(new PointF(500, 800));
        points.add(new PointF(600, 300));
        points.add(new PointF(700, 700));
        float[] pointsArr = new float[points.size() * 4 + 4];
        pointsArr[0] = 0;
        pointsArr[1] = 0;
        int index = 2;
        for (int i = 0; i < points.size(); i++) {
            pointsArr[index++] = points.get(i).x;
            pointsArr[index++] = points.get(i).y;
            pointsArr[index++] = points.get(i).x;
            pointsArr[index++] = points.get(i).y;
        }
        pointsArr[index++] = mBitmapCanvas.getWidth() - 90;
        pointsArr[index++] = 100;
        Log.d(Tag, "scale : " + scale);
        Log.d(Tag, "trans : " + xTrans + " , " + yTrans);
        mMatrix = new Matrix();
//        matrix.preScale(1.5f, 1.5f);
        mMatrix.preScale(scale, scale);
        mMatrix.postTranslate(xTrans, yTrans);
        mMatrix.mapPoints(pointsArr);
        mBitmapCanvas.drawLines(pointsArr, paint);
//        mBitmapCanvas.drawLine(0, 0, mBitmapCanvas.getWidth(), mBitmapCanvas.getHeight(), paint);
        Paint drawablePaint = new Paint();
        drawablePaint.setStyle(Paint.Style.FILL);
        canvas.drawBitmap(mDrawBitmap.get(), 0, 0, drawablePaint);
        Matrix matrix1 = new Matrix();
        matrix1.postScale(2f, 2f, 50f, 50f);
        matrix1.setScale(1f, 1f);
        matrix1.setTranslate(0f, 0f);
        Log.d(Tag, "matrix1 : " + matrix1.toShortString());
    }

}
