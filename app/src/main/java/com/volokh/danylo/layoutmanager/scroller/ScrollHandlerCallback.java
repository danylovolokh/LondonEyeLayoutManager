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

    int getDecoratedTop(View view);

    int getPaddingTop();

    int getLastVisiblePosition();

    int getItemCount();

    void removeView(View view);

    void incrementFirstVisiblePosition();

    void incrementLastVisiblePosition();

    void addView(View view);

    void decrementLastVisiblePosition();

    void decrementFirstVisiblePosition();

    void addView(View newFirstView, int position);
}
