package com.volokh.danylo.layoutmanager.layouter;

import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.FourQuadrantHelper;
import com.volokh.danylo.layoutmanager.circle_helper.Point;
import com.volokh.danylo.layoutmanager.ViewData;

/**
 * Created by danylo.volokh on 11/17/2015.
 * This is a helper that performs layouting and knows everything about how to layout views
 */
public class Layouter {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = Layouter.class.getSimpleName();

    private final LayouterCallback mCallback;

    private final FourQuadrantHelper mQuadrantHelper;

    private final int mRadius;

    public Layouter(LayouterCallback callback, int radius, FourQuadrantHelper quadrantHelper){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = quadrantHelper;
    }

    /**
     *
     *
     *                                                We start at this point "previousViewRight"
     *                                                CAUTION: previousViewRight cannot be in 2nd quadrant! Why ?
     *                                                Because only one single view should be in the 1st quadrant.
     *                                                This view is the one that is partially visible from top edge of RecyclerView
     *
     *                                                -> -> -> -> -> -> -> -> -> -> -> ->
     *                                               |____________                         | If we didn't find
     *                                               |            -------_                 V
     *                                               |                    \_
     *                                               |                      ---__          |
     *                                               |                           \__       V
     *                                               |                              \
     *                                               |                              |      |
     *                                               |             1st              |      V
     *                                               |          Quadrant             \
     *                                               |                     ___________|_____________
     *                                               |                    |This is the|only view    | The center of this view might be in 1st quadrant.
     *                                               |                    |in this    |  quadrant   |
     *                                               |                    |            \            |
     *                                               |                    |            |            |
     *  ---------------------------------------------|********************|*************************|****->
     *            \          3rd Quadrant            *   4th Quadrant     |            /            | <- We need this view to fill the gap here
     *            |                                  *                   _|____________|____________|
     *            |                                  *                  |Previous      |          |
     *             \                                 *                  |Previous     /           |
     *              |                                *                  |View        |            |
     *               \                               *             _____|___________/_____________|_
     *                |                              *            |                 |               |
     *                 |                             *            |Previous        |                |
     *                  \                            *            |View           /                 |
     *                   \                           *            |______________/__________________| <---- previousViewBottomY
     *                    \__                        *                        __/
     *                       ---___                  *                  ___---
     *                             -----_____________*_____________-----
     *                                               *               viewTop --->   __________________ _______
     *                                               *                             |   New View       |
     *                                               *                             |                  |       halfViewHeight
     *                                               V                             |       _|_        |_______
     *                                                                             |        |         |
     *                                                                             |    viewCenterY   |       halfViewHeight
     *                                                                             |__________________|_______
     * This method is layout-ing views in 1st, 4th and 3rd quadrants in that order.
     * We start from previous view bottom point and try to find a point on the circumference below that point.
     * TODO : If we don't find a suitable point below we try to find it to the left of previous view left
     *
     */
    public ViewData layoutViewNextView(View view, ViewData previousViewData) {
        if (SHOW_LOGS)Log.v(TAG, ">> layoutViewNextView, previousViewData " + previousViewData);

        Pair<Integer, Integer> halfWidthHeight = mCallback.getHalfWidthHeightPair(view);

        Point viewCenter = mQuadrantHelper.findNextViewCenter(previousViewData, halfWidthHeight.first, halfWidthHeight.second);
        if (SHOW_LOGS) Log.v(TAG, "layoutViewNextView, viewCenter " + viewCenter);

        performLayout(view, viewCenter, halfWidthHeight.first, halfWidthHeight.second);
        previousViewData.updateData(view, viewCenter);

        if (SHOW_LOGS) Log.v(TAG, "<< layoutViewNextView");
        return previousViewData;
    }

    public ViewData layoutViewPreviousView(View view, ViewData previousViewData) {
        if (SHOW_LOGS)Log.v(TAG, ">> layoutViewPreviousView, previousViewData " + previousViewData);

        Pair<Integer, Integer> halfWidthHeight = mCallback.getHalfWidthHeightPair(view);

        Point viewCenter = mQuadrantHelper.findPreviousViewCenter(previousViewData, halfWidthHeight.first, halfWidthHeight.second);
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
