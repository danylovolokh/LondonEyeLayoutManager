package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.QuadrantHelper;

/**
 * Created by danylo.volokh on 11/17/2015.
 * This is a helper that performs layouting and knows everything about how to layout views
 */
public class Layouter {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = Layouter.class.getSimpleName();

    private final Callback mCallback;

    private final QuadrantHelper mQuadrantHelper;

    private final int mRadius;

    public void reset() {
        mQuadrantHelper.reset();
    }

    interface Callback{
        void getHitRect(Rect rect);
        void layoutDecorated(View view, int left, int top, int right, int bottom);
        Pair<Integer,Integer> getWidthHeightPair(View view);
    }

    public Layouter(Callback callback, int radius){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = new QuadrantHelper(mRadius);
    }

    public boolean layoutInFourthQuadrant(View view, int previousViewBottom) {
        if (SHOW_LOGS)
            Log.v(TAG, ">> layoutInFourthQuadrant, previousViewBottom " + previousViewBottom);

        Pair<Integer, Integer> widthHeight = mCallback.getWidthHeightPair(view);

        int decoratedCapsuleWidth = widthHeight.first;
        int decoratedCapsuleHeight = widthHeight.second;

        if (SHOW_LOGS)
            Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleWidth " + decoratedCapsuleWidth);
        if (SHOW_LOGS)
            Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleHeight " + decoratedCapsuleHeight);

        int viewCenterY = previousViewBottom;
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewCenterY " + viewCenterY);

        int halfViewHeight = decoratedCapsuleHeight / 2;
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewHeight " + halfViewHeight);

        // viewTop is higher than viewCenterY. And "higher" is up. That's why we subtract halfViewHeight;
        int viewTop = viewCenterY - halfViewHeight;

        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTop " + viewTop);

        viewCenterY = mQuadrantHelper.findViewCenterY(previousViewBottom, halfViewHeight, viewTop);

        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, final viewCenterY " + viewCenterY);

        int left, top, right, bottom;

        top = viewCenterY - halfViewHeight;
        bottom = viewCenterY + halfViewHeight;

        int halfViewWidth = decoratedCapsuleWidth / 2;
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewWidth " + halfViewWidth);

        int viewCenterX = mQuadrantHelper.getXFromYInFourthQuadrant(viewCenterY);
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewCenterX " + viewCenterX);

        left = viewCenterX - halfViewWidth;
        right = viewCenterX + halfViewWidth;

        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, left " + left);
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, top " + top);
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, right " + right);
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, bottom " + bottom);

        mCallback.layoutDecorated(view, left, top, right, bottom);

        Rect visibleRect = new Rect();
        mCallback.getHitRect(visibleRect);
        boolean isViewVisible = view.getLocalVisibleRect(visibleRect);
        if (SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, isViewVisible " + isViewVisible);

        if (SHOW_LOGS) Log.v(TAG, "<< layoutInFourthQuadrant");
        return isViewVisible;
    }
}
