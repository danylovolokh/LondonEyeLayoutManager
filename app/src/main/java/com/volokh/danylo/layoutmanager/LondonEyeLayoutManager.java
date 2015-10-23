package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
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

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;

    private int mFirstVisiblePosition;

    private int mCurrentViewPosition;

    private int mOriginY;
    private int mOriginX;

    public LondonEyeLayoutManager(FragmentActivity activity, int screenWidthPixels) {
        mRadius = screenWidthPixels / 2;
        requestLayout();
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
        //We do allow scrolling
        return true; // TODO how to make this true but be able to  use HEIGHT = match_parent for capsules
    }

    @Override
    public boolean canScrollHorizontally() {
        return false; // TODO how to make this true but be able to  use WIDTH = match_parent for capsules
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        calculateCapsuleWidthHeight(recycler);

        mCurrentViewPosition = 0;

        int halfViewHeight = mDecoratedChildHeight / 2;

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, halfViewHeight " + halfViewHeight);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, state " + state);

        layoutInFourthQuadrant(recycler, halfViewHeight);


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

        mDecoratedChildWidth = getDecoratedMeasuredWidth(recycledCapsule);
        mDecoratedChildHeight = getDecoratedMeasuredHeight(recycledCapsule);

        removeAndRecycleAllViews(recycler);
        if(SHOW_LOGS) Log.v(TAG, "<< calculateCapsuleWidthHeight, mDecoratedChildWidth " + mDecoratedChildWidth + ", mDecoratedChildHeight " + mDecoratedChildHeight);
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
    private void layoutInFourthQuadrant(RecyclerView.Recycler recycler, int halfViewHeight) {

        int angleDegree = 270;

        int viewCenterY = (int) (mOriginY + sineInQuadrant(angleDegree, 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, initial viewCenterY " + viewCenterY);

        int viewTop = viewCenterY + halfViewHeight;
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, initial viewTop " + viewTop);
        boolean viewTopIsNotAtTheContainerTop = viewTop > mOriginY && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, initial viewTopIsAtTheContainerTop " + viewTopIsNotAtTheContainerTop);

        // while current view top didn't reach the top of RecyclerView we increase th angle and calculate nthe top.
        // TODO: optimize somehow :)
        while(viewTopIsNotAtTheContainerTop){
            angleDegree += ANGLE_DELTA;
            if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, new increased angleDegree " + angleDegree);
            double sine = sineInQuadrant(angleDegree, 4);
            if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, sine " + sine);

            viewCenterY = (int) (mOriginY + sine * mRadius);
            if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, new viewCenterY " + viewCenterY);

            viewTop = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
            if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewTop " + viewTop);

            viewTopIsNotAtTheContainerTop = viewTop > mOriginY && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
            if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewTopIsAtTheContainerTop " + viewTopIsNotAtTheContainerTop);
        }

        View firstView = recycler.getViewForPosition(0);

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getWidth " + firstView.getWidth());
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getHeight " + firstView.getHeight());
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getLayoutParams " + firstView.getLayoutParams());

        addView(firstView);

        measureChildWithMargins(firstView, 0, 0);

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getWidth " + firstView.getWidth());
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getHeight " + firstView.getHeight());

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getMeasuredWidth " + firstView.getMeasuredWidth());
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, firstView.getMeasuredHeight " + firstView.getMeasuredHeight());
        int left, top, right, bottom;

        top = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
        bottom = viewCenterY - (halfViewHeight * getQuadrantSinMultiplier(4));

        int halfViewWidth = mDecoratedChildWidth / 2;
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, halfViewWidth " + halfViewWidth);

        int viewCenterX = (int) (mOriginX + cosineInQuadrant(angleDegree, 4) * mRadius);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewCenterX " + viewCenterX);

        left = viewCenterX - halfViewWidth;
        right = left + viewCenterX + halfViewWidth;
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, getWidth " + getWidth());
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, getHeight " + getHeight());

        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, left " + left);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, top " + top);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, right " + right);
        if(SHOW_LOGS) Log.v(TAG, "onLayoutChildren, bottom " + bottom);

        layoutDecorated(firstView, left, top, right, bottom);
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
        if(SHOW_LOGS) Log.v(TAG, "sineInQuadrant, correctedSine " + correctedSine);
        return correctedSine;
    }
}
