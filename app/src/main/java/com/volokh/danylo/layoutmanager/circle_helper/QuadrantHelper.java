package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;
import com.volokh.danylo.layoutmanager.ViewData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danylo.volokh on 11/17/2015.
 */
public class QuadrantHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = QuadrantHelper.class.getSimpleName();

    /**
     * We use an "xy" coordinate as a key. It will improve performance of getting view coordinates.
     * Example:
     *  Point (1; 1). Key= "11";
     *  Point (0; 11). Key= "011";
     *  Point (-1234; 43). Key= "-123443";
     *  Point (-1; -57). Key= "-1-57";
     *
     *  This way we can have a unique identifier for a Point
     */
    private final Map<String, Point> mCircleMap;

    // TODO: use Sparse Array
    private final Map<Integer, String> mIndexesKeys;

    /**
     * Getting index by key is almost O(1)
     */
    private final Map<String, Integer> mKeysIndexes;

    private final int mRadius;

    private final CirclePointsCreator mCirclePointsCreator;

    public QuadrantHelper(int radius, int x0, int y0){

        mRadius = radius;

        mCircleMap = new HashMap<>();

        mIndexesKeys = new HashMap<>();
        mKeysIndexes = new HashMap<>();

        Log.v(TAG, ">> constructor, start filling sector points");
        long start = System.currentTimeMillis();
        mCirclePointsCreator = new CirclePointsCreator(mRadius, x0, y0);
        mCirclePointsCreator.fillCirclePointsCircle(mCircleMap, mIndexesKeys, mKeysIndexes);
        Log.v(TAG, "<< constructor, finished filling sector points in " + (System.currentTimeMillis() - start));

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

            int nextViewTop = nextViewCenter.y - nextViewHalfViewHeight;

            int nextViewBottom = nextViewCenter.y + nextViewHalfViewHeight;

            int nextViewRight = nextViewCenter.x + nextViewHalfViewWidth;

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

    public Point getPointByKey(String key) {
        if(SHOW_LOGS) Log.v(TAG, "getPointByKey, key [" + key + "]");
        return mCircleMap.get(key);
    }

    /**
     * We start from previous view center point.
     * Here is the flow :
     * 1. We get a key from (x; y)
     * 2. We get an index of this Key in the map of Key-Index {@link #mKeysIndexes}
     * 3. We increment the index.
     * 4. We get a key for incremented index from the list {@link #mIndexesKeys}
     *    New key for incremented list indicates a key of next point on the circle
     * 5. We get this point from the map of Key-Point {@link #mCircleMap}
     * 6. We check if new view will be next to the previous if we put it to the point retrieved in 5th paragraph of this list.
     *
     */
    private Point getNextViewCenter(Point previousViewCenter) {
        /** 1. */
        String previousViewCenterPointKey = CirclePointsCreator.getSectorKey(previousViewCenter.x, previousViewCenter.y);

        /** 2. */
        int previousViewCenterPointIndex = mKeysIndexes.get(previousViewCenterPointKey);

        /** 3. */
        int nextViewCenterCenterPointIndex = previousViewCenterPointIndex + 1;// TODO: index is wrong

        /** 4. */
        String nextViewCenterPointKey = mIndexesKeys.get(nextViewCenterCenterPointIndex);

        /** 5. */
        Point nextViewCenter = mCircleMap.get(nextViewCenterPointKey);
//        if(SHOW_LOGS) Log.v(TAG, "getNextViewCenter, nextViewCenter " + nextViewCenter);
        return nextViewCenter;
    }

    public void reset() {
    }

    public int getViewCenterPointIndex(int viewCenterX, int viewCenterY) {
        /** 1. Get Key*/
        String viewCenterPointKey = CirclePointsCreator.getSectorKey(viewCenterX, viewCenterY);
        if(SHOW_LOGS) Log.v(TAG, "getIndexOfViewCenterPoint, viewCenterPointKey [" + viewCenterPointKey + "]");

        /** 2. Get index using key*/
        Integer viewCenterPointIndex = mKeysIndexes.get(viewCenterPointKey);

        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, viewCenterPointIndex " + viewCenterPointIndex);

        return viewCenterPointIndex;
    }

    public Point getViewCenterPoint(int newCenterPointIndex) {
        return mCircleMap.get(
                mIndexesKeys.get(newCenterPointIndex) // Get key. And get point by key
        );
    }
}
