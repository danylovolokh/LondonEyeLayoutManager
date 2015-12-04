package com.volokh.danylo.layoutmanager.circle_helper.point;

/**
 * Created by danylo.volokh on 11/15/2015.
 */
public class Point {

    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }

    /**
     *  A hashCode like this gives us performance of O(N/4) when using {@link java.util.HashMap}
     *  N - count of points in the circle.
     *
     *  Explanation: we use "Mid point algorithm" for creating circle points.
     *  We create circle points for (x; y) in 1 octant, N/8 points. 2nd octant is created using (y; x), N/8 points.
     *  One quadrant is N/4 points.
     *
     *  We have 8 pair of points combined from every octant
     *  (x; y)      (y; x)
     *  (x; -y)     (-y; x)
     *  (-x; y)     (y; -x)
     *  (-x; -y)    (-y; -x)
     *
     *  Sum of four pairs are equal. Example:
     *  x = 8; y =22;
     *
     *  (30)   =   (30)
     *  (-14)  =   (-14)
     *  (14)   =   (14)
     *  (-30)  =   (-30)
     *
     */
    @Override
    public int hashCode() {
        return x + y;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        }

        if(!(o instanceof Point)){
            return false;
        }
        Point other = (Point) o;
        return other.x == x && other.y == y;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
