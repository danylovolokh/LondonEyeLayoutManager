package com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper;

import android.util.Log;
import android.view.View;

import com.volokh.danylo.utils.Config;
import com.volokh.danylo.layoutmanager.ViewData;
import com.volokh.danylo.layoutmanager.circle_helper.circle_points_creator.FirstQuadrantCirclePointsCreator;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;

import java.util.HashMap;
import java.util.Map;

/**
 * First quadrant isn't really 1st. Mainly views are laid out in 1st quadrant,
 * but the last view can be laid out partially in 2nd quadrant.
 * And the first view can be laid out partially in 4th quadrant.
 *
 *                  -y |                    \
 *                     |           4th       \
 *                     |______                V
 *                     |      --_
 *                     |         \_      x    |
 *                     |           |          |
 *        -------------|--------------->      V
 *        |_           |          _|
 *          \_         |        _/            /
 *            --_______|______--             /
 *      2nd            |            1st     V
 *                     |
 *        <--       +y V              <---
 *
 */
public class FirstQuadrantHelper implements QuadrantHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = FirstQuadrantHelper.class.getSimpleName();

    private final Map<Integer, Point> mCircleIndexPoint;
    private final Map<Point, Integer> mCirclePointIndex;

    private final int mRadius;

    public FirstQuadrantHelper(int radius, int xOrigin, int yOrigin) {
        mRadius = radius;

        mCircleIndexPoint = new HashMap<>();
        mCirclePointIndex = new HashMap<>();

        if(SHOW_LOGS) Log.v(TAG, ">> constructor, start filling sector points");
        long start = System.currentTimeMillis();

        FirstQuadrantCirclePointsCreator quadrantCirclePointsCreator = new FirstQuadrantCirclePointsCreator(radius, xOrigin, yOrigin);
        quadrantCirclePointsCreator.fillCirclePoints(mCircleIndexPoint, mCirclePointIndex);

        if(SHOW_LOGS) Log.v(TAG, "<< constructor, finished filling sector points in " + (System.currentTimeMillis() - start));
    }

    /**
     * This method looks for a next point clockwise. 4th, 1st, 2nd quadrants in that order.
     * It is using {@link #mCirclePointIndex} and {@link #mCircleIndexPoint} to get point on the circle.
     *
     *     ^ We end here          -->  --> We start here
     *    /             -y |                    \
     *   /                 |           4th       \
     *               ______|______                V
     *   ^        _--      |      --_
     *   |     _/          |         \_      x    |
     *   |    |            |           |          |
     *        -------------|--------------->      V
     *   ^    |_           |          _|
     *   |      \_         |        _/            /
     *   |        --_______|______--             /
     *      2nd            |            1st     V
     *   ^                 |
     *    \   <--       +y V              <---
     *     \
     *
     *     We have previousViewData. And center of previous view in previousViewData;
     *
     *     The algorithm of finding next view is following:
     *
     *     1. We get "next view center" using "previous view center"
     *     2. Calculate next view top, bottom, right
     *
     *     3. We check if "next view top" is below "previous view bottom"
     *
     *                  -y |                    \
     *                     |           4th       \
     *                     |______                V
     *                     |      --_
     *                     |         \_      x    |
     *                     |           |          |
     *                     |           |          |
     *        -------------|--------------->      V
     *        |            |      _____|___
     *        |_           |     |    _|   | previous view
     *          \_         | ____|___/_____|
     *            --_______||______-- | next view
     *      2nd            ||_________|
     *                     |
     *        <--       +y V              <---
     *
     *
     *     4. We check if "next view bottom" is above "previous view top"
     *
     *                  -y |
     *                     |
     *                     |______
     *                     |      --_
     *                     |         \_      x
     *                     |           |
     *                     |           |
     *        -------------|--------------->
     *    ____|_________   |           |
next view|    |_        |  |          _|
     *   |______\_______|__|_________/
     *            --_|_____|_______-- | previous view
     *               |_____|__________|
     *                     |
     *                  +y V
     *
     *     5. We check if next view is "to the left" of previous view
     *                  -y |
     *                     |
     *                     |______
     *                     |      --_
     *                     |         \_      x
     *                     |           |
     *                     |           |
     *        -------------|--------------->
     *    ____|_________   |           |
     next view|_        |  |          _|
     *   |______\_______|__|_________/
     *            --____|__|_______-- | previous view
     *                  |__|__________|
     *                     |
     *                  +y V
     *
     *     5. If any condition from 3, 4, 5 match then we found a center on the circle for the next view.
     */
    @Override
    public Point findNextViewCenter(ViewData previousViewData, int nextViewHalfViewWidth, int nextViewHalfViewHeight) {

        Point previousViewCenter = previousViewData.getCenterPoint();

        Point nextViewCenter;

        boolean foundNextViewCenter;
        do {

            /** 1. */
            nextViewCenter = getNextViewCenter(previousViewCenter);

            /** 2. */
            int nextViewTop = nextViewCenter.getY() - nextViewHalfViewHeight;
            int nextViewBottom = nextViewCenter.getY() + nextViewHalfViewHeight;
            int nextViewRight = nextViewCenter.getX() + nextViewHalfViewWidth;

            /** 3. */
            boolean nextViewTopIsBelowPreviousViewBottom = nextViewTop >= previousViewData.getViewBottom();
            /** 4. */
            boolean nextViewBottomIsAbovePreviousViewTop = nextViewBottom <= previousViewData.getViewTop();
            /** 5. */
            boolean nextViewIsToTheLeftOfThePreviousView = nextViewRight <= previousViewData.getViewLeft();

            foundNextViewCenter = nextViewTopIsBelowPreviousViewBottom || nextViewIsToTheLeftOfThePreviousView || nextViewBottomIsAbovePreviousViewTop;

            // "next view center" become previous
            previousViewCenter = nextViewCenter;
        } while (!foundNextViewCenter);

        return nextViewCenter;
    }

    /**
     * We start from previous view center point.
     * Here is the flow :
     *
     * 1. We get an index of previousViewCenter
     * 2. We increment the index.
     * 3. Correct received index. We might reach zero of last index
     * 4. We get next point using index
     *
     */
    private Point getNextViewCenter(Point previousViewCenter) {

        /** 1. */
        int previousViewCenterPointIndex = mCirclePointIndex.get(previousViewCenter);

        /** 2. */
        int newIndex = previousViewCenterPointIndex + 1;
        int lastIndex = mCircleIndexPoint.size() - 1;

        /** 3. if index is bigger than last index mean we exceeded the the limit and should start from zero. New index should be at the circle points start*/
        int nextViewCenterPointIndex = newIndex > lastIndex ?
                newIndex - lastIndex :
                newIndex;

        /** 4. */
        Point nextViewCenter = mCircleIndexPoint.get(nextViewCenterPointIndex);

//        if(SHOW_LOGS) Log.v(TAG, "getNextViewCenter, nextViewCenter " + nextViewCenter);
        return nextViewCenter;
    }

    private Point getPreviousViewCenter(Point nextViewCenter) {
        /** 1. */
        int nextViewCenterPointIndex = mCirclePointIndex.get(nextViewCenter);

        /** 2. */
        int newIndex = nextViewCenterPointIndex - 1;

        int lastIndex = mCircleIndexPoint.size() - 1;

        /** 3. */
        int previousViewCenterPointIndex = newIndex < 0 ?
                lastIndex + newIndex: // this will subtract newIndex from last index
                newIndex;

        /** 4. */
        return mCircleIndexPoint.get(previousViewCenterPointIndex);
    }

    @Override
    public int getViewCenterPointIndex(Point point) {
        return mCirclePointIndex.get(point);
    }

    @Override
    public Point getViewCenterPoint(int newCenterPointIndex) {
        return mCircleIndexPoint.get(
                newCenterPointIndex
        );
    }

    @Override
    public int getNewCenterPointIndex(int newCalculatedIndex) {

        int lastIndex = mCircleIndexPoint.size() - 1;
        int correctedIndex;
        if(newCalculatedIndex < 0){
            correctedIndex = lastIndex + newCalculatedIndex;
        } else {
            correctedIndex = newCalculatedIndex > lastIndex ?
                    newCalculatedIndex - lastIndex :
                    newCalculatedIndex;
        }

        return correctedIndex;
    }

    /**
     * This method looks for a previous point counterclockwise. 2nd, 1st, 4th  quadrants in that order. But in inverted y axis
     *
     *     We have "nextViewData". And center of next view in "nextViewData";
     *
     *     The algorithm of finding next view is following:
     *
     *     1. We get "previous view center" using "next view center"
     *     2. Calculate "previous view bottom"
     *
     *     3. If "previous view bottom" is above "next view top" then we found center of previous view on the circle
     *
     *                  -y |
     *                     |           4th
     *                     |______
     *                     |      --_
     *                     |         \_      x
     *                     |        ___|_______
     *                     |       |   |       |  previous view
     *        -------------|--------------------------->
     *        |            |      _|___|_______|
     *        |_           |     |    _|   |  next view
     *          \_         |     |___/_____|
     *            --_______|_______--
     *      2nd            |
     *                     |
     *                  +y V
     *  Be careful!
     *  Logic described above means that we are handling only specific cases when circle is going through device display starting from top edge:
     *
     *  Example 1
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
     *  Example 2
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
     *  Not handled case !
     *                  __________
     *   ___________   |          | In this case view might appear somewhere outside the display. B
     *  |           |  |/_________|
     *  |           | /
     *  |           |/
     *  |       ___/|
     *  |     _/    |
     *  |____/      |
     *  |           |
     *  |___________|
     *
     *
     */
    @Override
    public Point findPreviousViewCenter(ViewData nextViewData, int previousViewHalfViewHeight) {

        Point nextViewCenter = nextViewData.getCenterPoint();

        Point previousViewCenter;

        boolean foundNextViewCenter;
        do {
            /** 1.*/
            previousViewCenter = getPreviousViewCenter(nextViewCenter);

            /** 2. */
            int previousViewBottom = previousViewCenter.getY() + previousViewHalfViewHeight;

            boolean previousViewBottomIsAboveNextViewTop = previousViewBottom < nextViewData.getViewTop();

            foundNextViewCenter = previousViewBottomIsAboveNextViewTop;

            // "previous view center" become next
            nextViewCenter = previousViewCenter;
        } while (!foundNextViewCenter);

        if(SHOW_LOGS) Log.v(TAG, "<< findPreviousViewCenter, findPreviousViewCenter " + foundNextViewCenter);
        return nextViewCenter;
    }

    /**
     * This method checks if this is last visible layouted view.
     * The return might be used to know if we should stop laying out
     * TODO: use this method in Scroll Handler
     */
    @Override
    public boolean isLastLayoutedView(int recyclerHeight, View view) {
        boolean isLastLayoutedView;
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, recyclerHeight " + recyclerHeight);
        int spaceToLeftEdge = view.getLeft();
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, spaceToLeftEdge " + spaceToLeftEdge);
        int spaceToBottomEdge = view.getBottom();
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, spaceToBottomEdge " + spaceToBottomEdge);
        isLastLayoutedView = spaceToLeftEdge <= 0 || spaceToBottomEdge >= recyclerHeight;
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, " + isLastLayoutedView);
        return isLastLayoutedView;
    }

    @Override
    public int checkBoundsReached(int recyclerViewHeight, int dy, View firstView, View lastView, boolean isFirstItemReached, boolean isLastItemReached) {
        int delta;
        if (SHOW_LOGS) {
            Log.v(TAG, "checkBoundsReached, isFirstItemReached " + isFirstItemReached);
            Log.v(TAG, "checkBoundsReached, isLastItemReached " + isLastItemReached);
        }
        if (dy > 0) { // Contents are scrolling up
            //Check against bottom bound
            if (isLastItemReached) {
                //If we've reached the last row, enforce limits
                int bottomOffset = getOffset(recyclerViewHeight, lastView);
                delta = Math.max(-dy, bottomOffset);
            } else {

                delta = -dy;
            }
        } else { // Contents are scrolling down
            //Check against top bound
            int topOffset = firstView.getTop();
            if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, topOffset " + topOffset);
            if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, dy " + dy);

            if (isFirstItemReached) {
                delta = -Math.max(dy, topOffset); // stoled from FixedGrid
            } else {
                delta = -dy;
            }
        }
        if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, delta " + delta);
        return delta;
    }

    @Override
    public int getOffset(int recyclerViewHeight, View lastView) {

        int offset;

        if (SHOW_LOGS) Log.v(TAG, "getOffset, recyclerViewHeight " + recyclerViewHeight);

        int lastViewLeft = lastView.getLeft();
        int lastViewBottomOffset = lastView.getBottom() - recyclerViewHeight;
        if (lastViewLeft <= 0) {
            if (lastViewBottomOffset > 0) { // outside of recycler
                offset = Math.min(lastViewLeft, -lastViewBottomOffset);
            } else {
                offset = lastViewLeft;
            }
        } else {
            offset = -lastViewBottomOffset;
        }
        if (SHOW_LOGS) {
            Log.v(TAG, "getOffset lastViewLeft " + lastViewLeft + ", lastViewBottomOffset " + lastViewBottomOffset);
            Log.v(TAG, "getOffset, offset" + offset);
        }
        return offset;
    }
}
