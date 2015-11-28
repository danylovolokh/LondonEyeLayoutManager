package com.volokh.danylo.layoutmanager.scroller;

/**
 * Created by danylo.volokh on 28.11.2015.
 * This is an interface which descendants will "know" how to adjust view position using input parameters.
 *
 */
public interface ScrollHandler {
    void scrollVerticallyBy(int dy);
}
