package com.volokh.danylo.layoutmanager.scroller;

import android.util.Log;
import android.view.View;

import com.volokh.danylo.utils.Config;
import com.volokh.danylo.layoutmanager.ViewData;
import com.volokh.danylo.layoutmanager.circle_helper.point.Point;
import com.volokh.danylo.layoutmanager.circle_helper.point.UpdatablePoint;
import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

/**
 * Created by danylo.volokh on 28.11.2015.
 * This scroll handler keeps view in touch when scrolling.
 *  1. Views center is on the circle
 *  2. Views edges are always in touch with each other.
 *
 * Sometimes these requirements are making views "jump" when scroll:
 * If "view B" is below "view A" and views are scrolled down we can reach a point
 * in which "view B" cannot longer stay below "view A" and keep it's center on the circle so,
 * in this case "view B" jumps to the side in order to stay in touch with "view A" side by side and keep it's center on the circle.
 *
 *
 * The logic:
 * 1. Scroll first view by received offset.
 * 2. Calculate position of other views related to first view.
 */
public class PixelPerfectScrollHandler extends ScrollHandler {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = PixelPerfectScrollHandler.class.getSimpleName();

    private final ScrollHandlerCallback mCallback;
    private final QuadrantHelper mQuadrantHelper;
    private final Layouter mLayouter;

    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0, 0);

    PixelPerfectScrollHandler(ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter) {
        super(callback, quadrantHelper, layouter);
        mCallback = callback;
        mQuadrantHelper = quadrantHelper;
        mLayouter = layouter;
    }

    /**
     * 1. Shifts first view by "dy"
     * 2. Shifts all other views relatively to first view.
     */
    @Override
    protected void scrollViews(View firstView, int delta) {
        /**1. */
        Point firstViewNewCenter = scrollSingleViewVerticallyBy(firstView, delta);

        ViewData previousViewData = new ViewData(
                firstView.getTop(),
                firstView.getBottom(),
                firstView.getLeft(),
                firstView.getRight(),
                firstViewNewCenter);

        /**2. */
        for (int indexOfView = 1; indexOfView < mCallback.getChildCount(); indexOfView++) {
            View view = mCallback.getChildAt(indexOfView);
            scrollSingleView(previousViewData, view);
        }
    }

    private void scrollSingleView(ViewData previousViewData, View view) {
        if (SHOW_LOGS) Log.v(TAG, "scrollSingleView, previousViewData " + previousViewData);

        int width = view.getWidth();
        int height = view.getHeight();

        int viewCenterX = view.getRight() - width / 2;
        int viewCenterY = view.getTop() + height / 2;

        SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

        Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);

        Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width / 2, height / 2);

        int dX = newCenterPoint.getX() - oldCenterPoint.getX();
        int dY = newCenterPoint.getY() - oldCenterPoint.getY();

        view.offsetTopAndBottom(dY);
        view.offsetLeftAndRight(dX);

        previousViewData.updateData(view, newCenterPoint);
    }
}
