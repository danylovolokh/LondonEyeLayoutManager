package com.volokh.danylo.layoutmanager.scroller;

import android.support.v7.widget.RecyclerView;

import com.volokh.danylo.layoutmanager.circle_helper.quadrant_helper.QuadrantHelper;
import com.volokh.danylo.layoutmanager.layouter.Layouter;

/**
 * Created by danylo.volokh on 28.11.2015.
 * This is an interface which descendants will "know" how to adjust view position using input parameters.
 *
 */
public interface ScrollHandler {

    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler);

    public enum Strategy{
        PIXEL_PERFECT,
        NATURAL
    }

    public static class Factory{

        private Factory(){}

        public static ScrollHandler createScrollHandler(Strategy strategy, ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter){
            ScrollHandler scrollHandler = null;
            switch (strategy){
                case PIXEL_PERFECT:
                    scrollHandler = new PixelPerfectScrollHandler(callback, quadrantHelper, layouter);
                    break;
                case NATURAL:
                    scrollHandler = new NaturalScrollHandler(callback, quadrantHelper, layouter);
                    break;
            }
            return  scrollHandler;
        }
    }
}
