package com.volokh.danylo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by danylo.volokh on 10/31/2015.
 */
public class DebugRecyclerView extends RecyclerView {

    private Paint mPaint;
    private int mRadius;

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

    public void setRadius(int radius){
        mRadius = radius;
        invalidate();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(100);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawCircle(0, 0, mRadius, mPaint);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
