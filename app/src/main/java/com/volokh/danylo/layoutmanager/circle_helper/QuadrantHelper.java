package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danylo.volokh on 11/17/2015.
 */
public class QuadrantHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = QuadrantHelper.class.getSimpleName();

    private final ArrayList<Point> m_1st_4th_3rd_QuadrantCircumference; // order of quadrants does matter

    private final int mRadius;

    /**
     * This is view center for layouting
     */
    private Point mStartPoint = new Point(0, 0);

    public QuadrantHelper(int radius){

        mRadius = radius;

        m_1st_4th_3rd_QuadrantCircumference = new ArrayList<>(radius * 3/*three quadrants*/);
        fill_1st_4th_3rd_QuadrantCircumference();
    }

    private void fill_1st_4th_3rd_QuadrantCircumference() {
        fillFirstQuadrantCircumference(mRadius, m_1st_4th_3rd_QuadrantCircumference);
        // TODO: + (Radius; 0) ??
        fillFourthQuadrantCircumference(mRadius, m_1st_4th_3rd_QuadrantCircumference);
        // TODO: + (0; Radius) ??
        fillThirdQuadrantCircumference(mRadius, m_1st_4th_3rd_QuadrantCircumference);
        // TODO: + (-Radius; 0) ??
    }

    /**
     *      |
     *      |         x
     *------|---------->
     *      |           The formula is R^2 = x^2 + y^2
     *      |           We are calculating a circle points.
     *      |           Start and y=1 and increment y up to "radius"
     *    y V
     */
    private static void fillFourthQuadrantCircumference(int radius, List<Point> circumference) {
        if(SHOW_LOGS) Log.v(TAG, ">> fillFourthQuadrantCircumference, size " + circumference.size());

        /**
         * Basically in fourth quadrant y is negative. But devices display "thinks" differently so y is positive
         */
        for(int y = 1; y <= radius; y++){
            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantCircumference, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2

            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantCircumference, x " + x);
            circumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFourthQuadrantCircumference");
    }

    private static void fillThirdQuadrantCircumference(int radius, List<Point> circumference) {
        if(SHOW_LOGS) Log.v(TAG, ">> fillThirdQuadrantCircumference, size " + circumference.size());
        for(int y = radius; y > 0; y--){
            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantCircumference, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2

            x = -x; // x is negative in third quadrant
            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantCircumference, x " + x);
            circumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillThirdQuadrantCircumference");
    }

    /**
     *   -y |          The formula is R^2 = x^2 + y^2
     *      |          We are calculating a circle points.
     *      |______    Start at y=-radius and increment y up to "0"
     *      |      --_
     *      |         \_      x
     *      |           |
     *------|--------------->
     *      |
     *      |
     *      |
     *   +y V
     */
    private static void fillFirstQuadrantCircumference(int radius, List<Point> circumference) {
        if(SHOW_LOGS) Log.v(TAG, ">> fillFirstQuadrantCircumference, size " + circumference.size());
        for(int y = -radius; y < 0; y++){
            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantCircumference, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2
            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantCircumference, x " + x);
            circumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFirstQuadrantCircumference");
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

    public int findViewCenterY(int previousViewBottomY, int halfViewHeight, int viewTop) {
        if(SHOW_LOGS) Log.v(TAG, ">> findViewCenterY, previousViewBottomY " + previousViewBottomY);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);

        // When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
        int viewCenterY = previousViewBottomY;

        boolean viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, initial viewTopIsHigherThenPreviousViewBottom " + viewTopIsHigherThenPreviousViewBottom);
        boolean isViewCenterYOnTheCircle;
        do {

            viewCenterY++;

            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, new viewCenterY " + viewCenterY);

            isViewCenterYOnTheCircle = isViewCenterYOnTheFourthQuadrantCircle(viewCenterY);
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, isViewCenterYOnTheCircle " + isViewCenterYOnTheCircle);

            if(isViewCenterYOnTheCircle){
                viewTop = viewCenterY - halfViewHeight;
                if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);

                viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
                if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);

                if(!viewTopIsHigherThenPreviousViewBottom){
                    if(SHOW_LOGS) Log.i(TAG, "findViewCenterY, viewCenterY " + viewCenterY);
                }
            } else {
                if(SHOW_LOGS) {
                    Log.v(TAG, "findViewCenterY, view center Y is no longer on the circle");
                    Log.w(TAG, "findViewCenterY, this view should not be placed below previous view. Return -1");
                }
                viewCenterY = -1;
            }

        } while (viewTopIsHigherThenPreviousViewBottom && isViewCenterYOnTheCircle);

        return viewCenterY;
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

    private boolean isViewCenterYOnTheFourthQuadrantCircle(int viewCenterY) {
        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle, viewCenterY " + viewCenterY);

        int numberOfPointsIn_3rdQuadrant = mRadius;
        Point lastPointInFourthQuadrant = m_1st_4th_3rd_QuadrantCircumference.get(
                m_1st_4th_3rd_QuadrantCircumference.size() - numberOfPointsIn_3rdQuadrant);
        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle, lastPointInFourthQuadrant " + lastPointInFourthQuadrant);

        boolean isViewCenterYOnTheCircle = lastPointInFourthQuadrant.y >= viewCenterY;
        if(SHOW_LOGS) Log.v(TAG, "isViewCenterYOnTheFourthQuadrantCircle " + isViewCenterYOnTheCircle);

        return isViewCenterYOnTheCircle;
    }

    /**
     * View top is higher when it's smaller then previous View Bottom
     */
    private boolean isViewTopHigherThenPreviousViewBottom(int previousViewBottom, int viewTop) {
        return viewTop < previousViewBottom;
    }

    public int getXFromYInFourthQuadrant(int y) {
        if(SHOW_LOGS) Log.v(TAG, ">> getXFromYInFourthQuadrant, y " + y);

        int x = m_1st_4th_3rd_QuadrantCircumference.get(0).x;
        for(int index = 1; index < m_1st_4th_3rd_QuadrantCircumference.size(); index++){
            Point point = m_1st_4th_3rd_QuadrantCircumference.get(index);
            if(SHOW_LOGS) Log.v(TAG, "getXFromYInFourthQuadrant, point " + point);

            if(point.y == y){
                x = point.x;
                mStartPoint = point;
                break;
            }
        }
        if(SHOW_LOGS) Log.v(TAG, "<< getXFromYInFourthQuadrant, x " + x + ", new mStartPoint " + mStartPoint);
        return x;
    }

    public Point getStartPoint() {
        return mStartPoint;
    }

    public void reset() {
        mStartPoint = new Point(0, 0);
    }
}
