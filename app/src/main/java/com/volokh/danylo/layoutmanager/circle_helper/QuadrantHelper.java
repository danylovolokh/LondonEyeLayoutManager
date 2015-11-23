package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;
import com.volokh.danylo.layoutmanager.ViewData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final Map<String, Point> m_1st_4th_3rd_QuadrantCircularSectorsMap;

    /**
     * Getting key by index is O(1)
     * We could use map for it for consistency with {@link #mKeysIndexes} but we use list for better performance
     */
    private final List<String> mIndexesKeys;

    /**
     * Getting index by key is almost O(1)
     */
    private final Map<String, Integer> mKeysIndexes;

    private final int mRadius;

    /**
     * This is view center for layouting
     */
    private Point mStartPoint = new Point(0, 0);

    public QuadrantHelper(int radius){

        mRadius = radius;

        m_1st_4th_3rd_QuadrantCircularSectorsMap = new HashMap<>();

        mIndexesKeys = new ArrayList<>(radius * 3/*three quadrants*/);
        mKeysIndexes = new HashMap<>();

        /**
         * The order does matter!
         * Indexes of array depends on this order.
         * Probably it won't matter if {@link #mIndexesKeys} was a Map :)
         */
        if(SHOW_LOGS) Log.v(TAG, ">> constructor, start filling sector points");
        SectorPointCreator.fillFirstQuadrantWithSectorPoints(mRadius, m_1st_4th_3rd_QuadrantCircularSectorsMap, mIndexesKeys, mKeysIndexes);
        SectorPointCreator.fillFourthQuadrantWithSectorPoints(mRadius, m_1st_4th_3rd_QuadrantCircularSectorsMap, mIndexesKeys, mKeysIndexes);
        SectorPointCreator.fillThirdQuadrantWithSectorPoints(mRadius, m_1st_4th_3rd_QuadrantCircularSectorsMap, mIndexesKeys, mKeysIndexes);

        if(SHOW_LOGS) Log.v(TAG, "<< constructor, finished filling sector points");


    }



    /**
     *      |
     *------|------------------------------------>
     *      |                                 /
     *      |                   ______________|____
     *      |                  |              |   |
     *      |                  |  View 1     /    |
     *      |                  |            |     |
     *      |             _____|___________/______|
     *      |            |                 ||
     *      |            |Previous        | |
     *      |            |View           /  |
     *      |            |______________/___| <---- previousViewBottomY
     *      |                        __/
     *      |                  ___---
     *      |_____________-----
     *      |               viewTop --->   __________________ _______
     *      |                             |   New View       |
     *      |                             |                  |       halfViewHeight
     *      V                             |       _|_        |_______
     *                                    |        |         |
     *                                    |    viewCenterY   |       halfViewHeight
     *                                    |__________________|_______
     */

//    public int findViewCenterY(int previousViewBottomY, int halfViewHeight, int viewTop) {
//        if(SHOW_LOGS) Log.v(TAG, ">> findViewCenterY, previousViewBottomY " + previousViewBottomY);
//        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);
//
//        // When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
//        int viewCenterY = previousViewBottomY;
//
//        boolean viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);
//        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, initial viewTopIsHigherThenPreviousViewBottom " + viewTopIsHigherThenPreviousViewBottom);
//        boolean isViewCenterYOnTheCircle;
//        do {
//
//            viewCenterY++;
//
//            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, new viewCenterY " + viewCenterY);
//
//            isViewCenterYOnTheCircle = isViewCenterYOnTheFourthQuadrantCircle(viewCenterY);
//            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, isViewCenterYOnTheCircle " + isViewCenterYOnTheCircle);
//
//            if(isViewCenterYOnTheCircle){
//                viewTop = viewCenterY - halfViewHeight;
//                if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);
//
//                viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
//                if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);
//
//                if(!viewTopIsHigherThenPreviousViewBottom){
//                    if(SHOW_LOGS) Log.i(TAG, "findViewCenterY, viewCenterY " + viewCenterY);
//                }
//            } else {
//                if(SHOW_LOGS) {
//                    Log.v(TAG, "findViewCenterY, view center Y is no longer on the circle");
//                    Log.w(TAG, "findViewCenterY, this view should not be placed below previous view. Return -1");
//                }
//                viewCenterY = -1;
//            }
//
//        } while (viewTopIsHigherThenPreviousViewBottom && isViewCenterYOnTheCircle);
//
//        return viewCenterY;
//    }


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
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewHalfViewHeight " + nextViewHalfViewHeight);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewHalfViewWidth " + nextViewHalfViewWidth);

        // We start from previous view bottom. TODO: can be optimized if mHasFixedSize = true;
//        int viewCenterY = previousViewCenter.y + previousViewHalfViewHeight;
        Point previousViewCenter = previousViewData.getCenterPoint();
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, previousViewCenter " + previousViewCenter);


        Point nextViewCenter;

        boolean foundNextViewCenter;
        do {

            nextViewCenter = getNextViewCenter(previousViewCenter);
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewCenter " + nextViewCenter);

            int nextViewTop = nextViewCenter.y - nextViewHalfViewHeight;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewTop " + nextViewTop);

            boolean nextViewTopIsBelowPreviousViewBottom = nextViewTop > previousViewData.getViewBottom();
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewTopIsBelowPreviousViewBottom " + nextViewTopIsBelowPreviousViewBottom);

            int nextViewRight = nextViewCenter.x + nextViewHalfViewWidth;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewRight " + nextViewRight);

            boolean nextViewIsToTheLeftOfThePreviousView = nextViewRight < previousViewData.getViewLeft();
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewTopIsBelowPreviousViewBottom " + nextViewTopIsBelowPreviousViewBottom);

            foundNextViewCenter = nextViewTopIsBelowPreviousViewBottom || nextViewIsToTheLeftOfThePreviousView;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenter, foundNextViewCenter " + foundNextViewCenter);

            // "next view center" become previous
            previousViewCenter = nextViewCenter;
        } while (!foundNextViewCenter);

        if(SHOW_LOGS) Log.v(TAG, "<< findViewCenter, foundNextViewCenter " + foundNextViewCenter);
        return nextViewCenter;
    }

    public Point getPointByKey(String key) {
        if(SHOW_LOGS) Log.v(TAG, "getPointByKey, key [" + key + "]");
        return m_1st_4th_3rd_QuadrantCircularSectorsMap.get(key);
    }

    /**
     * We start from previous view center point.
     * Here is the flow :
     * 1. We get a key from (x; y)
     * 2. We get an index of this Key in the map of Key-Index {@link #mKeysIndexes}
     * 3. We increment the index.
     * 4. We get a key for incremented index from the list {@link #mIndexesKeys}
     *    New key for incremented list indicates a key of next point on the circle
     * 5. We get this point from the map of Key-Point {@link #m_1st_4th_3rd_QuadrantCircularSectorsMap}
     * 6. We check if new view will be next to the previous if we put it to the point retrieved in 5th paragraph of this list.
     *
     */
    private Point getNextViewCenter(Point previousViewCenter) {
        /** 1. */
        String previousViewCenterPointKey = SectorPointCreator.getSectorKey(previousViewCenter.x, previousViewCenter.y);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, previousViewCenterPointKey " + previousViewCenterPointKey);

        /** 2. */
        int previousViewCenterPointIndex = mKeysIndexes.get(previousViewCenterPointKey);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, previousViewCenterPointIndex " + previousViewCenterPointIndex);

        /** 3. */
        int nextViewCenterCenterPointIndex = previousViewCenterPointIndex + 1;
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewCenterCenterPointIndex " + nextViewCenterCenterPointIndex);

        /** 4. */
        String nextViewCenterPointKey = mIndexesKeys.get(nextViewCenterCenterPointIndex);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenter, nextViewCenterPointKey " + nextViewCenterPointKey);

        /** 5. */
        return m_1st_4th_3rd_QuadrantCircularSectorsMap.get(nextViewCenterPointKey);
    }


    /**
     *      |
     *------|-------------------->
     *      |    __________
     *      |   |          |
     *      |   |Previous  _/
     *      |   |View    _/|
     *      |   |_______/__|
     *      |        _/
     *      |      /  Here <---- If new View center Y is still on the circle it will return true
     *      |_____/
     *      |
     *      V
     *
     *      |
     *      |
     *------|-------------------->
     *      |      _________
     *      |     |       _/|
     *      |     |     _/  |
     *      |     |   _/    |
     *      |     | _/      |
     *      |_____|/        |
     *      |     | Previous|
     *      |     |  View   |
     *      |     |_________|
     *      V      Here <---- If new View center Y is no longer on the circle it will return false
     *
     */

//    private boolean isViewCenterYOnTheFourthQuadrantCircle(int viewCenterY) {
//        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle, viewCenterY " + viewCenterY);
//
//        int numberOfPointsIn_3rdQuadrant = mRadius;
//        Point lastPointInFourthQuadrant = m_1st_4th_3rd_QuadrantCircumference.get(
//                m_1st_4th_3rd_QuadrantCircumference.size() - numberOfPointsIn_3rdQuadrant);
//        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle, lastPointInFourthQuadrant " + lastPointInFourthQuadrant);
//
//        boolean isViewCenterYOnTheCircle = lastPointInFourthQuadrant.y >= viewCenterY;
//        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle " + isViewCenterYOnTheCircle);
//
//        return isViewCenterYOnTheCircle;
//    }

    /**
     * View top is higher when it's smaller then previous View Bottom
     */
    private boolean isViewTopHigherThenPreviousViewBottom(int previousViewBottom, int viewTop) {
        return viewTop < previousViewBottom;
    }

//    public int getXFromYInFourthQuadrant(int y) {
//        if(SHOW_LOGS) Log.v(TAG, ">> getXFromYInFourthQuadrant, y " + y);
//
//        int x = m_1st_4th_3rd_QuadrantCircumference.get(0).x;
//        for(int index = 1; index < m_1st_4th_3rd_QuadrantCircumference.size(); index++){
//            Point point = m_1st_4th_3rd_QuadrantCircumference.get(index);
//
////            if(SHOW_LOGS) Log.v(TAG, "getXFromYInFourthQuadrant, point " + point);
//
//            if(point.y == y){
//                x = point.x;
//                mStartPoint = point;
//                break;
//            }
//        }
//        if(SHOW_LOGS) Log.v(TAG, "<< getXFromYInFourthQuadrant, x " + x + ", new mStartPoint " + mStartPoint);
//        return x;
//    }

    public Point getStartPoint() {
        return mStartPoint;
    }

    public void reset() {
        mStartPoint = new Point(0, 0);
    }
}
