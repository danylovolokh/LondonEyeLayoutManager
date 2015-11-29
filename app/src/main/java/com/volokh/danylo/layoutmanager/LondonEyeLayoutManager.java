package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.FourQuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.UnitCircleFourthQuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;
import com.volokh.danylo.layoutmanager.layouter.LayouterCallback;
import com.volokh.danylo.layoutmanager.scroller.PixelPerfectScrollHandler;
import com.volokh.danylo.layoutmanager.scroller.ScrollHandler;
import com.volokh.danylo.layoutmanager.scroller.ScrollHandlerCallback;

/**
 * Created by danylo.volokh on 10/17/2015.
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager implements LayouterCallback, ScrollHandlerCallback {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

    private final UnitCircleFourthQuadrantHelper mUnitCircleHelper;

    private final RecyclerView mRecyclerView;

    private final Layouter mLayouter;

    private final ScrollHandler mScroller;
    private final FourQuadrantHelper mQuadrantHelper;


    /**
     * If this is set to "true" we will calculate size of capsules only once.
     * It is some sort of optimization
     */
    private boolean mHasFixedSizeCapsules;
    /**
     * This is a helper value. We should always return "true" from {@link #canScrollVertically()}
     * and {@link #canScrollHorizontally()} but we need to change this value to false when measuring a child view size.
     * This is because the width "match_parent" is not calculated correctly if {@link #canScrollHorizontally()} returns "true"
     * and
     * the height "match_parent" is not calculated correctly if {@link #canScrollVertically()} returns "true"
     */

    private boolean mCanScrollVerticallyHorizontally = true;

    private static final int ANGLE_DELTA = 1;
    private int mHold;

    @Override
    public void getHitRect(Rect rect) {
        mRecyclerView.getHitRect(rect);
    }

    @Override
    public Pair<Integer, Integer> getHalfWidthHeightPair(View view) {

        Pair<Integer, Integer> widthHeight;
        if (mHasFixedSizeCapsules) {
            // this is an optimization
            if (mHalfDecoratedCapsuleWidth == 0 || mHalfDecoratedCapsuleHeight == 0) {
                throw new RuntimeException("mHalfDecoratedCapsuleWidth " + mHalfDecoratedCapsuleWidth + ", mHalfDecoratedCapsuleHeight " + mHalfDecoratedCapsuleHeight + ", values should be calculated earlier");
            }
            widthHeight = new Pair<>(
                    mHalfDecoratedCapsuleWidth,
                    mHalfDecoratedCapsuleHeight
            );
            if(SHOW_LOGS) Log.i(TAG, "getHalfWidthHeightPair, mHalfDecoratedCapsuleWidth " + mHalfDecoratedCapsuleWidth + ", mHalfDecoratedCapsuleHeight " + mHalfDecoratedCapsuleHeight);

        } else {
            measureChildWithMargins(view, 0, 0);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            int measuredHeight = getDecoratedMeasuredHeight(view);

            int diameter = mRadius*2;

            if(SHOW_LOGS) Log.i(TAG, "getHalfWidthHeightPair, measuredWidth " + measuredWidth + ", measuredHeight " + measuredHeight);

            if(measuredWidth > diameter || measuredHeight > diameter){
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
        }
        return widthHeight;
    }

    private final int mRadius;

    private int mHalfDecoratedCapsuleWidth;
    private int mHalfDecoratedCapsuleHeight;

    private int mFirstVisiblePosition = 0; //TODO: restore state
    private int mLastVisiblePosition = 0; //TODO: restore state

    private int mCurrentViewPosition;

    private int mOriginY;
    private int mOriginX;

    public LondonEyeLayoutManager(FragmentActivity activity, int radius, RecyclerView recyclerView) {
        mRadius = radius;

        mUnitCircleHelper = new UnitCircleFourthQuadrantHelper(
                mRadius);

        mRecyclerView = recyclerView;

        mQuadrantHelper = new FourQuadrantHelper(mRadius, 0, 0);

        mLayouter = new Layouter(this, mRadius, mQuadrantHelper); // TODO: get from constructor
        mScroller = new PixelPerfectScrollHandler(this, mRadius, mQuadrantHelper, mLayouter); // TODO: use strategy for this
    }

    public void setHasFixedSizeCapsules(boolean hasFixedSizeCapsules){
        // TODO: find a usage for this?
        mHasFixedSizeCapsules = hasFixedSizeCapsules;
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

        //Optimize the case where the entire data set is too small to scroll
//        boolean canScroll = mScroller.canScroll();
//        if(!canScroll){
//            return 0;
//        }

//        if(mHold < 2){

        int delta = mScroller.scrollVerticallyBy(dy, recycler);
//            mHold++;
//        }

        return delta;
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

        if(mHasFixedSizeCapsules){
            // perform an optimization. If mHasFixedSizeCapsules == true we will calculate a size of views only once
            if(mHalfDecoratedCapsuleHeight == 0 && mHalfDecoratedCapsuleWidth == 0){
                calculateCapsuleHalfWidthHeight(recycler);
            }
        }

        mCurrentViewPosition = 0;

        if(SHOW_LOGS) {
            Log.v(TAG, "onLayoutChildren, state " + state);
            Log.v(TAG, "onLayoutChildren, mRadius " + mRadius);
            Log.v(TAG, "onLayoutChildren, mOriginY " + mOriginY);
            Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);        }


        ViewData viewData = new ViewData(0, 0, 0, 0,
                    mQuadrantHelper.getViewCenterPoint(0)
            );

        // when this variable will be false it mean that we have layout-ed a view outside of the RecyclerView.
        // It will be our stop flag
        boolean isLastLayoutedView;

        do{
            View view = recycler.getViewForPosition(mLastVisiblePosition);
            addView(view);
            viewData = mLayouter.layoutViewNextView(view, viewData);

            // We update coordinates instead of creating new object to keep the heap clean
            if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewData " + viewData);

            isLastLayoutedView = mLayouter.isLastLayoutedView(view);
            mLastVisiblePosition++;

        } while (!isLastLayoutedView && mLastVisiblePosition < itemCount);

        if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);
    }

    // TODO: move it to layout-er
    private void calculateCapsuleHalfWidthHeight(RecyclerView.Recycler recycler) {
        if(SHOW_LOGS) Log.v(TAG, ">> calculateCapsuleHalfWidthHeight");

        //Scrap measure one child
        View recycledCapsule = recycler.getViewForPosition(0);
        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleHalfWidthHeight recycledCapsule " + recycledCapsule);

        addView(recycledCapsule);

        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleHalfWidthHeight recycledCapsule.left " + recycledCapsule.getLeft());
        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleHalfWidthHeight recycledCapsule.right " + recycledCapsule.getRight());


        measureChildWithMargins(recycledCapsule, 0, 0);

        /*
         * We make some assumptions in this code based on every child
         * view being the same size (i.e. a uniform grid). This allows
         * us to compute the following values up front because they
         * won't change.
         */

        mHalfDecoratedCapsuleWidth = getDecoratedMeasuredWidth(recycledCapsule) / 2;
        mHalfDecoratedCapsuleHeight = getDecoratedMeasuredHeight(recycledCapsule) / 2;

        removeAndRecycleAllViews(recycler);

        if(SHOW_LOGS) Log.v(TAG, "<< calculateCapsuleHalfWidthHeight, mHalfDecoratedCapsuleWidth " + mHalfDecoratedCapsuleWidth + ", mHalfDecoratedCapsuleHeight " + mHalfDecoratedCapsuleHeight);
    }

    /**
     * This is a wrapper method for {@link android.support.v7.widget.RecyclerView#measureChildWithMargins(android.view.View, int, int, int, int)}
     *
     * If capsules width is "match_parent" then we we need to return "false" from {@link #canScrollHorizontally()}
     * If capsules height is "match_parent" then we we need to return "false" from {@link #canScrollVertically()}
     *
     * This method simply changes return values of {@link #canScrollHorizontally()} and {@link #canScrollVertically()} while measuring
     * size of a child view
     *
     * @param child
     * @param widthUsed
     * @param heightUsed
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
