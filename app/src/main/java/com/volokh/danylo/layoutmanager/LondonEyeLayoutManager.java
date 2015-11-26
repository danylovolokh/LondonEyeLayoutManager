package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.UnitCircleFourthQuadrantHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by danylo.volokh on 10/17/2015.
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager implements LayouterCallback {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

    private final UnitCircleFourthQuadrantHelper mUnitCircleHelper;

    private final RecyclerView mRecyclerView;

    private final Layouter mLayouter;

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

    private int mLastVisiblePosition; //TODO: restore state

    private int mCurrentViewPosition;

    private int mOriginY;
    private int mOriginX;

    public LondonEyeLayoutManager(FragmentActivity activity, int radius, RecyclerView recyclerView) {
        mRadius = radius;

        mUnitCircleHelper = new UnitCircleFourthQuadrantHelper(
                mRadius);

        mRecyclerView = recyclerView;

        requestLayout(); // TODO: check if I need this
        mLayouter = new Layouter(this, mRadius, 0, 0); // TODO: get from constructor
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
//        offsetChildrenVertical(dy);
        for(int indexOfView = 0; indexOfView < childCount; indexOfView++){
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy indexOfView " + indexOfView);
            mLayouter.scrollVerticallyBy(getChildAt(indexOfView), dy, indexOfView);
        }

        return 0;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, "scrollHorizontallyBy dx " + dx);

        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, ">> onLayoutChildren, state " + state);

        removeAllViews();

        mLayouter.reset();
        mLastVisiblePosition = 0;

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


        ViewData viewData = null;

        // when this variable will be false it mean that we have layout-ed a view outside of the RecyclerView.
        // It will be our stop flag
        boolean isLayoutedViewVisible;

        int index = 0;
        do{
            View view = recycler.getViewForPosition(mLastVisiblePosition);
            addView(view);
            viewData = mLayouter.layoutView(view, viewData);

            boolean isViewFullyVisible = isViewOnTheScreen(view);
            if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, isViewOnTheScreen " + isViewFullyVisible);

            // We update coordinates instead of creating new object to keep the heap clean
            if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewData " + viewData);

            isLayoutedViewVisible = viewData.isViewVisible();
            if(isLayoutedViewVisible){
                mLastVisiblePosition++;
            }

        } while (index++ < 0);

    }

    private boolean isViewOnTheScreen(View view) {
        Rect visibleRect = new Rect();
        boolean isVisible = view.getLocalVisibleRect(visibleRect);
        if(SHOW_LOGS) Log.v(TAG, "isViewOnTheScreen isVisible " + isVisible);
        if(SHOW_LOGS) Log.v(TAG, "isViewOnTheScreen visibleRect " + visibleRect);

        return view.getLocalVisibleRect(visibleRect);
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

//    /**
//     *                          |
//     *                          |
//     *       SECOND_QUADRANT    |    FIRST_QUADRANT
//     *                          |
//     *                          |
//     *     -------------------------------------------
//     *                          |    FOURTH_QUADRANT
//     *       THIRD_QUADRANT     |
//     *                          |         /
//     *                          |       |/  we are going on the circle in this direction
//     *                          |       |_
//     *                          |
//     */
//    private void layoutInFourthQuadrant(View view, RecyclerView.Recycler recycler, int previousViewBottom) {
//        if(SHOW_LOGS) Log.v(TAG, ">> layoutInFourthQuadrant");
//
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, previousViewBottom " + previousViewBottom);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);
//
//        int decoratedCapsuleWidth;
//        int decoratedCapsuleHeight;
//
//        if(mHasFixedSizeCapsules){
//            // this is an optimization
//            if(mHalfDecoratedCapsuleWidth == 0 || mHalfDecoratedCapsuleHeight == 0){
//                throw new RuntimeException("mHalfDecoratedCapsuleWidth " + mHalfDecoratedCapsuleWidth + ", mHalfDecoratedCapsuleHeight " + mHalfDecoratedCapsuleHeight + ", values should be calculated earlier");
//            }
//            decoratedCapsuleWidth = mHalfDecoratedCapsuleWidth;
//            decoratedCapsuleHeight = mHalfDecoratedCapsuleHeight;
//        } else {
//            measureChildWithMargins(view, 0, 0);
//            decoratedCapsuleWidth = getDecoratedMeasuredWidth(view);
//            decoratedCapsuleHeight = getDecoratedMeasuredHeight(view);
//        }
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleWidth " + decoratedCapsuleWidth);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleHeight " + decoratedCapsuleHeight);
//
//        int viewCenterY = (int) (mOriginY + sineInQuadrant(mFourthQuadrantLastAngle.get(), 4) * mRadius);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewCenterY " + viewCenterY);
//
//        int halfViewHeight = decoratedCapsuleHeight / 2;
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewHeight " + halfViewHeight);
//
//        // viewTop is higher than viewCenterY. And "higher" is up. That's why we subtract halfViewHeight;
//        int viewTop = viewCenterY - halfViewHeight;
//
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTop " + viewTop);
//
//        viewCenterY = mUnitCircleHelper.findViewCenterY(previousViewBottom, halfViewHeight, viewTop, mFourthQuadrantLastAngle);
//
//        int left, top, right, bottom;
//
//        top = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
//        bottom = viewCenterY - (halfViewHeight * getQuadrantSinMultiplier(4));
//
//        int halfViewWidth = decoratedCapsuleWidth / 2;
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewWidth " + halfViewWidth);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);
//
//        int viewCenterX = (int) (mOriginX + cosineInQuadrant(mFourthQuadrantLastAngle.get(), 4) * mRadius);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewCenterX " + viewCenterX);
//
//        left = viewCenterX - halfViewWidth;
//        right = viewCenterX + halfViewWidth;
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getWidth " + getWidth());
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getHeight " + getHeight());
//
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, left " + left);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, top " + top);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, right " + right);
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, bottom " + bottom);
//
//        layoutDecorated(view, left, top, right, bottom);
//        if(SHOW_LOGS) Log.v(TAG, "<< layoutInFourthQuadrant");
//    }
//
//    private int findViewCenterY(int previousViewBottom, int halfViewHeight, int viewTop) {
//        // Right now we need to decrease the angle.
//        // Because we are in four quadrant. We can decrease from 360 to 270.
//        /**
//         *      |
//         *      |
//         *------|------
//         *      |       / We are in this quadrant and going in this way.
//         *      |  /___/
//         *         \
//         */
//
//        int viewCenterY;// When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
//        boolean viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
//        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);
//
//        // while current "view top" didn't reach the bottom of previous view we decrease the angle and calculate the "top of view"
//        do {
//            mFourthQuadrantLastAngle.set(mFourthQuadrantLastAngle.get() - ANGLE_DELTA);
//            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new decreased mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);
//            double sine = sineInQuadrant(mFourthQuadrantLastAngle.get(), 4);
//            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, sine " + sine);
//
//            viewCenterY = (int) (mOriginY + sine * mRadius);
//            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new viewCenterY " + viewCenterY);
//
//            viewTop = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
//            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTop " + viewTop);
//
//            viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
//            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);
//            if(mFourthQuadrantLastAngle.get() < 270){
//                if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle + ", break");
//
////                break;
//                throw new RuntimeException("angleDegree less then 270");
//            }
//        } while (viewTopIsHigherThenPreviousViewBottom);
//        return viewCenterY;
//    }
//
//    /**
//     * View top is higher when it's smaller then previous View Bottom
//     */
//    private boolean isViewTopHigherThenPreviousViewBottom(int previousViewBottom, int viewTop) {
//        return viewTop < previousViewBottom;
//    }
//
//    private double cosineInQuadrant(int angleDegree, int quadrant) {
//        return Math.cos(Math.toRadians(angleDegree));
//    }
//
//
//    /**
//     *                          |
//     *                          |
//     *       SECOND_QUADRANT    |   FIRST_QUADRANT
//     *                          |
//     *                          |
//     *     -------------------------------------------
//     *                          |
//     *       THIRD_QUADRANT     |   FOURTH_QUADRANT
//     *                          |
//     *                          |
//     *                          |
//     *                          |
//     */
//    private int getQuadrantSinMultiplier(int quadrant) {
//        int quadrantCorrectionMultiplier;
//        switch (quadrant){
//            case Circle.FIRST_QUADRANT:
//                throw new RuntimeException("not handled yet");
//
//            case Circle.SECOND_QUADRANT:
//                throw new RuntimeException("not handled yet");
//
//            case Circle.THIRD_QUADRANT:
//                throw new RuntimeException("not handled yet");
//
//            case Circle.FOURTH_QUADRANT:
//                quadrantCorrectionMultiplier = -1;
//                break;
//            default:
//                throw new RuntimeException("not handled yet");
//        }
//        return quadrantCorrectionMultiplier;
//    }
//
//    /**
//     * This method returns a sine multiplied by correction value.
//     * We need it because y axis positive direction is down, device wise.
//     * And in Cartesian coordinate system positive direction is up.
//     */
//    private double sineInQuadrant(int angleDegree, int quadrant) {
//
//        double correctedSine = Math.sin(Math.toRadians(angleDegree)) * getQuadrantSinMultiplier(quadrant);
//        if(SHOW_LOGS) Log.v(TAG, String.format("sineInQuadrant, correctedSine %f", correctedSine));
//        return correctedSine;
//    }
}
