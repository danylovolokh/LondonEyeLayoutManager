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
        previousViewData.updateData(view, viewCenter);

        if (SHOW_LOGS) Log.v(TAG, "<< layoutView");
        return previousViewData;
    }

    private void performLayout(View view, Point viewCenter, int halfViewWidth, int halfViewHeight) {
        if (SHOW_LOGS) Log.i(TAG, "performLayout, final viewCenter " + viewCenter);

        int left, top, right, bottom;

        top = viewCenter.y - halfViewHeight;
        bottom = viewCenter.y + halfViewHeight;

        left = viewCenter.x - halfViewWidth;
        right = viewCenter.x + halfViewWidth;

        mCallback.layoutDecorated(view, left, top, right, bottom);
    }

    private Point scrollFirstViewVerticallyBy(View view, int indexOffset) {
        if (SHOW_LOGS) Log.v(TAG, ">> scrollFirstViewVerticallyBy, indexOffset " + indexOffset);

        int top = view.getTop();
        int right = view.getRight();
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, top " + top);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, right " + right);

        int width = view.getWidth();
        int height = view.getHeight();
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, width " + width);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, height " + height);
// TODO: thi is view center. No need to get it by index
        int viewCenterX = view.getRight() - view.getWidth()/2;
        int viewCenterY = view.getTop() + view.getHeight()/2;

        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterX " + viewCenterX);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterY " + viewCenterY);

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(viewCenterX, viewCenterY);

        int newCenterPointIndex = centerPointIndex + indexOffset;
        
        Point newCenterPoint = mQuadrantHelper.getViewCenterPoint(newCenterPointIndex);
        if (SHOW_LOGS) Log.v(TAG, "scrollFirstViewVerticallyBy, viewCenterY " + viewCenterY);

        int dx = newCenterPoint.x - viewCenterX;
        int dy = newCenterPoint.y - viewCenterY;

        view.offsetTopAndBottom(dy);
        view.offsetLeftAndRight(dx);

        return newCenterPoint;
    }

    public int scrollVerticallyBy(int dy) {

        View firstView = mCallback.getChildAt(0);
        Point firstViewNewCenter = scrollFirstViewVerticallyBy(firstView, -dy);

        if (SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy, firstViewNewCenter " + firstViewNewCenter);

        ViewData previousViewData = new ViewData(
                firstView.getTop(),
                firstView.getBottom(),
                firstView.getLeft(),
                firstView.getRight(),
                firstViewNewCenter);

        for(int indexOfView = 1; indexOfView < mCallback.getChildCount(); indexOfView++){

            View view = mCallback.getChildAt(indexOfView);

            int right = view.getRight();
            int top = view.getTop();

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy right " + right);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy top " + top);

            int width = view.getWidth();
            int height = view.getHeight();

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy width " + width);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy height " + height);

            // TODO: this is old center point. No need to get it by index
            int viewCenterX = view.getRight() - view.getWidth()/2;
            int viewCenterY = view.getTop() + view.getHeight()/2;

            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterX " + viewCenterX);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy viewCenterY " + viewCenterY);

            int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(viewCenterX, viewCenterY);

            Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy oldCenterPoint " + oldCenterPoint);

            Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width/2, height/2);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy newCenterPoint " + newCenterPoint);

            int dX = newCenterPoint.x - oldCenterPoint.x;
            int dY = newCenterPoint.y - oldCenterPoint.y;
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dX " + dX);
            if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dY " + dY);

            view.offsetTopAndBottom(dY);
            view.offsetLeftAndRight(dX);

            previousViewData.updateData(view, newCenterPoint);
        }

        return 0;
    }
}
