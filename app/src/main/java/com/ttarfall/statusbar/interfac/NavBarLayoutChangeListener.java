package com.ttarfall.statusbar.interfac;/**
 * Created by ttarfall on 2016/7/18.
 */

import android.view.View;

/**
 * NavigationBar视图Layout改变监听
 * @author ttarfall
 * @date 2016-07-18 09:55
 */
public interface NavBarLayoutChangeListener {
    void onLayoutChange(View navBarView, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom);
}
