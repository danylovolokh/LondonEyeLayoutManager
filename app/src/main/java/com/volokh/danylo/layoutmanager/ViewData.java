package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;

/**
 * Created by danylo.volokh on 11/21/2015.
 * This class is used as helper to hold coordinates of the edges of the view.
 * These coordinates are used to layout new view relative to the previous
 */
public class ViewData {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = ViewData.class.getSimpleName();

    private final Rect mViewRect = new Rect();

    private int mViewTop;
    private int mViewBottom;
    private int mViewLeft;
    private int mViewRight;

    private boolean mIsViewVisible;
    private Point mViewCenter;

    public ViewData(int viewTop, int viewBottom, int viewLeft, int viewRight, Point viewCenter) {
        mViewTop = viewTop;
        mViewBottom = viewBottom;
        mViewLeft = viewLeft;
        mViewRight = viewRight;
        mViewCenter = viewCenter;
    }

    public void updateData(View view, Point viewCenter) {
        mIsViewVisible = view.getLocalVisibleRect(mViewRect);

        mViewTop = view.getTop();
        mViewBottom = view.getBottom();
        mViewLeft = view.getLeft();
        mViewRight = view.getRight();
        mViewCenter = viewCenter;
    }

    @Override
    public String toString() {
        return "ViewData{" +
                "mViewRect=" + mViewRect +
                ", mViewTop=" + mViewTop +
                ", mViewBottom=" + mViewBottom +
                ", mViewLeft=" + mViewLeft +
                ", mViewRight=" + mViewRight +
                ", mIsViewVisible=" + mIsViewVisible +
                '}';
    }

    public int getViewBottom() {
        return mViewBottom;
    }

    public int getViewLeft() {
        return mViewLeft;
    }

    public int getViewTop() {
        return mViewTop;
    }

    public boolean isViewVisible() {
        if(SHOW_LOGS) Log.v(TAG, "isViewVisible " + mIsViewVisible);
        return mIsViewVisible;
    }

    public Point getCenterPoint() {
        return mViewCenter;
    }
}
