package com.volokh.danylo.layoutmanager;

import android.view.View;

/**
 * Created by danylo.volokh on 11/21/2015.
 * This class is used as helper to hold coordinates of the edges of the view.
 * These coordinates are used to layout new view relative to the previous
 */
public class ViewCoordinates {

    private int mViewBottom;
    private int mViewLeft;
    private int mViewRight;

    public ViewCoordinates(int viewBottom, int viewLeft, int viewRight) {
        mViewBottom = viewBottom;
        mViewLeft = viewLeft;
        mViewRight = viewRight;
    }

    public void updateCoordinates(View view) {
        mViewBottom = view.getBottom();
        mViewLeft = view.getLeft();
        mViewRight = view.getRight();
    }

    @Override
    public String toString() {
        return "ViewCoordinates{" +
                "mViewBottom=" + mViewBottom +
                ", mViewLeft=" + mViewLeft +
                ", mViewRight=" + mViewRight +
                '}';
    }

    public int getPreviousViewBottom() {
        return mViewBottom;
    }
}
