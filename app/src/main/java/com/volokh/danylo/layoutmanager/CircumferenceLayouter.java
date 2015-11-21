package com.volokh.danylo.layoutmanager;

import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.QuadrantHelper;

/**
 * Created by danylo.volokh on 11/17/2015.
 * This is a helper that performs layouting and knows everything about how to layout views
 */
public class CircumferenceLayouter {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = CircumferenceLayouter.class.getSimpleName();

    private final LayouterCallback mCallback;

    private final QuadrantHelper mQuadrantHelper;

    private final int mRadius;

    public void reset() {
        mQuadrantHelper.reset();
    }

    public CircumferenceLayouter(LayouterCallback callback, int radius){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = new QuadrantHelper(mRadius);
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
     *  ---------------------------------------------|DDDDDDDDDDDDDDDDDDDD|DDDDDDDDDDDDDDDDDDDDDDDDD|DDDD->
     *            \          3rd Quadrant            D   4th Quadrant     |            /            | <- We need this view to fill the gap here
     *            |                                  D                   _|____________|____________|
     *            |                                  D                  |Previous      |          |
     *             \                                 D                  |Previous     /           |
     *              |                                D                  |View        |            |
     *               \                               D             _____|___________/_____________|_
     *                |                              D            |                 |               |
     *                 |                             D            |Previous        |                |
     *                  \                            D            |View           /                 |
     *                   \                           D            |______________/__________________| <---- previousViewBottomY
     *                    \__                        D                        __/
     *                       ---___                  D                  ___---
     *                             -----_____________D_____________-----
     *                                               D               viewTop --->   __________________ _______
     *                                               D                             |   New View       |
     *                                               D                             |                  |       halfViewHeight
     *                                               V                             |       _|_        |_______
     *                                                                             |        |         |
     *                                                                             |    viewCenterY   |       halfViewHeight
     *                                                                             |__________________|_______
     * This method is layout-ing views in 1st, 4th and 3rd quadrants in that order.
     * We start from previous view bottom point and try to find a point on the circumference below that point.
     * TODO : If we don't find a suitable point below we try to find it to the left of previous view left
     *
     */
    public void layoutIn_1st_4th_3rd_Quadrant(View view, ViewCoordinates previousViewCoordinates) {
        if (SHOW_LOGS)
            Log.v(TAG, ">> layoutIn_1st_4th_3rd_Quadrant, previousViewCoordinates " + previousViewCoordinates);

        Pair<Integer, Integer> widthHeight = mCallback.getWidthHeightPair(view);

        int decoratedCapsuleWidth = widthHeight.first;
        int decoratedCapsuleHeight = widthHeight.second;

        if (SHOW_LOGS)
            Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, decoratedCapsuleWidth " + decoratedCapsuleWidth);
        if (SHOW_LOGS)
            Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, decoratedCapsuleHeight " + decoratedCapsuleHeight);

        int viewCenterY = previousViewCoordinates.getPreviousViewBottom();
        if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, initial viewCenterY " + viewCenterY);

        int halfViewHeight = decoratedCapsuleHeight / 2;
        if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, halfViewHeight " + halfViewHeight);

        // viewTop is higher than viewCenterY. And "higher" is up. That's why we subtract halfViewHeight;
        int viewTop = viewCenterY - halfViewHeight;

        if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, initial viewTop " + viewTop);

        viewCenterY = mQuadrantHelper.findViewCenterY(
                previousViewCoordinates.getPreviousViewBottom(),
                halfViewHeight,
                viewTop
        );
        if (SHOW_LOGS) Log.i(TAG, "layoutIn_1st_4th_3rd_Quadrant, viewCenterY " + viewCenterY);

        if(viewCenterY != -1){
            if (SHOW_LOGS) Log.i(TAG, "layoutIn_1st_4th_3rd_Quadrant, final viewCenterY " + viewCenterY);

            int left, top, right, bottom;

            top = viewCenterY - halfViewHeight;
            bottom = viewCenterY + halfViewHeight;

            int halfViewWidth = decoratedCapsuleWidth / 2;
            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, halfViewWidth " + halfViewWidth);

            int viewCenterX = mQuadrantHelper.getXFromYInFourthQuadrant(viewCenterY);
            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, viewCenterX " + viewCenterX);

            left = viewCenterX - halfViewWidth;
            right = viewCenterX + halfViewWidth;

            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, left " + left);
            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, top " + top);
            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, right " + right);
            if (SHOW_LOGS) Log.v(TAG, "layoutIn_1st_4th_3rd_Quadrant, bottom " + bottom);

            mCallback.layoutDecorated(view, left, top, right, bottom);
        } else {
            // TODO: try to find View center X and then Y
        }


        if (SHOW_LOGS) Log.v(TAG, "<< layoutInFourthQuadrant");
    }
}
