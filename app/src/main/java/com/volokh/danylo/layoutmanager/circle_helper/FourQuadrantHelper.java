package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.ViewData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danylo.volokh on 11/17/2015.
 */
public class FourQuadrantHelper { // TODO: implements generic quadrant helper

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = FourQuadrantHelper.class.getSimpleName();

    private final Map<Integer, Point> mCircleIndexPoint;
    private final Map<Point, Integer> mCirclePointIndex;

    private final int mRadius;

    public FourQuadrantHelper(int radius, int x0, int y0){

        mRadius = radius;

        mCircleIndexPoint = new HashMap<>();
        mCirclePointIndex = new HashMap<>();

        if(SHOW_LOGS) Log.v(TAG, ">> constructor, start filling sector points");
        long start = System.currentTimeMillis();

        CirclePointsCreator circlePointsCreator = new CirclePointsCreator(mRadius, x0, y0);
        circlePointsCreator.fillCirclePointsCircle(mCircleIndexPoint, mCirclePointIndex);

        if(SHOW_LOGS) Log.v(TAG, "<< constructor, finished filling sector points in " + (System.currentTimeMillis() - start));

    }

    /**
     * This method looks for a next point clockwise. 1st, 4th, 3rd, 2nd quadrants in that order.
     * It changes "y" coordinate like we were going through these quadrants.
     * In 1st and 4th quadrants "y" increases. In 3rd and 2nd in decreases. (Clockwise)
     *
     *     ^ We end here          -->  --> We start here
     *    /             -y |                    \
     *   /                 |           1st       \
     *               ______|______                V
     *   ^        _--      |      --_
     *   |     _/          |         \_      x    |
     *   |    |            |           |          |
     *        -------------|--------------->      V
     *   ^    |_           |          _|
     *   |      \_         |        _/            /
     *   |        --_______|______--             /
     *      3rd            |            4nd     V
     *   ^                 |
     *    \   <--       +y V              <---
     *     \
     */
    public Point findNextViewCenter(ViewData previousViewData, int nextViewHalfViewWidth, int nextViewHalfViewHeight) {
        if(SHOW_LOGS) Log.v(TAG, ">> findViewCenter, previousViewData " + previousViewData);

        Point previousViewCenter = previousViewData.getCenterPoint();

        Point nextViewCenter;

        boolean foundNextViewCenter;
        do {

            nextViewCenter = getNextViewCenter(previousViewCenter);

            int nextViewTop = nextViewCenter.getY() - nextViewHalfViewHeight;

            int nextViewBottom = nextViewCenter.getY() + nextViewHalfViewHeight;

            int nextViewRight = nextViewCenter.getX() + nextViewHalfViewWidth;

            boolean nextViewTopIsBelowPreviousViewBottom = nextViewTop >= previousViewData.getViewBottom();

            boolean nextViewBottomIsAbovePreviousViewTop = nextViewBottom <= previousViewData.getViewTop();

            boolean nextViewIsToTheLeftOfThePreviousView = nextViewRight <= previousViewData.getViewLeft();

            foundNextViewCenter = nextViewTopIsBelowPreviousViewBottom || nextViewIsToTheLeftOfThePreviousView || nextViewBottomIsAbovePreviousViewTop;

            // "next view center" become previous
            previousViewCenter = nextViewCenter;
        } while (!foundNextViewCenter);

        if(SHOW_LOGS) Log.v(TAG, "<< findViewCenter, foundNextViewCenter " + foundNextViewCenter);
        return nextViewCenter;
    }

    /**
     * We start from previous view center point.
     * Here is the flow :
     *
     * 1. We get an index of previousViewCenter
     * 2. We increment the index.
     * 3. We get next point using index
     *
     */
    private Point getNextViewCenter(Point previousViewCenter) {

        /** 2. */
        int previousViewCenterPointIndex = mCirclePointIndex.get(previousViewCenter);

        /** 3. */
        int nextViewCenterCenterPointIndex = previousViewCenterPointIndex + 1;

        /** 5. */
        Point nextViewCenter = mCircleIndexPoint.get(nextViewCenterCenterPointIndex);

//        if(SHOW_LOGS) Log.v(TAG, "getNextViewCenter, nextViewCenter " + nextViewCenter);
        return nextViewCenter;
    }

    public void reset() {
    }

    public int getViewCenterPointIndex(Point point) {

        int viewCenterPointIndex = mCirclePointIndex.get(point);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, viewCenterPointIndex " + viewCenterPointIndex);

        return viewCenterPointIndex;
    }

    public Point getViewCenterPoint(int newCenterPointIndex) {
        return mCircleIndexPoint.get(
                newCenterPointIndex
        );
    }
}
