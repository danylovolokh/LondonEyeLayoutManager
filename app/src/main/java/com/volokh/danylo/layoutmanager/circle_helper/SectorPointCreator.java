package com.volokh.danylo.layoutmanager.circle_helper;

import android.util.Log;

import com.volokh.danylo.Config;
import com.volokh.danylo.layoutmanager.Point;

import java.util.List;
import java.util.Map;

/**
 * Created by danylo.volokh on 11/22/2015.
 * This class "knows" how to fill sector lists with points
 */
public class SectorPointCreator {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = SectorPointCreator.class.getSimpleName();

    private SectorPointCreator(){}

    /**
     *   -y |          The formula is R^2 = x^2 + y^2
     *      |          We are calculating a circle points.
     *      |______    Start at y=-radius and increment y up to "0"
     *      |      --_
     *      |         \_      x
     *      |           |
     *------|--------------->
     *      |
     *      |
     *      |
     *   +y V
     */
    public static void fillFirstQuadrantWithSectorPoints(
            int radius,
            Map<String, Point> sector,
            List<String> indexesKeys,
            Map<String, Integer> keysIndexes) {

        if(SHOW_LOGS) Log.v(TAG, ">> fillFirstQuadrantWithSectorPoints, size " + sector.size());


        for(int y = -radius; y <= 0; y++){
//            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantWithSectorPoints, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2
//            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantWithSectorPoints, x " + x);

            String key = getSectorKey(x, y);
            if(SHOW_LOGS) Log.v(TAG, "fillFirstQuadrantWithSectorPoints, key[" + key + "]");

            indexesKeys.add(key);
            keysIndexes.put(key, indexesKeys.size() - 1);
            sector.put(key, new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFirstQuadrantWithSectorPoints");
    }

    /**
     *      |
     *      |         x
     *------|---------->
     *      |           The formula is R^2 = x^2 + y^2
     *      |           We are calculating a circle points.
     *      |           Start and y=1 and increment y up to "radius"
     *    y V
     */
    public static void fillFourthQuadrantWithSectorPoints(
            int radius,
            Map<String, Point> sector,
            List<String> indexesKeys,
            Map<String, Integer> keysIndexes) {

        if(SHOW_LOGS) Log.v(TAG, ">> fillFourthQuadrantWithSectorPoints, size " + sector.size());

        /**
         * Basically in fourth quadrant y is negative. But devices display "thinks" differently so y is positive
         */
        for(int y = 1; y <= radius; y++){
//            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantWithSectorPoints, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2
//            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantWithSectorPoints, x " + x);

            String key = getSectorKey(x, y);
            if(SHOW_LOGS) Log.v(TAG, "fillFourthQuadrantWithSectorPoints, key[" + key + "]");

            indexesKeys.add(key);
            keysIndexes.put(key, indexesKeys.size() - 1);
            sector.put(key, new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillFourthQuadrantWithSectorPoints");
    }

    public static void fillThirdQuadrantWithSectorPoints(
            int radius,
            Map<String, Point> sector,
            List<String> indexesKeys,
            Map<String, Integer> keysIndexes) {

        if(SHOW_LOGS) Log.v(TAG, ">> fillThirdQuadrantWithSectorPoints, size " + sector.size());
        for(int y = radius; y >= 0; y--){
//            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantWithSectorPoints, y " + y);

            int x = (int) Math.sqrt(Math.pow(radius, 2) - Math.pow(y, 2)); // r^2 = x^2 + y^2

            x = -x; // x is negative in third quadrant
//            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantWithSectorPoints, x " + x);

            String key = getSectorKey(x, y);
            if(SHOW_LOGS) Log.v(TAG, "fillThirdQuadrantWithSectorPoints, key[" + key + "]");

            indexesKeys.add(key);
            keysIndexes.put(key, indexesKeys.size() - 1);
            sector.put(key, new Point(x, y));
        }
        if(SHOW_LOGS) Log.v(TAG, "<< fillThirdQuadrantWithSectorPoints");
    }

    public static String getSectorKey(int x, int y) {
        return String.format("%s%s", x, y);
    }
}
