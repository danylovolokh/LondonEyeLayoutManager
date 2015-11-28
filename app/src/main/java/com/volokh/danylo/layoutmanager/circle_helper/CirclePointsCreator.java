package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;

import java.util.Map;

/**
 * Created by danylo.volokh on 11/22/2015.
 * This class "knows" how to fill sector lists with points
 */
public class CirclePointsCreator {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = CirclePointsCreator.class.getSimpleName();

    private final int mRadius;
    private final int mX0;
    private final int mY0;

    public CirclePointsCreator(int radius, int x0, int y0){
        mRadius = radius;
        mX0 = x0;
        mY0 = y0;
    }

    /**
     * This method is based on "Midpoint circle algorithm."
     *
     *  We use three steps:
     *
     *  1. Create 1 octant of a circle.
     *  2. Mirror the created points for the 2nd octant
     *  At this stage we have points for 1 quadrant of a circle
     *
     *  3. Mirror 2nd quadrant points using points from 1 quadrant
     *  At this stage we have points for 1 semicircle
     *
     *  4. Mirror 2nd semicircle points using points from 1 semicircle
     *
     */
    public void fillCirclePointsCircle(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex)
    {
        if(SHOW_LOGS) Log.v(TAG, ">> fillCirclePointsCircle");

        createFirstOctant(circleIndexPoint, circlePointIndex);

        /** at this stage "circleIndexPoint" and "circlePointIndex" contains only the points from first octant*/
        CircleMirrorHelper.mirror_2nd_Octant(
                circleIndexPoint,
                circlePointIndex);

        /** at this stage "circleIndexPoint" and "circlePointIndex" contains only the points from first quadrant*/
        CircleMirrorHelper.mirror_2nd_Quadrant(
                circleIndexPoint,
                circlePointIndex
        );

        /** at this stage "circleIndexPoint" and "circlePointIndex" contains only the points from first semicircle*/
        CircleMirrorHelper.mirror_2nd_Semicircle(
                circleIndexPoint,
                circlePointIndex
        );

        if(SHOW_LOGS) Log.v(TAG, "<< fillCirclePointsCircle");
    }

    /**
     *
     * This method is based on "Midpoint circle algorithm."
     * It creates a points that are situated in first octant.
     *
     * First point has an index of "0", next is "1" and so on.
     * First point is (radius;0)
     *                     ^         here is the last point of first octant. x == y
     *                  +y |        |      /
     *                     |        |    /
     *               ______|        V  /
     *            _--      |        *
     *         _/          |       / \_  1st Octant    <-_
     *       _|            |     /     |_                   \  Points are created in this direction
     *      |              |   /         |                    |
     *      |              | /           |                    |
     *      ---------------|---------- * --->      V
     *      |              |           ^
     *      |_             |           |
     *        |_           |           |
     *           \         |            here is the first point of first octant (radius;0)
     *             --______|
     *                     |
     *
     *
     *  In our reverse coordinates system it's a bit different
     *
     *                  -y |
     *                     |
     *               ______|
     *            _--      |
     *         _/          |
     *        |            |
     *        |            |
     *        |            |
     *      ---------------|-------------------------------------->      V
     *        |            | \         |            *
     *        |            |   \       | 1st Octant *
     *        |_           |     \    _|            *
     *          \_         |       \_/              *
     *            --_______|         \              *
     *                     |           \            *
     *                     |                        *
     *                     V                        *
     *                  +y *                        *
     *                     *     Device's display   *
     *                     *                        *
     *                     *                        *
     *                     *                        *
     *                     *************************
     */
    private void createFirstOctant(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
    ) {

        int x = mRadius;
        int y = 0;
        int decisionOver2 = 1 - x;   // Decision criterion divided by 2 evaluated at x=r, y=0
        while(y <= x){

            createPoint(x + mX0, y + mY0, circleIndexPoint, circlePointIndex);

            y++;
            if (decisionOver2<=0){
                decisionOver2 += 2 * y + 1;   // Change in decision criterion for y -> y+1
            } else {
                x--;
                decisionOver2 += 2 * (y - x) + 1;   // Change for y -> y+1, x -> x-1
            }
        }
    }

    private void createPoint(
            int x,
            int y,
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
            ) {

//        if(SHOW_LOGS) Log.v(TAG, "createPoint, key[" + key + "]");
        int index = circleIndexPoint.size();
//        if(SHOW_LOGS) Log.v(TAG, "createPoint, index " + index);

        Point point = new Point(x, y);

        circleIndexPoint.put(index, point);
        circlePointIndex.put(point, index);
    }
}
