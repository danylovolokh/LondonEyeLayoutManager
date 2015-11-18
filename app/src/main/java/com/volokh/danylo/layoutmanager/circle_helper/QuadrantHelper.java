package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;

import java.util.ArrayList;

/**
 * Created by danylo.volokh on 11/17/2015.
 */
public class QuadrantHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = QuadrantHelper.class.getSimpleName();

    private final ArrayList<Point> mFirstQuadrantCircumference;
    private final ArrayList<Point> mFourthQuadrantCircumference;
    private final ArrayList<Point> mThirdQuadrantCircumference;

    private final int mRadius;

    /**
     * This is view center for layouting
     */
    private Point mStartPoint = new Point(0, 0);

    public QuadrantHelper(int radius){

        mRadius = radius;

        //TODO: calculate in one run to improve performance
        mFirstQuadrantCircumference = new ArrayList<>(radius);
        fillFirstQuadrantCircumference();

        mThirdQuadrantCircumference = new ArrayList<>(radius);
        fillThirdQuadrantCircumference();

        mFourthQuadrantCircumference = new ArrayList<>(radius);
        fillFourthQuadrantCircumference();
    }

    private void fillFourthQuadrantCircumference() {
        if(SHOW_LOGS) Log.v(TAG, ">> fillFourthQuadrantCircumference, size " + mFirstQuadrantCircumference.size());
        if(SHOW_LOGS) Log.v(TAG, ">> fillFourthQuadrantCircumference, size " + mFirstQuadrantCircumference.size());

        /**
         * Basically in fourth quadrant y is negative. but devices display "thinks" differently so y is positive
         */
        for(int y = 1; y <= mRadius; y++){
            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantCircumference, y " + y);

            int x = (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2

            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantCircumference, x " + x);
            mFourthQuadrantCircumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFourthQuadrantCircumference, " + mFirstQuadrantCircumference);
    }

    private void fillThirdQuadrantCircumference() {
        if(SHOW_LOGS) Log.v(TAG, ">> fillThirdQuadrantCircumference, size " + mFirstQuadrantCircumference.size());
        for(int y = mRadius; y > 0; y--){
            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantCircumference, y " + y);

            int x = (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2

            x = -x; // x is negative in third quadrant
            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantCircumference, x " + x);
            mThirdQuadrantCircumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillThirdQuadrantCircumference, " + mFirstQuadrantCircumference);
    }

    private void fillFirstQuadrantCircumference() {
        if(SHOW_LOGS) Log.v(TAG, ">> fillFirstQuadrantCircumference, size " + mFirstQuadrantCircumference.size());
        for(int y = mRadius; y > 0; y--){
            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantCircumference, y " + y);
            int x = (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2
            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantCircumference, x " + x);
            mFirstQuadrantCircumference.add(new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFirstQuadrantCircumference, " + mFirstQuadrantCircumference);
    }

    public int findViewCenterY(int previousViewBottomY, int halfViewHeight, int viewTop) {
        if(SHOW_LOGS) Log.v(TAG, ">> findViewCenterY, previousViewBottomY " + previousViewBottomY);

        /**
         *      |
         *      |
         *------|------
         *      |       / We are in this quadrant and going in this way.
         *      |  /___/
         *         \
         */

        // When we calculate this value for the first time, "view top" is higher than previousViewBottom because it is "container top" and == 0
        int viewCenterY = previousViewBottomY;

        boolean viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);
        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, initial viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);

        do {

            viewCenterY++;

            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, new viewCenterY " + viewCenterY);

            viewTop = viewCenterY - halfViewHeight;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);

            viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottomY, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);

            if(!viewTopIsHigherThenPreviousViewBottom){
                if(SHOW_LOGS) Log.i(TAG, "findViewCenterY, viewCenterY " + viewCenterY);
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

    public int getXFromYInFourthQuadrant(int y) {
        if(SHOW_LOGS) Log.v(TAG, ">> getXFromYInFourthQuadrant, y " + y);

        int x = mFourthQuadrantCircumference.get(0).x;
        for(int index = 1; index < mFourthQuadrantCircumference.size(); index++){
            Point point = mFourthQuadrantCircumference.get(index);
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
