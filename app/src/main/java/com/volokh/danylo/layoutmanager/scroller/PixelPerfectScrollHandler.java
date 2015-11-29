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
 * TODO: specify 1st quadrant specific stuff
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

        int newCenterPointIndex = mQuadrantHelper.getNewCenterPointIndex(centerPointIndex + indexOffset);

        Point newCenterPoint = mQuadrantHelper.getViewCenterPoint(newCenterPointIndex);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterY " + viewCenterY);

        int dx = newCenterPoint.getX() - viewCenterX;
        int dy = newCenterPoint.getY() - viewCenterY;

        view.offsetTopAndBottom(dy);
        view.offsetLeftAndRight(dx);

        return newCenterPoint;
    }


    @Override
    public int scrollVerticallyBy(int dy) {
        View firstView = mCallback.getChildAt(0);
        View lastView = mCallback.getChildAt(
                mCallback.getChildCount()-1
        );
        boolean topBoundReached = isTopBoundReached(firstView);
        boolean bottomBoundReached = isBottomBoundReached(lastView);

        if (SHOW_LOGS) Log.v(TAG, "scrolscrollVerticallyBy, topBoundReached " + topBoundReached);
        if (SHOW_LOGS) Log.v(TAG, "scrolscrollVerticallyBy, bottomBoundReached " + bottomBoundReached);
        if (SHOW_LOGS) Log.v(TAG, "scrolscrollVerticallyBy, dy " + dy);

        int delta;
        if (dy > 0) { // Contents are scrolling up
            //Check against bottom bound
            if (bottomBoundReached) {
                //If we've reached the last row, enforce limits
                int bottomOffset = getBottomOffset(lastView);
                delta = Math.max(-dy, bottomOffset);
            } else {
                //No limits while the last row isn't visible
                delta = -dy;
            }
        } else { // Contents are scrolling down
            //Check against top bound
            if (topBoundReached) {
                int topOffset = -mCallback.getDecoratedTop(firstView) + mCallback.getPaddingTop();
                delta = Math.min(-dy, topOffset);
            } else {
                delta = -dy;
            }
        }
//        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, delta " + delta);

        Point firstViewNewCenter = scrollFirstViewVerticallyBy(firstView, delta);

//        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, firstViewNewCenter " + firstViewNewCenter);

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

//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy right " + right);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy top " + top);

            int width = view.getWidth();
            int height = view.getHeight();

//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy width " + width);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy height " + height);

            // TODO: this is old center point. No need to get it by index
            int viewCenterX = view.getRight() - view.getWidth()/2;
            int viewCenterY = view.getTop() + view.getHeight()/2;

//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterX " + viewCenterX);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterY " + viewCenterY);

            SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

            int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

            Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy oldCenterPoint " + oldCenterPoint);

            Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width/2, height/2);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy newCenterPoint " + newCenterPoint);

            int dX = newCenterPoint.getX() - oldCenterPoint.getX();
            int dY = newCenterPoint.getY() - oldCenterPoint.getY();
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dX " + dX);
//            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dY " + dY);

            view.offsetTopAndBottom(dY);
            view.offsetLeftAndRight(dX);

            previousViewData.updateData(view, newCenterPoint);
        }
        return -delta;
    }

    private int getBottomOffset(View lastView) {
        int bottomOffset;

        int recyclerHeight = mCallback.getHeight();
        if (SHOW_LOGS) Log.v(TAG, "getBottomOffset, recyclerHeight " + recyclerHeight);

        if (recyclerHeight > mRadius) {
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
            bottomOffset = lastView.getLeft();

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
            if (SHOW_LOGS) Log.v(TAG, "getBottomOffset lastViewBottom " + lastViewBottom);
            bottomOffset = lastViewBottom - recyclerHeight;
        }
        if (SHOW_LOGS) Log.v(TAG, "getBottomOffset,  " + bottomOffset);
        return bottomOffset;
    }

    private boolean isBottomBoundReached(View lastView) {
        int lastVisiblePosition = mCallback.getLastVisiblePosition();
        boolean isBottomBoundReached;

        if(lastVisiblePosition == mCallback.getItemCount()){


            int recyclerHeight = mCallback.getHeight();
            if(SHOW_LOGS) Log.v(TAG, "isBottomBoundReached, recyclerHeight " + recyclerHeight);

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
                if(SHOW_LOGS) Log.v(TAG, "isBottomBoundReached spaceToLeftEdge " + spaceToLeftEdge);
                isBottomBoundReached = spaceToLeftEdge >= 0;

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
                isBottomBoundReached = lastViewBottom <= recyclerHeight;
            }
            if(SHOW_LOGS) Log.v(TAG, "isBottomBoundReached " + isBottomBoundReached);
        } else {
            isBottomBoundReached = false;
        }
        return isBottomBoundReached;
    }

    private boolean isTopBoundReached(View firstView) {
        int firstVisiblePosition = mCallback.getFirstVisiblePosition();
        boolean isTopBoundReached;
        if(firstVisiblePosition == 0){
            int top = firstView.getTop();
            isTopBoundReached = top >= 0;
        } else {
            isTopBoundReached = false;
        }
        return isTopBoundReached;
    }

    @Override
    public boolean canScroll() {
        boolean canScroll;

        View firstView = mCallback.getChildAt(0);
        boolean firstViewTopIsAboveTopEdge = firstView.getTop() < 0;

        View lastView = mCallback.getChildAt(mCallback.getChildCount()-1);

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
            canScroll = spaceToLeftEdge < 0 || firstViewTopIsAboveTopEdge;

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
            canScroll = lastViewBottom > recyclerHeight || firstViewTopIsAboveTopEdge;
        }
        if(SHOW_LOGS) Log.v(TAG, "canScroll " + canScroll);
        return canScroll;
    }
}
