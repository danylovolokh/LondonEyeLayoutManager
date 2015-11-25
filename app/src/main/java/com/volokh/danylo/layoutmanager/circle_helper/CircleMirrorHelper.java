package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a helper for {@link com.volokh.danylo.layoutmanager.circle_helper.CirclePointsCreator}
 *
 * It can create a full circle points using points from 1st octant. It is mirroring existing points to the points in other circle sectors.
 *
 * It is based on "Midpoint circle algorithm"
 *
 */
public class CircleMirrorHelper{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = CircleMirrorHelper.class.getSimpleName();

    enum Action{
        MIRROR_2ND_OCTANT,
        MIRROR_2ND_QUADRANT,
        MIRROR_2ND_SEMICIRCLE,
    }

    private CircleMirrorHelper(){}



    /**
     * This method takes the points from 1st octant and mirror them to the 2nd octant
     *
     *                     ^                  /
     *                  +y | 2nd octant    /
     *                     |             /
     *                     |_____      /
     *                     |     --_ /
     *                     |       / *_  <-- this s the point from where we start
     *                     |     /     |                  \
     *                     |   /       | 1st Octant        | We are going through points in this direction
     *                     | /         |                   V
     *      ---------------|--------------->      V
     *        |            |
     *        |            |
     *        |_           |
     *          \_         |
     *            --_______|
     *                     |
     *                     |
     */
    public static void mirror_2nd_Octant(
            Map<String, Point> firstOctant,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes,
            Map<String, Point> circlePoints
    ) {

        int countOfPointsIn_1st_octant = firstOctant.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Octant, countOfPointsIn_1st_octant " + countOfPointsIn_1st_octant);

        for(int pointIndex = countOfPointsIn_1st_octant - 1;
            pointIndex >= 0;
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_OCTANT, pointIndex, firstOctant, indexesKeys, keysIndexes, circlePoints);
        }
    }

    public static void mirror_2nd_Quadrant(
            HashMap<String, Point> firstQuadrantPoints,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes,
            Map<String, Point> circlePoints) {

        int countOfPointsIn_1st_quadrant = firstQuadrantPoints.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Quadrant, countOfPointsIn_1st_quadrant " + countOfPointsIn_1st_quadrant);

        for(int pointIndex = countOfPointsIn_1st_quadrant - 1;
            pointIndex >= 0;
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_QUADRANT, pointIndex, firstQuadrantPoints, indexesKeys, keysIndexes, circlePoints);
        }
    }

    public static void mirror_2nd_Semicircle(
            HashMap<String, Point> firstSemicircle,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes,
            Map<String, Point> circlePoints) {

        int countOfPointsIn_1st_semicircle = firstSemicircle.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Semicircle, countOfPointsIn_1st_semicircle " + countOfPointsIn_1st_semicircle);

        for(int pointIndex = countOfPointsIn_1st_semicircle - 2; // don't count (-radius, 0) because it already in the list
            pointIndex > 0; // don't count (radius, 0) because it already in the list
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_SEMICIRCLE, pointIndex, firstSemicircle, indexesKeys, keysIndexes, circlePoints);

        }

    }

    public static void createMirroredPoint(
            Action action,
            int pointIndex,
            Map<String, Point> existingPoints,
            Map<Integer, String> indexesKeys,
            Map<String, Integer> keysIndexes,
            Map<String, Point> circlePoints
            ) {

        String keyOfPoint = indexesKeys.get(pointIndex);
        Point pointAtIndex = existingPoints.get(keyOfPoint);

        if(pointAtIndex.x != pointAtIndex.y){
            Point mirroredPoint;
            switch (action){
                case MIRROR_2ND_OCTANT:
                    mirroredPoint = mirror_2nd_OctantPoint(pointAtIndex);
                    break;
                case MIRROR_2ND_QUADRANT:
                    mirroredPoint = mirror_2nd_QuadrantPoint(pointAtIndex);
                    break;
                case MIRROR_2ND_SEMICIRCLE:
                    mirroredPoint = mirror_2nd_SemicirclePoint(pointAtIndex);
                    break;
                default:
                    throw new RuntimeException("Not handled action " + action);
            }

            String key = CirclePointsCreator.getSectorKey(mirroredPoint.x, mirroredPoint.y);
            int index = indexesKeys.size();

            indexesKeys.put(index, key);
            keysIndexes.put(key, index);
            circlePoints.put(key, mirroredPoint);

        } else {
            if(SHOW_LOGS) Log.w(TAG, "createMirroredPoint, this point is already created. Skip it");
        }
    }

    /**
     * This method takes a single point from 1st octant and mirror it to the 2nd octant
     *
     *                     ^
     *                  +y |   2nd octant
     *                     |                           /
     *                     |                         /
     *                     |                       /
     *                     |                     /
     *                     |                   /
     *                     |     * (x1*, y1*)  /
     *                     |         * (x2*, y2*)
     *                     |             /
     *                     |           /    * (x2, y2)
     *                     |         /
     *                     |       /          * (x1, y1)
     *                     |     /
     *                     |   /
     *                     | /                       1st octant
     *                     |------------------------------------->
     *
     *   How to get a mirrored point (x1*, y1*) when we have it's mirror (x1, y1)?
     *   x1 = y1
     *   y1 = x1
     ************
     *   How to get a mirrored point (x2*, y2*) when we have it's mirror (x2, y2)?
     *   x2* = y2
     *   y2* = x2
     *
     */
    private static Point mirror_2nd_OctantPoint(Point firstOctantPoint) {
        return new Point(firstOctantPoint.y, firstOctantPoint.x);
    }

    /**
     * This method takes a single point from 1st octant and mirror it to the 2nd octant
     *
     *                                     ^
     *                    2nd Quadrant  +y |     1st Quadrant
     *                                     |
     *                                     |
     *                      (x3*; y3*) *<--|---* (x3; y3)
     *               (x2*; y2*) *<---------|----------*(x2; y2)
     *                                     |
     *                                     |
     *                                     |
     *                                     |
     *                                     |
     *                                     |
     *                                     |
     *          (x1*; y1*) *<--------------|---------------* (x1; y1)
     *                                     |
     *                                     |                                      +x
     *-------------------------------------|------------------------------------->
     *
     *   How to get a mirrored point (x1*, y1*) when we have it's mirror (x1, y1)?
     *   x1* = -x1
     *   y1* = y1
     ************
     *   How to get a mirrored point (x2*, y2*) when we have it's mirror (x2, y2)?
     *   x2* = -x2
     *   y2* = y2
     ************
     *   How to get a mirrored point (x3*, y3*) when we have it's mirror (x3, y3)?
     *   x3* = -x3
     *   y3* = y3
     */
    private static Point mirror_2nd_QuadrantPoint(Point secondQuadrantPoint) {
        return new Point(-secondQuadrantPoint.x, secondQuadrantPoint.y);
    }

    /**
     * This method takes a single point from 1st octant and mirror it to the 2nd octant
     *
     *                                     ^
     *                    2nd Quadrant  +y |     1st Quadrant
     *                                     |
     *                                     |
     *                      (x3; y3)   *   |
     *        (x4; y4) *               |   |
     *                 |               |   |
     *                 |               |   |
     *                 |               |   |
     *                 |               |   |
     *                 |               |   |                              * (x2; y2)
     *                 |               |   |                              |
     *                 |               |   |                              |
     *                 |               |   |                              |  * (x1; y1)
     *                 |               |   |                              |  |
     *                 |               |   |                              |  |     +x
     *-------------------------------------|------------------------------------->
     *                 |               |   |                              |  |
     *                 |               |   |                              |  V
     *                 |               |   |                              |  * (x1*; y1*)
     *                 |               |   |                              |
     *                 |               |   |                              V
     *                 |               |   |                              * (x2*; y2*)
     *                 |               |   |
     *                 |               |   |
     *                 |               |   |
     *                 V               |   |
     *        (x4; y4) *               V   |
     *                      (x3*; y3*) *   |
     *                                     |
     *                                     |
     *   How to get a mirrored point (x1*, y1*) when we have it's mirror (x1, y1)?
     *   x1* = x1
     *   y1* = -y1
     ************
     *   How to get a mirrored point (x2*, y2*) when we have it's mirror (x2, y2)?
     *   x2* = x2
     *   y2* = -y2
     ************
     *   How to get a mirrored point (x3*, y3*) when we have it's mirror (x3, y3)?
     *   x3* = x3
     *   y3* = -y3
     ************
     *   How to get a mirrored point (x4*, y4*) when we have it's mirror (x4, y4)?
     *   x4* = x4
     *   y4* = -y4
     */
    private static Point mirror_2nd_SemicirclePoint(Point firstSemicirclePoint) {
        return new Point(firstSemicirclePoint.x, -firstSemicirclePoint.y);
    }

}
