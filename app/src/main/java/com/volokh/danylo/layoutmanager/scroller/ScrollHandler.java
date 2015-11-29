package com.volokh.danylo.layoutmanager.scroller;

import android.support.v7.widget.RecyclerView;

/**
 * Created by danylo.volokh on 28.11.2015.
 * This is an interface which descendants will "know" how to adjust view position using input parameters.
 *
 */
public interface ScrollHandler {
    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler);
}
