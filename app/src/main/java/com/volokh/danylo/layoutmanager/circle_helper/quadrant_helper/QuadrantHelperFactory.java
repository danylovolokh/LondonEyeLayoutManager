package com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.QuadrantCalculator;

/**
 * Created by danylo.volokh on 12/8/2015.
 * This factory creates one of 4 quadrant helpers
 */
public class QuadrantHelperFactory {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = QuadrantHelperFactory.class.getSimpleName();

    private final static int FIRST_QUADRANT = 1;
    private final static int SECOND_QUADRANT = 2;
    private final static int THIRD_QUADRANT = 3;
    private final static int FOURTH_QUADRANT = 4;

    public static QuadrantHelper createQuadrantHelper(int radius, int xOrigin, int yOrigin) {
        int quadrant = QuadrantCalculator.getQuadrant(radius, xOrigin, yOrigin);

        if(SHOW_LOGS) Log.v(TAG, ">> createQuadrantHelper, quadrant " + quadrant);
        QuadrantHelper quadrantHelper;
        switch (quadrant){
            case FIRST_QUADRANT:
                quadrantHelper = new FirstQuadrantHelper(radius, xOrigin, yOrigin);
                break;
            case SECOND_QUADRANT:
            case THIRD_QUADRANT:
            case FOURTH_QUADRANT:
                throw new RuntimeException("QuadrantHelper is not implemented for quadrant " + quadrant);

            default:
                throw new RuntimeException("impossible value " + quadrant + ", circle can have 1,2,3 or 4 quadrant");

        }
        if(SHOW_LOGS) Log.v(TAG, "<< createQuadrantHelper, quadrantHelper " + quadrantHelper);
        return quadrantHelper;
    }
}
