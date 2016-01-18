package com.volokh.danylo.layoutmanager.scroller;

import android.view.View;

import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

/**
 * Created by danylo.volokh on 12/9/2015.
 *
 * This scroll handler scrolls every view by the offset that user scrolled with his finger.
 */
public class NaturalScrollHandler extends ScrollHandler {

    private final ScrollHandlerCallback mCallback;

    public NaturalScrollHandler(ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter) {
        super(callback, quadrantHelper, layouter);
        mCallback = callback;
    }

    @Override
    protected void scrollViews(View firstView, int delta) {
        for (int indexOfView = 0; indexOfView < mCallback.getChildCount(); indexOfView++) {
            View view = mCallback.getChildAt(indexOfView);
            scrollSingleViewVerticallyBy(view, delta);
        }
    }
}
