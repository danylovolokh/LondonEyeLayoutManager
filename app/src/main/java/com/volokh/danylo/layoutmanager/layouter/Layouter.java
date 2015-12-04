package com.volokh.danylo.layoutmanager.layouter;

import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;
import com.volokh.danylo.layoutmanager.ViewData;

/**
 * Created by danylo.volokh on 11/17/2015.
 * This is a helper that performs layouting and knows everything about how to layout views
 */
public class Layouter {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = Layouter.class.getSimpleName();

    private final LayouterCallback mCallback;

    private final QuadrantHelper mQuadrantHelper;

    private final int mRadius;

    public Layouter(LayouterCallback callback, int radius, QuadrantHelper quadrantHelper){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = quadrantHelper;
    }

    /**
     *
     *
     *                                               |_____________                         |
     *                                               |             -------_                 V
     *                                               |                     ---__
     *                                               |                          \           |
     *                                               |                           \          V
     *                                               |                            \
     *                                               |                             |        |
     *                                               |             4th             |        V
     *                                               |          Quadrant            \
     *                                               |                      _________|_______________
     *                                               |                     |This is   \ only view    | The center of this view might be in 1st quadrant.
     *                                               |                     |in this    |  quadrant   |
     *                                               |                     |           |             |
     *                                               |                     |           \             |
     *  ---------------------------------------------|*********************|*************************|****->
     *             \          2nd Quadrant            *   1st Quadrant      |           /            | <- We need this view to fill the gap here
     *             |                                 *                   __|___________|____________|
     *             |                                 *                  |Previous      |          |
     *              \                                *                  |Previous     /           |
     *               |                               *                  |View        |            |
     *                \                              *             _____|___________/_____________|_
     *                 |                             *            |                |                |
     *                 |                             *            |Previous        |                |
     *                  \                            *            |View           /                 |
     *                   \                           *            |______________/__________________| <---- previousViewBottomY
     *                    \__                        *                        __/
     *                       ---___                  *                    _---
     *                             -----_____________*_____________-------
     *                                               *               viewTop --->   __________________ _______
     *                                               *                             |   New View       |
     *                                               *                             |                  |       halfViewHeight
     *                                               V                             |       _|_        |_______
     *                                                                             |        |         |
     *                                                                             |    viewCenterY   |       halfViewHeight
     *                                                                             |__________________|_______
     * This method is layout-ing views in 4th, 2nd and 3rd quadrants in that order.
     */
    public ViewData layoutNextView(View view, ViewData previousViewData) {
        if (SHOW_LOGS)Log.v(TAG, ">> layoutNextView, previousViewData " + previousViewData);

        Pair<Integer, Integer> halfWidthHeight = mCallback.getHalfWidthHeightPair(view);

        Point viewCenter = mQuadrantHelper.findNextViewCenter(previousViewData, halfWidthHeight.first, halfWidthHeight.second);
        if (SHOW_LOGS) Log.v(TAG, "layoutNextView, viewCenter " + viewCenter);

        performLayout(view, viewCenter, halfWidthHeight.first, halfWidthHeight.second);
        previousViewData.updateData(view, viewCenter);

        if (SHOW_LOGS) Log.v(TAG, "<< layoutNextView");
        return previousViewData;
    }

    public ViewData layoutViewPreviousView(View view, ViewData previousViewData) {
        if (SHOW_LOGS)Log.v(TAG, ">> layoutViewPreviousView, previousViewData " + previousViewData);

        Pair<Integer, Integer> halfWidthHeight = mCallback.getHalfWidthHeightPair(view);

        Point viewCenter = mQuadrantHelper.findPreviousViewCenter(previousViewData, halfWidthHeight.second);
        if (SHOW_LOGS) Log.v(TAG, "layoutViewPreviousView, viewCenter " + viewCenter);

        performLayout(view, viewCenter, halfWidthHeight.first, halfWidthHeight.second);
        previousViewData.updateData(view, viewCenter);

        if (SHOW_LOGS) Log.v(TAG, "<< layoutViewPreviousView");
        return previousViewData;
    }
    private void performLayout(View view, Point viewCenter, int halfViewWidth, int halfViewHeight) {
        if (SHOW_LOGS) Log.i(TAG, "performLayout, final viewCenter " + viewCenter);

        int left, top, right, bottom;

        top = viewCenter.getY() - halfViewHeight;
        bottom = viewCenter.getY() + halfViewHeight;

        left = viewCenter.getX() - halfViewWidth;
        right = viewCenter.getX() + halfViewWidth;

        mCallback.layoutDecorated(view, left, top, right, bottom);
    }

    /**
     * This method checks if this is last visible layouted view.
     * The return might be used to know if we should stop laying out
     */
    public boolean isLastLayoutedView(View view) {
        boolean isLastLayoutedView;
        int recyclerHeight = mCallback.getHeight();
        if(SHOW_LOGS) Log.v(TAG, "isLastLayoutedView, recyclerHeight " + recyclerHeight);

        if(recyclerHeight > mRadius){
            /** mean that circle is hiding behind the left edge
             *   ___________
             *  |        |  |
             *  |        |  |
             *  |      _/   |
             *  |_____/     |
             *  |           |
             *  |           |
             *  |           |
             *  |___________|
             *
             */
            int spaceToLeftEdge = view.getLeft();
            if(SHOW_LOGS) Log.v(TAG, "isLastLayoutedView, spaceToLeftEdge " + spaceToLeftEdge);
            isLastLayoutedView = spaceToLeftEdge <= 0;

        } else {
            /** mean that circle is hiding behind the bottom edge
             *   ___________
             *  |     \     |
             *  |       \   |
             *  |        |  |
             *  |        |  |
             *  |        |  |
             *  |        |  |
             *  |       /   |
             *  |_____/_____|
             *
             */

            int lastViewBottom = view.getBottom();
            if(SHOW_LOGS) Log.v(TAG, "isLastLayoutedView, lastViewBottom " + lastViewBottom);
            isLastLayoutedView = lastViewBottom >= recyclerHeight;
        }
        if(SHOW_LOGS) Log.v(TAG, "isLastLayoutedView, " + isLastLayoutedView);
        return isLastLayoutedView;
    }
}
