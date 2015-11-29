package com.volokh.danylo.layoutmanager.scroller;

import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.ViewData;
import com.volokh.danylo.layoutmanager.circle_helper.FourQuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.Point;
import com.volokh.danylo.layoutmanager.circle_helper.UpdatablePoint;

/**
 * Created by danylo.volokh on 28.11.2015.
 * TODO: specify 4th quadrant specific stuff
 */
public class PixelPerfectScrollHandler implements ScrollHandler {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = PixelPerfectScrollHandler.class.getSimpleName();

    private final ScrollHandlerCallback mCallback;
    private final FourQuadrantHelper mQuadrantHelper;
    private final int mRadius;

    public PixelPerfectScrollHandler(ScrollHandlerCallback callback, int radius, FourQuadrantHelper quadrantHelper){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = quadrantHelper;
    }

    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0,0);

    private Point scrollFirstViewVerticallyBy(View view, int indexOffset) {
        if (SHOW_LOGS) Log.v(TAG, ">> scrollFirstViewVerticallyBy, indexOffset " + indexOffset);

        int top = view.getTop();
        int right = view.getRight();
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, top " + top);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, right " + right);

        int width = view.getWidth();
        int height = view.getHeight();
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, width " + width);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, height " + height);
// TODO: thi is view center. No need to get it by index
        int viewCenterX = view.getRight() - view.getWidth()/2;
        int viewCenterY = view.getTop() + view.getHeight()/2;

        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterX " + viewCenterX);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterY " + viewCenterY);

        SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

        int newCenterPointIndex = centerPointIndex + indexOffset;

        Point newCenterPoint = mQuadrantHelper.getViewCenterPoint(newCenterPointIndex);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterY " + viewCenterY);

        int dx = newCenterPoint.getX() - viewCenterX;
        int dy = newCenterPoint.getY() - viewCenterY;

        view.offsetTopAndBottom(dy);
        view.offsetLeftAndRight(dx);

        return newCenterPoint;
    }


    @Override
    public void scrollVerticallyBy(int dy) {

        boolean topBoundReached = isTopBoundReached();
//        boolean bottomBoundReached = getLastVisibleRow() >= maxRowCount;

        View firstView = mCallback.getChildAt(0);
        Point firstViewNewCenter = scrollFirstViewVerticallyBy(firstView, -dy);

        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, firstViewNewCenter " + firstViewNewCenter);

        ViewData previousViewData = new ViewData(
                firstView.getTop(),
                firstView.getBottom(),
                firstView.getLeft(),
                firstView.getRight(),
                firstViewNewCenter);

        for(int indexOfView = 1; indexOfView < mCallback.getChildCount(); indexOfView++){

            View view = mCallback.getChildAt(indexOfView);

            int right = view.getRight();
            int top = view.getTop();

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy right " + right);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy top " + top);

            int width = view.getWidth();
            int height = view.getHeight();

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy width " + width);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy height " + height);

            // TODO: this is old center point. No need to get it by index
            int viewCenterX = view.getRight() - view.getWidth()/2;
            int viewCenterY = view.getTop() + view.getHeight()/2;

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterX " + viewCenterX);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterY " + viewCenterY);

            SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

            int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

            Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy oldCenterPoint " + oldCenterPoint);

            Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width/2, height/2);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy newCenterPoint " + newCenterPoint);

            int dX = newCenterPoint.getX() - oldCenterPoint.getX();
            int dY = newCenterPoint.getY() - oldCenterPoint.getY();
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dX " + dX);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dY " + dY);

            view.offsetTopAndBottom(dY);
            view.offsetLeftAndRight(dX);

            previousViewData.updateData(view, newCenterPoint);
        }
    }

    private boolean isTopBoundReached() {
        int firstVisiblePosition = mCallback.getFirstVisiblePosition();
        boolean isTopBoundReached;
        if(firstVisiblePosition == 0){
            View firstView = mCallback.getChildAt(0);
            isTopBoundReached = true;
        } else {
            isTopBoundReached = false;
        }
        return isTopBoundReached;
    }

    @Override
    public boolean canScroll() {
        boolean canScroll;
        int childCount = mCallback.getChildCount();
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, childCount " + childCount);

        View lastView = mCallback.getChildAt(childCount-1);
        int recyclerHeight = mCallback.getHeight();
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, recyclerHeight " + recyclerHeight);

        if(recyclerHeight > mRadius){
            /** mean that circle is hiding behind the left edge
             *   ___________
             *  |        |  |
             *  |        |  |
             *  |      _/   |
             *  |_____/     |
             *  |           |
             *  |           |
             *  |           |
             *  |___________|
             *
             */
            int spaceToLeftEdge = lastView.getLeft();
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy spaceToLeftEdge " + spaceToLeftEdge);
            canScroll = spaceToLeftEdge < 0;

        } else {
            /** mean that circle is hiding behind the bottom edge
             *   ___________
             *  |     \     |
             *  |       \   |
             *  |        |  |
             *  |        |  |
             *  |        |  |
             *  |        |  |
             *  |       /   |
             *  |_____/_____|
             *
             */

            int lastViewBottom = lastView.getBottom();
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy lastViewBottom " + lastViewBottom);
            canScroll = lastViewBottom > recyclerHeight;
        }
        if(SHOW_LOGS) Log.v(TAG, "canScroll " + canScroll);
        return canScroll;
    }
}
