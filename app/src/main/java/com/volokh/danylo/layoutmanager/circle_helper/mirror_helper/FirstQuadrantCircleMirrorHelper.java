package com.volokh.danylo.layoutmanager.circle_helper.mirror_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;

import java.util.Map;

/**
 * This class is a helper for {@link com.volokh.danylo.layoutmanager.circle_helper.circle_points_creator.FirstQudrantCirclePointsCreator}
 *
 * It can create a full circle points using points from 1st octant. It is mirroring existing points to the points in other circle sectors.
 *
 */
public class FirstQuadrantCircleMirrorHelper implements CircleMirrorHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = FirstQuadrantCircleMirrorHelper.class.getSimpleName();

    private final int mX0;
    private final int mY0;

    public FirstQuadrantCircleMirrorHelper(int x0, int y0){
        mX0 = x0;
        mY0 = y0;
    }

    enum Action{
        MIRROR_2ND_OCTANT,
        MIRROR_2ND_QUADRANT,
        MIRROR_2ND_SEMICIRCLE
    }

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
    @Override
    public void mirror_2nd_Octant(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
    ) {

        int countOfPointsIn_1st_octant = circlePointIndex.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Octant, countOfPointsIn_1st_octant " + countOfPointsIn_1st_octant);

        for(int pointIndex = countOfPointsIn_1st_octant - 1;
            pointIndex >= 0;
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_OCTANT, pointIndex, circleIndexPoint, circlePointIndex);
        }
    }

    @Override
    public void mirror_2nd_Quadrant(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
    ) {

        int countOfPointsIn_1st_quadrant = circlePointIndex.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Quadrant, countOfPointsIn_1st_quadrant " + countOfPointsIn_1st_quadrant);

        for(int pointIndex = countOfPointsIn_1st_quadrant - 1;
            pointIndex >= 0;
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_QUADRANT, pointIndex, circleIndexPoint, circlePointIndex);
        }
    }

    @Override
    public void mirror_2nd_Semicircle(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
    ) {

        int countOfPointsIn_1st_semicircle = circlePointIndex.size();
        if(SHOW_LOGS) Log.v(TAG, "mirror_2nd_Semicircle, countOfPointsIn_1st_semicircle " + countOfPointsIn_1st_semicircle);

        for(int pointIndex = countOfPointsIn_1st_semicircle - 2; // don't count (-radius, 0) because it already in the list
            pointIndex > 0; // don't count (radius, 0) because it already in the list
            pointIndex-- ){

            createMirroredPoint(Action.MIRROR_2ND_SEMICIRCLE, pointIndex, circleIndexPoint, circlePointIndex);

        }

    }

    private void createMirroredPoint(
            Action action,
            int pointIndex,
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex
    ) {

        Point pointAtIndex = circleIndexPoint.get(pointIndex);

        if(pointAtIndex.getX() != pointAtIndex.getY()){
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

            int index = circleIndexPoint.size();

            circleIndexPoint.put(index, mirroredPoint);
            circlePointIndex.put(mirroredPoint, index);

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
     * Here is the explanation of the implementation.
     * This is how 1st and 2nd octant is drawn in "Mid point circle" algorithm
     *
     * DrawPixel( x + x0,  y + y0); // Octant 1
     * DrawPixel( y + x0,  x + y0); // Octant 2
     *
     * To mirror second point using "firstOctantPoint" we have to know original x and y;
     *
     * Get original x, y from "firstOctantPoint":
     * firstOctant_X = x + x0; -> x = firstOctant_X - x0;
     * firstOctant_Y = y + y0; -> y = firstOctant_Y - y0;
     *
     * Get "secondOctantPoint" from original x, y
     * secondOctant_X = y + x0; -> firstOctant_Y - y0 + x0;
     * secondOctant_Y = x + y0; -> firstOctant_X - x0 + y0;
     */
    private Point mirror_2nd_OctantPoint(Point firstOctantPoint) {
        return new Point(firstOctantPoint.getY() - mY0 + mX0, firstOctantPoint.getX() - mX0 + mY0);
    }

    /**
     * This method takes a single point from 1st octant and mirror it to the 2nd octant
     *
     *  ^ +y                                ^
     *  |                  2nd Quadrant  +y |     1st Quadrant
     *  |                                   |
     *  |                                   |
     *  |                    (x3*; y3*) *<--|---* (x3; y3)
     *  |             (x2*; y2*) *<---------|----------*(x2; y2)
     *  |                                   |
     *  |                                   |
     *  |                                   |
     *  |                                   |
     *  |                                   |
     *  |                                   |
     *  |                                   |
     *  |        (x1*; y1*) *<--------------|---------------* (x1; y1)
     *  |                                   |
     *  |                                   |                                      +x
     *--|-----------------------------------|------------------------------------->
     *
     *   How to get a mirrored point (x1*, y1*) when we have it's mirror (x1, y1)?
     *   x1* = x0 - (x1 - x0) = 2*x0 - x1
     *   y1* = y1
     ************
     *   How to get a mirrored point (x2*, y2*) when we have it's mirror (x2, y2)?
     *   x2* = x0 - (x2 - x0) = 2*x0 - x2
     *   y2* = y2
     ************
     *   How to get a mirrored point (x3*, y3*) when we have it's mirror (x3, y3)?
     *   x3* = x0 - (x3 - x0) = 2*x0 - x3
     *   y3* = y3
     */
    private Point mirror_2nd_QuadrantPoint(Point secondQuadrantPoint) {
        return new Point(-secondQuadrantPoint.getX() + 2 * mX0, secondQuadrantPoint.getY());
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
     *   y1* = y0 - (y1 - y0) = 2 * y0 - y1
     ************
     *   How to get a mirrored point (x2*, y2*) when we have it's mirror (x2, y2)?
     *   x2* = x2
     *   y2* = y0 - (y2 - y0) = 2 * y0 - y2
     ************
     *   How to get a mirrored point (x3*, y3*) when we have it's mirror (x3, y3)?
     *   x3* = x3
     *   y3* = y0 - (y3 - y0) = 2 * y0 - y3
     ************
     *   How to get a mirrored point (x4*, y4*) when we have it's mirror (x4, y4)?
     *   x4* = x4
     *   y4* = y0 - (y3 - y0) = 2 * y0 - y3
     */
    private Point mirror_2nd_SemicirclePoint(Point firstSemicirclePoint) {
        return new Point(firstSemicirclePoint.getX(), -firstSemicirclePoint.getY() + 2 * mY0);
    }

}
