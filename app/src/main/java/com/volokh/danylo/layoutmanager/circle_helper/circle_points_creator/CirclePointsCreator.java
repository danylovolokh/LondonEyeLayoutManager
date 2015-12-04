package com.volokh.danylo.layoutmanager.circle_helper.circle_points_creator;

import com.volokh.danylo.layoutmanager.circle_helper.point.Point;

import java.util.Map;

/**
 * Created by danylo.volokh on 12/4/2015.
 *
 * Implementation should be quadrant specific and it should "know" in which order point should be created.
 */
public interface CirclePointsCreator {

    void fillCirclePoints(
            Map<Integer, Point> circleIndexPoint,
            Map<Point, Integer> circlePointIndex);
}
