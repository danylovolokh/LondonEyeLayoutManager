package com.volokh.danylo.layoutmanager.scroller;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.ViewData;
import com.volokh.danylo.layoutmanager.circle_helper.FourQuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.Point;
import com.volokh.danylo.layoutmanager.circle_helper.UpdatablePoint;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

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
    private final Layouter mLayouter;

    public PixelPerfectScrollHandler(ScrollHandlerCallback callback, int radius, FourQuadrantHelper quadrantHelper, Layouter layouter){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = quadrantHelper;
        mLayouter = layouter;
    }

    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0,0);

    private Point scrollFirstViewVerticallyBy(View view, int indexOffset) {
        if (SHOW_LOGS) Log.v(TAG, ">> scrollFirstViewVerticallyBy, indexOffset " + indexOffset);

        int viewCenterX = view.getRight() - view.getWidth()/2;
        int viewCenterY = view.getTop() + view.getHeight()/2;
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
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler) {
        View firstView = mCallback.getChildAt(0);
        View lastView = mCallback.getChildAt(
                mCallback.getChildCount()-1
        );

        if (SHOW_LOGS) Log.v(TAG, "scrolscrollVerticallyBy, dy " + dy);

        int delta = checkBoundsReached(dy, firstView, lastView);
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
            scrollSingleView(previousViewData, view);
        }

        performRecycling(delta, firstView, lastView, recycler);

        return -delta;
    }

    private void scrollSingleView(ViewData previousViewData, View view) {

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

    private void performRecycling(int delta, View firstView, View lastView, RecyclerView.Recycler recycler) {
        if(SHOW_LOGS) Log.v(TAG, ">> performRecycling, delta " + delta);

        if(delta < 0){

            recycleTopIfNeeded(firstView, recycler);
            addToBottomIfNeeded(lastView, recycler);

        } else {
            recycleBottomIfNeeded(lastView, recycler);
            addTopIfNeeded(firstView, recycler);
        }
    }

    private void addTopIfNeeded(View firstView, RecyclerView.Recycler recycler) {
        int topOffset = firstView.getTop();
        if(SHOW_LOGS) Log.v(TAG, "addTopIfNeeded, topOffset " + topOffset);

        if(topOffset >= 0){
            int firstVisiblePosition = mCallback.getFirstVisiblePosition();

            if(SHOW_LOGS) Log.v(TAG, "addTopIfNeeded, firstVisiblePosition " + firstVisiblePosition);

            if(firstVisiblePosition > 0){
                if(SHOW_LOGS) Log.i(TAG, "addTopIfNeeded, add to top");

                View newFirstView = recycler.getViewForPosition(firstVisiblePosition - 1);

                int viewCenterX = firstView.getRight() - firstView.getWidth()/2;
                int viewCenterY = firstView.getTop() + firstView.getHeight()/2;
                SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

                ViewData previousViewData = new ViewData(
                        firstView.getTop(),
                        firstView.getBottom(),
                        firstView.getLeft(),
                        firstView.getRight(),
                        SCROLL_HELPER_POINT
                );
                mCallback.addView(newFirstView, 0);
                mLayouter.layoutViewPreviousView(newFirstView, previousViewData);
                mCallback.decrementFirstVisiblePosition();
            } else {
                // this is first view there is no views to add to the top
            }
        }
    }

    private void recycleBottomIfNeeded(View lastView, RecyclerView.Recycler recycler) {
        /**
         * Scroll up. Finger is pulling down
         * This mean that view that goes to bottom-left direction might hide.
         * If view is hidden we will recycle it
         */

        boolean lastViewIsVisible;
        int recyclerViewHeight = mCallback.getHeight();
        boolean isEnoughOverScrollForRecycling;
        if (recyclerViewHeight > mRadius) {
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
            int right = lastView.getRight();
            if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded right " + right);

            lastViewIsVisible = right >= 0;
            if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewIsVisible " + lastViewIsVisible);
            int viewCenterX = lastView.getRight() - lastView.getWidth()/2;

            isEnoughOverScrollForRecycling =
                    // This check handles small views: view width is smaller then radius.
                    // It will help us recycle views early. Right after it exceeded screen bound
                    Math.abs(right) > lastView.getWidth() ||

                    // This check handles large views: view width is bigger then radius.
                    // We might have a case where view width is as big as diameter.
                    // It will be recycled only when view center will be on the distance of circle radius
                    Math.abs(viewCenterX) > mRadius/2;

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
            if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewBottom " + lastViewBottom);
            lastViewIsVisible = lastViewBottom - recyclerViewHeight > 0;
            isEnoughOverScrollForRecycling = Math.abs(lastViewBottom) > lastView.getHeight();
        }
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewIsVisible " + lastViewIsVisible);
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded isEnoughOverScrollForRecycling " + isEnoughOverScrollForRecycling);

        boolean lastViewShouldBeRecycled = !lastViewIsVisible && isEnoughOverScrollForRecycling;
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewShouldBeRecycled " + lastViewShouldBeRecycled);

        if(lastViewShouldBeRecycled){
            if(SHOW_LOGS) Log.i(TAG, "recycleTopIfNeeded, recycling bottom view");

            mCallback.removeView(lastView);
            mCallback.decrementLastVisiblePosition();
            recycler.recycleView(lastView);
        }
    }

    private void addToBottomIfNeeded(View lastView, RecyclerView.Recycler recycler) {
        // now we should fill extra gap on the bottom if there is one
        int bottomOffset = getBottomOffset(lastView);
        if(SHOW_LOGS) Log.v(TAG, "addToBottomIfNeeded, bottomOffset " + bottomOffset);

        if(bottomOffset > 0){
            int itemCount = mCallback.getItemCount();

            if(SHOW_LOGS) Log.v(TAG, "addToBottomIfNeeded, itemCount " + itemCount);
            int nextPosition = mCallback.getLastVisiblePosition() + 1;
            if(SHOW_LOGS) Log.v(TAG, "addToBottomIfNeeded, nextPosition " + nextPosition);

            if(nextPosition <= itemCount){
                if(SHOW_LOGS) Log.i(TAG, "addToBottomIfNeeded, add new view to bottom");

                View newLastView = recycler.getViewForPosition(nextPosition - 1);

                int viewCenterX = lastView.getRight() - lastView.getWidth()/2;
                int viewCenterY = lastView.getTop() + lastView.getHeight()/2;
                SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

                ViewData previousViewData = new ViewData(
                        lastView.getTop(),
                        lastView.getBottom(),
                        lastView.getLeft(),
                        lastView.getRight(),
                        SCROLL_HELPER_POINT
                );
                mCallback.addView(newLastView);
                mLayouter.layoutViewNextView(newLastView, previousViewData);
                mCallback.incrementLastVisiblePosition();
            } else {
                // last view is the last item. Do nothing
            }
        }
    }

    private void recycleTopIfNeeded(View firstView, RecyclerView.Recycler recycler) {
        /**
         * Scroll down. Finger is pulling up
         * This mean that view that goes to up-right direction might hide.
         * If view is hidden we will recycle it
         */
        int bottom = firstView.getBottom();
        boolean firstViewOnTheScreen = bottom >= 0;
        boolean needRecycling = !firstViewOnTheScreen &&
                Math.abs(bottom) > firstView.getHeight();

        if(SHOW_LOGS) Log.v(TAG, "recycleTopIfNeeded, needRecycling " + needRecycling);

        if(needRecycling){
            // first view is hidden
            if(SHOW_LOGS) Log.i(TAG, "recycleTopIfNeeded, recycling first view");

            mCallback.removeView(firstView);
            mCallback.incrementFirstVisiblePosition();
            recycler.recycleView(firstView);
        }
    }

    private int checkBoundsReached(int dy, View firstView, View lastView) {
        int delta;
        boolean topBoundReached = isTopBoundReached(firstView);
        boolean bottomBoundReached = isBottomBoundReached(lastView);

        if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, topBoundReached " + topBoundReached);
        if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, bottomBoundReached " + bottomBoundReached);
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
                int topOffset = getTopOffset(firstView);
                delta = Math.min(-dy, topOffset); // stoled from FixedGrid
            } else {
                delta = -dy;
            }
        }
        return delta;
    }

    private int getTopOffset(View firstView) {
        return -mCallback.getDecoratedTop(firstView) + mCallback.getPaddingTop();
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

    /**
     * By "bottom" we mean left or bottom edge
     * @param lastView
     * @return
     */
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

    /**
     * By "top" we mean only top edge.
     */
    private boolean isTopBoundReached(View firstView) {
        int firstVisiblePosition = mCallback.getFirstVisiblePosition();
        if (SHOW_LOGS) Log.v(TAG, ">> isTopBoundReached, firstVisiblePosition " + firstVisiblePosition);

        boolean isTopBoundReached;
        int top = firstView.getTop();
        isTopBoundReached = top >= 0;
        if (SHOW_LOGS) Log.v(TAG, "<< isTopBoundReached, isTopBoundReached " + isTopBoundReached);

        return isTopBoundReached;
    }

}
