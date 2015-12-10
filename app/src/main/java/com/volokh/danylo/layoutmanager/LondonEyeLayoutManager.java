package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.utils.Config;
import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelperFactory;
import com.volokh.danylo.layoutmanager.layouter.Layouter;
import com.volokh.danylo.layoutmanager.layouter.LayouterCallback;
import com.volokh.danylo.layoutmanager.scroller.IScrollHandler;
import com.volokh.danylo.layoutmanager.scroller.ScrollHandlerCallback;

/**
 * This layout manager is created to layout views on the screen exactly like passenger capsules are situated on the London Eye :)
 *
 *                               ______
 *                              |      |
 *        *---------------------|View1 |
 *         \_\____          ____|______|
 *           \    \__      |      |
 *            \      \_____|View2 |
 *             \      _____|______|
 *              \___ |      |
 *                  \|View3 |
 *                   |______|
 *
 * CAUTION: you cannot layout views on a fully visible circle.
 * Purpose of RecyclerView and LayoutManager is to recycle views that were scrolled out of the RecyclerView.
 *
 * If you situate a center of your circle and set radius to a values that full circle will be visible,
 * you won't be able to recycle any views because all of them will be always on the screen.
 *
 * TODO: validate radius, and xOrigin, yOrigin. Don't let user to set values that makes recycling impossible
 *
 *
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager implements LayouterCallback, ScrollHandlerCallback {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

    private final RecyclerView mRecyclerView;

    private final Layouter mLayouter;

    private final IScrollHandler mScroller;
    private final QuadrantHelper mQuadrantHelper;

    /**
     * This is a helper value. We should always return "true" from {@link #canScrollVertically()} but we need to change this value to false when measuring a child view size.
     * This is because the height "match_parent" is not calculated correctly if {@link #canScrollHorizontally()} returns "true"
     * and
     * the height "match_parent" is not calculated correctly if {@link #canScrollVertically()} returns "true"
     */

    private boolean mCanScrollVertically = true;

    private final int mRadius;

    private int mFirstVisiblePosition = 0; //TODO: implement save/restore state
    private int mLastVisiblePosition = 0; //TODO: implement save/restore state

    public LondonEyeLayoutManager(int radius, int xOrigin, int yOrigin, RecyclerView recyclerView, IScrollHandler.Strategy scrollStrategy) {
        mRadius = radius;

        mRecyclerView = recyclerView;

        mQuadrantHelper = QuadrantHelperFactory.createQuadrantHelper(radius, xOrigin, yOrigin);

        mLayouter = new Layouter(this, mQuadrantHelper);
        mScroller = IScrollHandler.Factory.createScrollHandler(
                scrollStrategy,
                this,
                mQuadrantHelper,
                mLayouter);
    }

    @Override
    public void getHitRect(Rect rect) {
        mRecyclerView.getHitRect(rect);
    }

    @Override
    public Pair<Integer, Integer> getHalfWidthHeightPair(View view) {

        Pair<Integer, Integer> widthHeight;
        measureChildWithMargins(view, 0, 0);

        int measuredWidth = getDecoratedMeasuredWidth(view);
        int measuredHeight = getDecoratedMeasuredHeight(view);

        if (SHOW_LOGS)
            Log.i(TAG, "getHalfWidthHeightPair, measuredWidth " + measuredWidth + ", measuredHeight " + measuredHeight);

        int diameter = mRadius*2;

        if (measuredWidth > diameter || measuredHeight > diameter) {
            throw new RuntimeException("View size is bigger than diameter. " +
                    "\nWe are unable to layout multiple views. " +
                    "\nDefine a better algorithm and let us know :)" +
                    "\n, measuredWidth " + measuredWidth + ", measuredHeight " + measuredHeight + ", diameter " + diameter);
        }

        int halfViewHeight = measuredHeight / 2;
        if (SHOW_LOGS) Log.v(TAG, "getHalfWidthHeightPair, halfViewHeight " + halfViewHeight);

        int halfViewWidth = measuredWidth / 2;
        if (SHOW_LOGS) Log.v(TAG, "getHalfWidthHeightPair, halfViewWidth " + halfViewWidth);

        widthHeight = new Pair<>(
                halfViewWidth,
                halfViewHeight
        );
        return widthHeight;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return mCanScrollVertically;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dy " + dy);
        int childCount = getChildCount();
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy childCount " + childCount);

        if (childCount == 0) {
            // we cannot scroll if we don't have views
            return 0;
        }

        return mScroller.scrollVerticallyBy(dy, recycler);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, ">> onLayoutChildren, state " + state);

        //We have nothing to show for an empty data set but clear any existing views
        int itemCount = getItemCount();
        if (itemCount == 0) {
            removeAllViews();
            return;
        }

        removeAllViews();

        // TODO: These values should not be set to "0". They should be restored from state
        mLastVisiblePosition = 0;
        mFirstVisiblePosition = 0;

        if(SHOW_LOGS) {
            Log.v(TAG, "onLayoutChildren, state " + state);
            Log.v(TAG, "onLayoutChildren, mRadius " + mRadius);
            Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);
        }

        ViewData viewData = new ViewData(0, 0, 0, 0,
                    mQuadrantHelper.getViewCenterPoint(0)
            );

        // It will be our stop flag
        boolean isLastLayoutedView;

        do{
            View view = recycler.getViewForPosition(mLastVisiblePosition);
            addView(view);
            viewData = mLayouter.layoutNextView(view, viewData);

            // We update coordinates instead of creating new object to keep the heap clean
            if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewData " + viewData);

            isLastLayoutedView = mLayouter.isLastLaidOutView(view);
            mLastVisiblePosition++;

        } while (!isLastLayoutedView && mLastVisiblePosition < itemCount);

        if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);
    }

    /**
     * This is a wrapper method for {@link android.support.v7.widget.RecyclerView#measureChildWithMargins(android.view.View, int, int, int, int)}
     *
     * If capsules width is "match_parent" then we we need to return "false" from {@link #canScrollHorizontally()}
     * If capsules height is "match_parent" then we we need to return "false" from {@link #canScrollVertically()}
     *
     * This method simply changes return values of {@link #canScrollHorizontally()} and {@link #canScrollVertically()} while measuring
     * size of a child view
     */
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        // change a value to "false "temporary while measuring
        mCanScrollVertically = false;

        super.measureChildWithMargins(child, widthUsed, heightUsed);

        // return a value to "true" because we do actually can scroll in both ways
        mCanScrollVertically = true;
    }

    @Override
    public int getFirstVisiblePosition() {
        return mFirstVisiblePosition;
    }

    @Override
    public int getLastVisiblePosition() {
        return mLastVisiblePosition;
    }

    @Override
    public void incrementFirstVisiblePosition() {
        mFirstVisiblePosition++;
    }

    @Override
    public void incrementLastVisiblePosition() {
        mLastVisiblePosition++;
    }

    @Override
    public void decrementLastVisiblePosition() {
        mLastVisiblePosition--;
    }

    @Override
    public void decrementFirstVisiblePosition() {
        mFirstVisiblePosition--;
    }
}
