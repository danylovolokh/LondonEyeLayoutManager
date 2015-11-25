package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;

import java.util.HashMap;
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

    public static String getSectorKey(int x, int y) {
        return (x > 0 ? "+" : "") + x +
                "" +
                (y > 0 ? "+" : "") + y;
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
     * @param circlePoints - a result map which should be filled with
     *                     key = {@link #getSectorKey(int, int)}
     *                     value = {@link com.volokh.danylo.layoutmanager.Point}
     *
     * @param indexesKeys a map which should be filled with
     *                     key = index of a circle point starting from (radius;0)
     *                     value = a key that can be used to retrieve {@link com.volokh.danylo.layoutmanager.Point} from "circlePoints" map
     *
     * @param keysIndexes a map which should be filled with
     *                    key = a key that can be used to retrieve {@link com.volokh.danylo.layoutmanager.Point} from "circlePoints" map
     *                    value = index of a circle point starting from (radius;0)
     */
    public void fillCirclePointsCircle(
                                Map<String, Point> circlePoints,
                                Map<Integer, String> indexesKeys,
                                Map<String, Integer> keysIndexes)
    {
        if(SHOW_LOGS) Log.v(TAG, ">> fillCirclePointsCircle");

        createFirstOctant(circlePoints, indexesKeys, keysIndexes);

        /** at this stage "circlePoints" contains only the points from first octant*/
        CircleMirrorHelper.mirror_2nd_Octant(
                new HashMap<>(circlePoints), /*copy points of first octant just in case*/
                indexesKeys,
                keysIndexes,
                circlePoints);

        /** at this stage "circlePoints" contains only the points from first quadrant*/
        CircleMirrorHelper.mirror_2nd_Quadrant(
                new HashMap<>(circlePoints), /*copy points of first quadrant just in case*/
                indexesKeys,
                keysIndexes,
                circlePoints
        );

        /** at this stage "circlePoints" contains only the points from first semicircle*/
        CircleMirrorHelper.mirror_2nd_Semicircle(
                new HashMap<>(circlePoints), /*copy points of first semicircle just in case*/
                indexesKeys,
                keysIndexes,
                circlePoints
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
            Map<String, Point> circlePoints,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes) {

        int x = mRadius;
        int y = 0;
        int decisionOver2 = 1 - x;   // Decision criterion divided by 2 evaluated at x=r, y=0
        while(y <= x){

            createPoint(x + mX0, y + mY0, circlePoints, indexesKeys, keysIndexes);

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
            Map<String, Point> circlePoints,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes) {

        String key = getSectorKey(x, y);
        if(SHOW_LOGS) Log.v(TAG, "createPoint, key[" + key + "]");
        int index = indexesKeys.size();
        if(SHOW_LOGS) Log.v(TAG, "createPoint, index " + index);

        indexesKeys.put(index, key);
        keysIndexes.put(key, index);
        circlePoints.put(key, new Point(x, y));
    }
}
