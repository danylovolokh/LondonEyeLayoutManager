package com.volokh.danylo.layoutmanager;

import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.circle_helper.CirclePointsCreator;
import com.volokh.danylo.layoutmanager.circle_helper.QuadrantHelper;

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

    public void reset() {
        mQuadrantHelper.reset();
    }

    public Layouter(LayouterCallback callback, int radius, int x0, int y0){
        mCallback = callback;
        mRadius = radius;
        mQuadrantHelper = new QuadrantHelper(mRadius, x0, y0);
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
    public ViewData layoutView(View view, ViewData previousViewData) {
        if (SHOW_LOGS)Log.v(TAG, ">> layoutView, previousViewData " + previousViewData);

        if(previousViewData == null){
            previousViewData = new ViewData(0, 0, 0, 0,
                    mQuadrantHelper.getPointByKey(
                            CirclePointsCreator.getSectorKey(mRadius, 0)
                    )
            );
        }
        Pair<Integer, Integer> halfWidthHeight = mCallback.getHalfWidthHeightPair(view);

        Point viewCenter = mQuadrantHelper.findNextViewCenter(previousViewData, halfWidthHeight.first, halfWidthHeight.second);
        if (SHOW_LOGS) Log.v(TAG, "layoutView, viewCenter " + viewCenter);

        performLayout(view, viewCenter, halfWidthHeight.first, halfWidthHeight.second);
        if (SHOW_LOGS) Log.v(TAG, "<< layoutView");
        previousViewData.updateData(view, viewCenter);

        return previousViewData;
    }

    private void performLayout(View view, Point viewCenter, int halfViewWidth, int halfViewHeight) {
        if (SHOW_LOGS) Log.i(TAG, "layoutView, final viewCenter " + viewCenter);

        int left, top, right, bottom;

        top = viewCenter.y - halfViewHeight;
        bottom = viewCenter.y + halfViewHeight;

        left = viewCenter.x - halfViewWidth;
        right = viewCenter.x + halfViewWidth;

        if (SHOW_LOGS) Log.v(TAG, "layoutView, left " + left);
        if (SHOW_LOGS) Log.v(TAG, "layoutView, top " + top);
        if (SHOW_LOGS) Log.v(TAG, "layoutView, right " + right);
        if (SHOW_LOGS) Log.v(TAG, "layoutView, bottom " + bottom);

        mCallback.layoutDecorated(view, left, top, right, bottom);
    }

    public void scrollVerticallyBy(View view, int indexOffset, int indexOfView) {
        if (SHOW_LOGS) Log.v(TAG, ">> scrollVerticallyBy, indexOffset " + indexOffset);

        int right = view.getRight();
        int width = view.getWidth();

        int top = view.getTop();
        int height = view.getHeight();

        int viewCenterX = right - width/2;
        int viewCenterY = top + height/2;

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(viewCenterX, viewCenterY);

        int newCenterPointIndex = centerPointIndex + indexOffset;
        
        Point newCenterPoint = mQuadrantHelper.getViewCenterPoint(newCenterPointIndex);
        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, viewCenterY " + viewCenterY);

        if(newCenterPoint == null){
            throw new RuntimeException("newCenterPointIndex " + newCenterPointIndex);
        }

        int dx = newCenterPoint.x - viewCenterX;
        int dy = newCenterPoint.y - viewCenterY;

        if(dy != indexOffset){
            // todo remove
            Log.e(TAG, "founded dy " + dy + ", indexOffset " +indexOffset);
        }

        view.offsetTopAndBottom(dy);
        view.offsetLeftAndRight(dx);

        int new_right = view.getRight();
        int new_width = view.getWidth();

        int new_top = view.getTop();
        int new_height = view.getHeight();
        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, viewCenterY " + viewCenterY);

    }
}
