package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.FirstQuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.UnitCircleFourthQuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;
import com.volokh.danylo.layoutmanager.layouter.LayouterCallback;
import com.volokh.danylo.layoutmanager.scroller.PixelPerfectScrollHandler;
import com.volokh.danylo.layoutmanager.scroller.ScrollHandler;
import com.volokh.danylo.layoutmanager.scroller.ScrollHandlerCallback;

/**
 * This layout manager is created to layout views on the screen exactly like passenger capsules are situated on the London Eye:
 *
 *  TODO: add javadoc
 *
 *
 *
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager implements LayouterCallback, ScrollHandlerCallback {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

    private final UnitCircleFourthQuadrantHelper mUnitCircleHelper;

    private final RecyclerView mRecyclerView;

    private final Layouter mLayouter;

    private final ScrollHandler mScroller;
    private final FirstQuadrantHelper mQuadrantHelper;


    /**
     * This is a helper value. We should always return "true" from {@link #canScrollVertically()}
     * and {@link #canScrollHorizontally()} but we need to change this value to false when measuring a child view size.
     * This is because the width "match_parent" is not calculated correctly if {@link #canScrollHorizontally()} returns "true"
     * and
     * the height "match_parent" is not calculated correctly if {@link #canScrollVertically()} returns "true"
     */

    private boolean mCanScrollVerticallyHorizontally = true;

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

    private final int mRadius;

    private int mFirstVisiblePosition = 0; //TODO: restore state
    private int mLastVisiblePosition = 0; //TODO: restore state

    public LondonEyeLayoutManager(int radius, RecyclerView recyclerView) {
        mRadius = radius;

        mUnitCircleHelper = new UnitCircleFourthQuadrantHelper(
                mRadius);

        mRecyclerView = recyclerView;

        mQuadrantHelper = new FirstQuadrantHelper(mRadius, 0, 0);

        mLayouter = new Layouter(this, mRadius, mQuadrantHelper); // TODO: get from constructor
        mScroller = new PixelPerfectScrollHandler(this, mRadius, mQuadrantHelper, mLayouter); // TODO: use strategy for this
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return mCanScrollVerticallyHorizontally;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;// TODO: return false ony for now .mCanScrollVerticallyHorizontally;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dy " + dy);
        int childCount = getChildCount();
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy childCount " + childCount);

        if (childCount == 0) {
            return 0;
        }

        return mScroller.scrollVerticallyBy(dy, recycler);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, "scrollHorizontallyBy dx " + dx);

        return super.scrollHorizontallyBy(dx, recycler, state);
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

            isLastLayoutedView = mLayouter.isLastLayoutedView(view);
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
        mCanScrollVerticallyHorizontally = false;

        super.measureChildWithMargins(child, widthUsed, heightUsed);

        // return a value to "true" because we do actually can scroll in both ways
        mCanScrollVerticallyHorizontally = true;
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
