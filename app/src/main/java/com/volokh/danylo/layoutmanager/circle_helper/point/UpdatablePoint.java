package com.volokh.danylo.layoutmanager.circle_helper.point;

/**
 * Created by danylo.volokh on 28.11.2015.
 */
public class UpdatablePoint extends Point {

    public UpdatablePoint(int x, int y) {
        super(x, y);
    }

    public void update(int x, int y) {
        setX(x);
        setY(y);
    }
}
