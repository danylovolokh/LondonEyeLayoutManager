package com.volokh.danylo.layoutmanager.scroller;

import android.support.v7.widget.RecyclerView;

import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

/**
 * Created by danylo.volokh on 12/9/2015.
 *
 * This scroll handler scrolls every view by the offset that user scrolled with his finger.
 */
public class NaturalScrollHandler implements IScrollHandler {

    public NaturalScrollHandler(ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter) {

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler) {
        return 0;
    }
}
