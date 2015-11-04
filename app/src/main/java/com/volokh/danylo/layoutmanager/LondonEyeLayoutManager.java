package com.volokh.danylo.layoutmanager;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.UnitCircleFourthQuadrantHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by danylo.volokh on 10/17/2015.
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

    private final UnitCircleFourthQuadrantHelper mUnitCircleHelper;

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

    private OrientationHelper mOrientationHelper;
    private int mOrientation;

    interface Circle{
        int FIRST_QUADRANT = 1;
        int SECOND_QUADRANT = 2;
        int THIRD_QUADRANT = 3;
        int FOURTH_QUADRANT = 4;
    }

    /**
     * This value is healed for optimization. We don't calculate angle from 360 every time.
     * We reuse this value that was set during previous view layout.
     */
    private AtomicInteger mFourthQuadrantLastAngle = new AtomicInteger();

    private final int mRadius;

    private int mDecoratedCapsuleWidth;
    private int mDecoratedCapsuleHeight;

    private int mFirstVisiblePosition;

    private int mCurrentViewPosition;

    private int mOriginY;
    private int mOriginX;

    public LondonEyeLayoutManager(FragmentActivity activity, int screenWidthPixels) {
        mRadius = screenWidthPixels / 2;
        mUnitCircleHelper = new UnitCircleFourthQuadrantHelper(mRadius);
        requestLayout();
    }

    public void setHasFixedSizeCapsules(boolean hasFixedSizeCapsules){
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
        return mCanScrollVerticallyHorizontally;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if(mHasFixedSizeCapsules){
            // perform an optimization. If mHasFixedSizeCapsules == true we will calculate a size of views only once
            calculateCapsuleWidthHeight(recycler);
        }

        mCurrentViewPosition = 0;

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, state " + state);

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, mRadius " + mRadius);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, mOriginY " + mOriginY);

        int previousViewBottom = 0;
        mFourthQuadrantLastAngle.set(360);

        for(int i = 0; i < 4; i++){
            View view = recycler.getViewForPosition(i);
            addView(view);
            layoutInFourthQuadrant(view, recycler, previousViewBottom/*This is Recycler view Top == 0*/);
            previousViewBottom = view.getBottom();
        }
    }

    private void calculateCapsuleWidthHeight(RecyclerView.Recycler recycler) {
        if(SHOW_LOGS) Log.v(TAG, ">> calculateCapsuleWidthHeight");

        //Scrap measure one child
        View recycledCapsule = recycler.getViewForPosition(0);
        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleWidthHeight recycledCapsule " + recycledCapsule);

        addView(recycledCapsule);

        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleWidthHeight recycledCapsule.left " + recycledCapsule.getLeft());
        if(SHOW_LOGS) Log.v(TAG, "calculateCapsuleWidthHeight recycledCapsule.right " + recycledCapsule.getRight());


        measureChildWithMargins(recycledCapsule, 0, 0);

        /*
         * We make some assumptions in this code based on every child
         * view being the same size (i.e. a uniform grid). This allows
         * us to compute the following values up front because they
         * won't change.
         */

        mDecoratedCapsuleWidth = getDecoratedMeasuredWidth(recycledCapsule);
        mDecoratedCapsuleHeight = getDecoratedMeasuredHeight(recycledCapsule);

        removeAndRecycleAllViews(recycler);

        if(SHOW_LOGS) Log.v(TAG, "<< calculateCapsuleWidthHeight, mDecoratedCapsuleWidth " + mDecoratedCapsuleWidth + ", mDecoratedCapsuleHeight " + mDecoratedCapsuleHeight);
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

    public int getDecoratedMeasurement(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    /**
     *                          |
     *                          |
     *       SECOND_QUADRANT    |    FIRST_QUADRANT
     *                          |
     *                          |
     *     -------------------------------------------
     *                          |    FOURTH_QUADRANT
     *       THIRD_QUADRANT     |
     *                          |         /
     *                          |       |/  we are going on the circle in this direction
     *                          |       |_
     *                          |
     */
    private void layoutInFourthQuadrant(View view, RecyclerView.Recycler recycler, int previousViewBottom) {
        if(SHOW_LOGS) Log.v(TAG, ">> layoutInFourthQuadrant");

        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, previousViewBottom " + previousViewBottom);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);

        int decoratedCapsuleWidth;
        int decoratedCapsuleHeight;

        if(mHasFixedSizeCapsules){
            // this is an optimization
            if(mDecoratedCapsuleWidth == 0 || mDecoratedCapsuleHeight == 0){
                throw new RuntimeException("mDecoratedCapsuleWidth " + mDecoratedCapsuleWidth + ", mDecoratedCapsuleHeight " + mDecoratedCapsuleHeight + ", values should be calculated earlier");
            }
            decoratedCapsuleWidth = mDecoratedCapsuleWidth;
            decoratedCapsuleHeight = mDecoratedCapsuleHeight;
        } else {
            measureChildWithMargins(view, 0, 0);
            decoratedCapsuleWidth = getDecoratedMeasuredWidth(view);
            decoratedCapsuleHeight = getDecoratedMeasuredHeight(view);
        }
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleWidth " + decoratedCapsuleWidth);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, decoratedCapsuleHeight " + decoratedCapsuleHeight);

        int viewCenterY = (int) (mOriginY + sineInQuadrant(mFourthQuadrantLastAngle.get(), 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewCenterY " + viewCenterY);

        int halfViewHeight = decoratedCapsuleHeight / 2;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewHeight " + halfViewHeight);

        // viewTop is higher than viewCenterY. And "higher" is up. That's why we subtract halfViewHeight;
        int viewTop = viewCenterY - halfViewHeight;

        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTop " + viewTop);

        viewCenterY = mUnitCircleHelper.findViewCenterY(previousViewBottom, halfViewHeight, viewTop, mFourthQuadrantLastAngle);

        int left, top, right, bottom;

        top = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
        bottom = viewCenterY - (halfViewHeight * getQuadrantSinMultiplier(4));

        int halfViewWidth = decoratedCapsuleWidth / 2;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewWidth " + halfViewWidth);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);

        int viewCenterX = (int) (mOriginX + cosineInQuadrant(mFourthQuadrantLastAngle.get(), 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewCenterX " + viewCenterX);

        left = viewCenterX - halfViewWidth;
        right = viewCenterX + halfViewWidth;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getWidth " + getWidth());
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getHeight " + getHeight());

        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, left " + left);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, top " + top);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, right " + right);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, bottom " + bottom);

        layoutDecorated(view, left, top, right, bottom);
        if(SHOW_LOGS) Log.v(TAG, "<< layoutInFourthQuadrant");
    }

    private int findViewCenterY(int previousViewBottom, int halfViewHeight, int viewTop) {
        // Right now we need to decrease the angle.
        // Because we are in four quadrant. We can decrease from 360 to 270.
        /**
         *      |
         *      |
         *------|------
         *      |       / We are in this quadrant and going in this way.
         *      |  /___/
         *         \
         */

        int viewCenterY;// When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
        boolean viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);

        // while current "view top" didn't reach the bottom of previous view we decrease the angle and calculate the "top of view"
        do {
            mFourthQuadrantLastAngle.set(mFourthQuadrantLastAngle.get() - ANGLE_DELTA);
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new decreased mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle);
            double sine = sineInQuadrant(mFourthQuadrantLastAngle.get(), 4);
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, sine " + sine);

            viewCenterY = (int) (mOriginY + sine * mRadius);
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new viewCenterY " + viewCenterY);

            viewTop = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTop " + viewTop);

            viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);
            if(mFourthQuadrantLastAngle.get() < 270){
                if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, mFourthQuadrantLastAngle " + mFourthQuadrantLastAngle + ", break");

//                break;
                throw new RuntimeException("angleDegree less then 270");
            }
        } while (viewTopIsHigherThenPreviousViewBottom);
        return viewCenterY;
    }

    /**
     * View top is higher when it's smaller then previous View Bottom
     */
    private boolean isViewTopHigherThenPreviousViewBottom(int previousViewBottom, int viewTop) {
        return viewTop < previousViewBottom;
    }

    public int getDecoratedMeasurementInOther(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    private double cosineInQuadrant(int angleDegree, int quadrant) {
        return Math.cos(Math.toRadians(angleDegree));
    }


    /**
     *                          |
     *                          |
     *       SECOND_QUADRANT    |   FIRST_QUADRANT
     *                          |
     *                          |
     *     -------------------------------------------
     *                          |
     *       THIRD_QUADRANT     |   FOURTH_QUADRANT
     *                          |
     *                          |
     *                          |
     *                          |
     */
    private int getQuadrantSinMultiplier(int quadrant) {
        int quadrantCorrectionMultiplier;
        switch (quadrant){
            case Circle.FIRST_QUADRANT:
                throw new RuntimeException("not handled yet");

            case Circle.SECOND_QUADRANT:
                throw new RuntimeException("not handled yet");

            case Circle.THIRD_QUADRANT:
                throw new RuntimeException("not handled yet");

            case Circle.FOURTH_QUADRANT:
                quadrantCorrectionMultiplier = -1;
                break;
            default:
                throw new RuntimeException("not handled yet");
        }
        return quadrantCorrectionMultiplier;
    }

    /**
     * This method returns a sine multiplied by correction value.
     * We need it because y axis positive direction is down, device wise.
     * And in Cartesian coordinate system positive direction is up.
     */
    private double sineInQuadrant(int angleDegree, int quadrant) {

        double correctedSine = Math.sin(Math.toRadians(angleDegree)) * getQuadrantSinMultiplier(quadrant);
        if(SHOW_LOGS) Log.v(TAG, String.format("sineInQuadrant, correctedSine %f", correctedSine));
        return correctedSine;
    }
}
