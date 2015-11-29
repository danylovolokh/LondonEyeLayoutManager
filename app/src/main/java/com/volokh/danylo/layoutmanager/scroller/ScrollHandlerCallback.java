package com.volokh.danylo.layoutmanager.scroller;

import android.view.View;

/**
 * Created by danylo.volokh on 28.11.2015.
 */
public interface ScrollHandlerCallback {
    int getChildCount();

    View getChildAt(int index);

    int getHeight();

    int getFirstVisiblePosition();
}
