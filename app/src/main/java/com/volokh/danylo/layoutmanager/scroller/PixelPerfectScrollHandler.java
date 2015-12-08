package com.volokh.danylo.layoutmanager.scroller;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.ViewData;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;
import com.volokh.danylo.layoutmanager.circle_helper.point.UpdatablePoint;
import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

/**
 * Created by danylo.volokh on 28.11.2015.
 * This scroll handler keeps view in touch when scrolling.
 *  1. Views center is on the circle
 *  2. Views edges are always in touch with each other.
 *
 * Sometimes these requirements are making views "jump" when scroll:
 * If "view B" is below "view A" and views are scrolled down we can reach a point
 * in which "view B" cannot longer stay below "view A" and keep it's center on the circle so,
 * in this case "view B" jumps to the side in order to stay in touch with "view A" side by side and keep it's center on the circle.
 *
 *
 * The logic:
 * 1. Scroll first view by received offset.
 * 2. Calculate position of other views related to first view.
 */
public class PixelPerfectScrollHandler implements ScrollHandler {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = PixelPerfectScrollHandler.class.getSimpleName();

    private final ScrollHandlerCallback mCallback;
    private final QuadrantHelper mQuadrantHelper;
    private final Layouter mLayouter;

    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0, 0);

    PixelPerfectScrollHandler(ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter) {
        mCallback = callback;
        mQuadrantHelper = quadrantHelper;
        mLayouter = layouter;
    }

    /**
     * This method calculates new position of first view and returns new center point of first view
     */
    private Point scrollFirstViewVerticallyBy(View view, int indexOffset) {
        if (SHOW_LOGS) Log.v(TAG, ">> scrollFirstViewVerticallyBy, indexOffset " + indexOffset);

        int viewCenterX = view.getRight() - view.getWidth() / 2;
        int viewCenterY = view.getTop() + view.getHeight() / 2;
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


    /**
     * This method does:
     * 1. Shifts all views by received offset "dy".
     *      1a. Shifts first view by "dy"
     *      1b. Shifts all other views relatively to first view.
     * 2. Perform recycling if needed
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler) {
        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, dy " + dy);
        boolean isFirstItemReached = isFirstItemReached();
        boolean isLastItemReached = isLastItemReached();

        View firstView = mCallback.getChildAt(0);
        View lastView = mCallback.getChildAt(
                mCallback.getChildCount() - 1
        );

        int delta = mQuadrantHelper.checkBoundsReached(mCallback.getHeight(), dy, firstView, lastView, isFirstItemReached, isLastItemReached);

        /**1a. */
        Point firstViewNewCenter = scrollFirstViewVerticallyBy(firstView, delta);

        ViewData previousViewData = new ViewData(
                firstView.getTop(),
                firstView.getBottom(),
                firstView.getLeft(),
                firstView.getRight(),
                firstViewNewCenter);
        /**1b. */
        for (int indexOfView = 1; indexOfView < mCallback.getChildCount(); indexOfView++) {
            View view = mCallback.getChildAt(indexOfView);
            scrollSingleView(previousViewData, view);
        }

        /**2. */
        performRecycling(delta, firstView, lastView, recycler);

        return -delta;
    }

    private void scrollSingleView(ViewData previousViewData, View view) {
        if (SHOW_LOGS) Log.v(TAG, "scrollSingleView, previousViewData " + previousViewData);

        int width = view.getWidth();
        int height = view.getHeight();

        int viewCenterX = view.getRight() - width / 2;
        int viewCenterY = view.getTop() + height / 2;

        SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

        Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);

        Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width / 2, height / 2);

        int dX = newCenterPoint.getX() - oldCenterPoint.getX();
        int dY = newCenterPoint.getY() - oldCenterPoint.getY();

        view.offsetTopAndBottom(dY);
        view.offsetLeftAndRight(dX);

        previousViewData.updateData(view, newCenterPoint);
    }

    /**
     * This method recycles views:
     * If views was scrolled down then it recycles top if needed and add views from the bottom
     * If views was scrolled up then it recycles bottom if needed and add views from the top

     * @param delta - indicator of scroll direction
     */
    private void performRecycling(int delta, View firstView, View lastView, RecyclerView.Recycler recycler) {
        if (SHOW_LOGS) Log.v(TAG, ">> performRecycling, delta " + delta);

        if (delta < 0) {
            /** Scroll down*/
            recycleTopIfNeeded(firstView, recycler);
            addToBottomIfNeeded(lastView, recycler);

        } else {
            /** Scroll up*/
            recycleBottomIfNeeded(lastView, recycler);
            addTopIfNeeded(firstView, recycler);
        }
    }

    private void addTopIfNeeded(View firstView, RecyclerView.Recycler recycler) {
        int topOffset = firstView.getTop();
        if (SHOW_LOGS) Log.v(TAG, "addTopIfNeeded, topOffset " + topOffset);

        if (topOffset >= 0) {
            int firstVisiblePosition = mCallback.getFirstVisiblePosition();

            if (SHOW_LOGS)
                Log.v(TAG, "addTopIfNeeded, firstVisiblePosition " + firstVisiblePosition);

            if (firstVisiblePosition > 0) {
                if (SHOW_LOGS) Log.i(TAG, "addTopIfNeeded, add to top");

                View newFirstView = recycler.getViewForPosition(firstVisiblePosition - 1);

                int viewCenterX = firstView.getRight() - firstView.getWidth() / 2;
                int viewCenterY = firstView.getTop() + firstView.getHeight() / 2;
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

        int recyclerViewHeight = mCallback.getHeight();
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded recyclerViewHeight " + recyclerViewHeight);

        int lastViewRight = lastView.getRight();
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewRight " + lastViewRight);

        int lastViewTop = lastView.getTop();
        if (SHOW_LOGS) Log.v(TAG, "recycleBottomIfNeeded lastViewTop " + lastViewTop);
        boolean lastViewIsVisible = lastViewTop - recyclerViewHeight < 0 && lastViewRight >= 0;

        boolean isEnoughOverScrollForRecycling =
                // This check handles small views: view width is smaller then radius.
                // It will help us recycle views early. Right after it exceeded screen bound
                Math.abs(lastViewRight) > lastView.getWidth() ||
                Math.abs(lastViewTop) > lastView.getHeight();

        if (SHOW_LOGS){
            Log.v(TAG, "recycleBottomIfNeeded lastViewIsVisible " + lastViewIsVisible);
            Log.v(TAG, "recycleBottomIfNeeded isEnoughOverScrollForRecycling " + isEnoughOverScrollForRecycling);
        }

        boolean lastViewShouldBeRecycled = !lastViewIsVisible && isEnoughOverScrollForRecycling;
        if (SHOW_LOGS)
            Log.v(TAG, "recycleBottomIfNeeded lastViewShouldBeRecycled " + lastViewShouldBeRecycled);

        if (lastViewShouldBeRecycled) {
            if (SHOW_LOGS) Log.i(TAG, "recycleBottomIfNeeded, recycling bottom view");

            mCallback.removeView(lastView);
            mCallback.decrementLastVisiblePosition();
            recycler.recycleView(lastView);
        }
    }

    private void addToBottomIfNeeded(View lastView, RecyclerView.Recycler recycler) {
        // now we should fill extra gap on the bottom if there is one
        int bottomOffset = mQuadrantHelper.getOffset(mCallback.getHeight(), lastView);
        if (SHOW_LOGS){
            Log.v(TAG, "addToBottomIfNeeded, bottomOffset " + bottomOffset);
            Log.v(TAG, "addToBottomIfNeeded, tag " + lastView.getTag());
        }

        if (bottomOffset > 0) {
            int itemCount = mCallback.getItemCount();

            if (SHOW_LOGS) Log.v(TAG, "addToBottomIfNeeded, itemCount " + itemCount);
            int nextPosition = mCallback.getLastVisiblePosition() + 1;
            if (SHOW_LOGS) Log.v(TAG, "addToBottomIfNeeded, nextPosition " + nextPosition);

            if (nextPosition <= itemCount) {
                if (SHOW_LOGS) Log.i(TAG, "addToBottomIfNeeded, add new view to bottom");

                View newLastView = recycler.getViewForPosition(nextPosition - 1);

                int viewCenterX = lastView.getRight() - lastView.getWidth() / 2;
                int viewCenterY = lastView.getTop() + lastView.getHeight() / 2;
                SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

                ViewData previousViewData = new ViewData(
                        lastView.getTop(),
                        lastView.getBottom(),
                        lastView.getLeft(),
                        lastView.getRight(),
                        SCROLL_HELPER_POINT
                );
                mCallback.addView(newLastView);
                mLayouter.layoutNextView(newLastView, previousViewData);
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

        if (SHOW_LOGS) Log.v(TAG, "recycleTopIfNeeded, needRecycling " + needRecycling);

        if (needRecycling) {
            // first view is hidden
            if (SHOW_LOGS) Log.i(TAG, "recycleTopIfNeeded, recycling first view");

            mCallback.removeView(firstView);
            mCallback.incrementFirstVisiblePosition();
            recycler.recycleView(firstView);
        }
    }

    private boolean isLastItemReached() {
        int lastVisiblePosition = mCallback.getLastVisiblePosition();
        if (SHOW_LOGS)
            Log.v(TAG, "isLastItemReached, lastVisiblePosition " + lastVisiblePosition + ", mCallback.getItemCount() " + mCallback.getItemCount());

        boolean isLastItemReached = lastVisiblePosition == mCallback.getItemCount();
        if (SHOW_LOGS) Log.v(TAG, "<< isLastItemReached " + isLastItemReached);
        return isLastItemReached;
    }

    private boolean isFirstItemReached() {
        boolean isFirstItemReached = mCallback.getFirstVisiblePosition() == 0;
        if (SHOW_LOGS) Log.v(TAG, "isFirstItemReached, " + isFirstItemReached);
        return isFirstItemReached;
    }

}
