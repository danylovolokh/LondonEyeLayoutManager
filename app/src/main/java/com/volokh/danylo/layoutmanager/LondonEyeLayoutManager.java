package com.volokh.danylo.layoutmanager;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.volokh.danylo.Config;

import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Created by danylo.volokh on 10/17/2015.
 */
public class LondonEyeLayoutManager extends RecyclerView.LayoutManager {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LondonEyeLayoutManager.class.getSimpleName();

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

    private static final int ANGLE_DELTA = 2;

    private OrientationHelper mOrientationHelper;
    private int mOrientation;

    interface Circle{
        int FIRST_QUADRANT = 1;
        int SECOND_QUADRANT = 2;
        int THIRD_QUADRANT = 3;
        int FOURTH_QUADRANT = 4;
    }

    private final int mRadius;

    private int mDecoratedCapsuleWidth;
    private int mDecoratedCapsuleHeight;

    private int mFirstVisiblePosition;

    private int mCurrentViewPosition;

    private int mOriginY;
    private int mOriginX;

    public LondonEyeLayoutManager(FragmentActivity activity, int screenWidthPixels) {
        mRadius = screenWidthPixels / 2;
        requestLayout();
    }

    public void setHasFixedSizeCapsules(boolean hasFixedSizeCapsules){
        mHasFixedSizeCapsules = hasFixedSizeCapsules;
    }

    static class LayoutState {

        final static String TAG = "LinearLayoutManager#LayoutState";

        final static int LAYOUT_START = -1;

        final static int LAYOUT_END = 1;

        final static int INVALID_LAYOUT = Integer.MIN_VALUE;

        final static int ITEM_DIRECTION_HEAD = -1;

        final static int ITEM_DIRECTION_TAIL = 1;

        final static int SCOLLING_OFFSET_NaN = Integer.MIN_VALUE;

        private static final boolean DEBUG = true;

        /**
         * We may not want to recycle children in some cases (e.g. layout)
         */
        boolean mRecycle = true;

        /**
         * Pixel offset where layout should start
         */
        int mOffset;

        /**
         * Number of pixels that we should fill, in the layout direction.
         */
        int mAvailable;

        /**
         * Current position on the adapter to get the next item.
         */
        int mCurrentPosition;

        /**
         * Defines the direction in which the data adapter is traversed.
         * Should be {@link #ITEM_DIRECTION_HEAD} or {@link #ITEM_DIRECTION_TAIL}
         */
        int mItemDirection;

        /**
         * Defines the direction in which the layout is filled.
         * Should be {@link #LAYOUT_START} or {@link #LAYOUT_END}
         */
        int mLayoutDirection;

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        int mScrollingOffset;

        /**
         * Used if you want to pre-layout items that are not yet visible.
         * The difference with {@link #mAvailable} is that, when recycling, distance laid out for
         * {@link #mExtra} is not considered to avoid recycling visible children.
         */
        int mExtra = 0;

        /**
         * Equal to {@link RecyclerView.State#isPreLayout()}. When consuming scrap, if this value
         * is set to true, we skip removed views since they should not be laid out in post layout
         * step.
         */
        boolean mIsPreLayout = false;

        /**
         * The most recent {@link #scrollBy(int, RecyclerView.Recycler, RecyclerView.State)} amount.
         */
        int mLastScrollDelta;

        /**
         * When LLM needs to layout particular views, it sets this list in which case, LayoutState
         * will only return views from this list and return null if it cannot find an item.
         */
        List<RecyclerView.ViewHolder> mScrapList = null;

        /**
         * @return true if there are more items in the data adapter
         */
        boolean hasMore(RecyclerView.State state) {
            return mCurrentPosition >= 0 && mCurrentPosition < state.getItemCount();
        }

        /**
         * Gets the view for the next element that we should layout.
         * Also updates current item index to the next item, based on {@link #mItemDirection}
         *
         * @return The next element that we should layout.
         */
        View next(RecyclerView.Recycler recycler) {
            if (mScrapList != null) {
                return nextViewFromScrapList();
            }
            final View view = recycler.getViewForPosition(mCurrentPosition);
            mCurrentPosition += mItemDirection;
            return view;
        }

        /**
         * Returns the next item from the scrap list.
         * <p>
         * Upon finding a valid VH, sets current item position to VH.itemPosition + mItemDirection
         *
         * @return View if an item in the current position or direction exists if not null.
         */
        private View nextViewFromScrapList() {
            final int size = mScrapList.size();
            for (int i = 0; i < size; i++) {
                final View view = mScrapList.get(i).itemView;
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (lp.isItemRemoved()) {
                    continue;
                }
                if (mCurrentPosition == lp.getViewLayoutPosition()) {
                    assignPositionFromScrapList(view);
                    return view;
                }
            }
            return null;
        }

        public void assignPositionFromScrapList() {
            assignPositionFromScrapList(null);
        }

        public void assignPositionFromScrapList(View ignore) {
            final View closest = nextViewInLimitedList(ignore);
            if (closest == null) {
                mCurrentPosition = NO_POSITION;
            } else {
                mCurrentPosition = ((RecyclerView.LayoutParams) closest.getLayoutParams())
                        .getViewLayoutPosition();
            }
        }

        public View nextViewInLimitedList(View ignore) {
            int size = mScrapList.size();
            View closest = null;
            int closestDistance = Integer.MAX_VALUE;
            if (DEBUG && mIsPreLayout) {
                throw new IllegalStateException("Scrap list cannot be used in pre layout");
            }
            for (int i = 0; i < size; i++) {
                View view = mScrapList.get(i).itemView;
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (view == ignore || lp.isItemRemoved()) {
                    continue;
                }
                final int distance = (lp.getViewLayoutPosition() - mCurrentPosition) *
                        mItemDirection;
                if (distance < 0) {
                    continue; // item is not in current direction
                }
                if (distance < closestDistance) {
                    closest = view;
                    closestDistance = distance;
                    if (distance == 0) {
                        break;
                    }
                }
            }
            return closest;
        }

        void log() {
            Log.d(TAG, "avail:" + mAvailable + ", ind:" + mCurrentPosition + ", dir:" +
                    mItemDirection + ", offset:" + mOffset + ", layoutDir:" + mLayoutDirection);
        }
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

        int previoutViewBottom = 0;

        for(int i=0; i< 2; i++){
            View view = recycler.getViewForPosition(i);
            addView(view);
            layoutInFourthQuadrant(view, recycler, previoutViewBottom/*This is Recycler view Top == 0*/);
            previoutViewBottom = view.getBottom();
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
     *       THIRD_QUADRANT     |         _
     *                          |         /|
     *                          |       _/  we are going on the circle in this direction
     *                          |
     *                          |
     */
    private void layoutInFourthQuadrant(View view, RecyclerView.Recycler recycler, int previousViewBottom) {

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

        int angleDegree = 360; // TODO: calculate it using bottom of previous view or save latest value

        int viewCenterY = (int) (mOriginY + sineInQuadrant(angleDegree, 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewCenterY " + viewCenterY);

        int halfViewHeight = decoratedCapsuleHeight / 2;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewHeight " + halfViewHeight);

        // viewTop is higher than viewCenterY. And "higher" is up. That's why we subtract halfViewHeight;
        int viewTop = viewCenterY - halfViewHeight;

        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTop " + viewTop);

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

        // When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
        boolean viewTopIsNotAtTheContainerTop = isViewTopHigherThenViewCenter(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, initial viewTopIsNotAtTheContainerTop " + viewTopIsNotAtTheContainerTop);

        if(!viewTopIsNotAtTheContainerTop){
            throw new RuntimeException("Developer error. 'Top of view' cannot be lower than 'top of container' when we just start to calculate");
        }

        // while current "view top" didn't reach the bottom of previous view we decrease the angle and calculate the "top of view"
        do {
            angleDegree -= ANGLE_DELTA;
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new decreased angleDegree " + angleDegree);
            double sine = sineInQuadrant(angleDegree, 4);
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, sine " + sine);

            viewCenterY = (int) (mOriginY + sine * mRadius);
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, new viewCenterY " + viewCenterY);

            viewTop = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTop " + viewTop);

            viewTopIsNotAtTheContainerTop = isViewTopHigherThenViewCenter(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
            if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewTopIsAtTheContainerTop " + viewTopIsNotAtTheContainerTop);
            if(angleDegree < 270){
                throw new RuntimeException("angleDegree less then 270");
            }
        } while (viewTopIsNotAtTheContainerTop);

        int left, top, right, bottom;

        top = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
        bottom = viewCenterY - (halfViewHeight * getQuadrantSinMultiplier(4));

        int halfViewWidth = decoratedCapsuleWidth / 2;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, halfViewWidth " + halfViewWidth);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, angleDegree " + angleDegree);

        int viewCenterX = (int) (mOriginX + cosineInQuadrant(angleDegree, 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, viewCenterX " + viewCenterX);

        left = viewCenterX - halfViewWidth;
        right = left + viewCenterX + halfViewWidth;
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getWidth " + getWidth());
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, getHeight " + getHeight());

        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, left " + left);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, top " + top);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, right " + right);
        if(SHOW_LOGS) Log.v(TAG, "layoutInFourthQuadrant, bottom " + bottom);

        layoutDecorated(view, left, top, right, bottom);
    }

    /**
     * View top is higher when it's smaller then previous View Bottom
     */
    private boolean isViewTopHigherThenViewCenter(int previousViewBottom, int viewTop) {
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
