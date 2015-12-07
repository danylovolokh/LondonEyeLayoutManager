package com.volokh.danylo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

import com.volokh.danylo.layoutmanager.circle_helper.circle_points_creator.FirstQuadrantCirclePointsCreator;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;

import java.util.LinkedHashMap;

/**
 * Created by danylo.volokh on 10/31/2015.
 */
public class DebugRecyclerView extends RecyclerView {

    private static final String TAG = DebugRecyclerView.class.getSimpleName();
    private Paint mPaint;
    private Paint mPaint2;

    private int mRadius;

    private int mXOrigin;
    private int mYOrigin;

    private FirstQuadrantCirclePointsCreator mCirclePointsCreator;

    private LinkedHashMap<Integer, Point> mCircleIndexPoint;
    private LinkedHashMap<Point, Integer> mCirclePointIndex;

    public DebugRecyclerView(Context context) {
        super(context);
        init();
    }

    public DebugRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public DebugRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setParameters(int radius, int xOrigin, int yOrigin){
        mRadius = radius;
        mXOrigin = xOrigin;
        mYOrigin = yOrigin;

        mCircleIndexPoint = new LinkedHashMap<>();
        mCirclePointIndex = new LinkedHashMap<>();

        Log.v(TAG, "init mRadius " + mRadius);
        mCirclePointsCreator = new FirstQuadrantCirclePointsCreator(mRadius, mXOrigin, mYOrigin);
        mCirclePointsCreator.fillCirclePoints(mCircleIndexPoint, mCirclePointIndex);
        Log.v(TAG, "init " + mCirclePointIndex.size());

        invalidate();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(100 /*This is not percents*/);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.BLACK);
        mPaint2.setStrokeWidth(5);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawCircle(mXOrigin, mYOrigin, mRadius, mPaint);
        canvas.drawLine(mXOrigin, mYOrigin, 3000, mYOrigin, mPaint2);
        canvas.drawLine(mXOrigin, mYOrigin, -3000, mYOrigin, mPaint2);
        canvas.drawLine(mXOrigin, mYOrigin, mXOrigin, 3000, mPaint2);
        canvas.drawLine(mXOrigin, mYOrigin, mXOrigin, -3000, mPaint2);

    }
}
