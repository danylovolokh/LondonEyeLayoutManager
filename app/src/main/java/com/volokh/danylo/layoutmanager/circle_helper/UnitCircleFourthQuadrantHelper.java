package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by danylo.volokh on 11/4/2015.
 */
public class UnitCircleFourthQuadrantHelper {

    private static final int ANGLE_DELTA = 1;

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    private static final String TAG = UnitCircleFourthQuadrantHelper.class.getSimpleName();

    private int mOriginY;
    private int mOriginX;

    private final int mRadius;

    private interface Circle{
        int FIRST_QUADRANT = 1;
        int SECOND_QUADRANT = 2;
        int THIRD_QUADRANT = 3;
        int FOURTH_QUADRANT = 4;
    }

    public UnitCircleFourthQuadrantHelper(int radius) {
        mRadius = radius;
    }

    public int findViewCenterY(int previousViewBottom, int halfViewHeight, int viewTop, AtomicInteger fourthQuadrantLastAngle) {
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
        if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, initial viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);

        // while current "view top" didn't reach the bottom of previous view we decrease the angle and calculate the "top of view"
        do {
            fourthQuadrantLastAngle.set(fourthQuadrantLastAngle.get() - ANGLE_DELTA);
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, new decreased fourthQuadrantLastAngle " + fourthQuadrantLastAngle);
            double sine = sineInQuadrant(fourthQuadrantLastAngle.get(), 4);
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, sine " + sine);

            viewCenterY = (int) (mOriginY + sine * mRadius);
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, new viewCenterY " + viewCenterY);

            viewTop = viewCenterY + (halfViewHeight * getQuadrantSinMultiplier(4));
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTop " + viewTop);

            viewTopIsHigherThenPreviousViewBottom = isViewTopHigherThenPreviousViewBottom(previousViewBottom, viewTop);// && angleDegree < 360 - ANGLE_DELTA /*360 degrees*/;
            if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, viewTopIsNotAtTheContainerTop " + viewTopIsHigherThenPreviousViewBottom);
            if(fourthQuadrantLastAngle.get() < 270){
                if(SHOW_LOGS) Log.v(TAG, "findViewCenterY, mFourthQuadrantLastAngle " + fourthQuadrantLastAngle + ", break");

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
