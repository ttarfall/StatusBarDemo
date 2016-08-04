package com.ttarfall.statusbar;/**
 * Created by ttarfall on 2016/8/4.
 */

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.ttarfall.statusbar.base.BaseActivity;
import com.ttarfall.statusbar.interfac.NavBarLayoutChangeListener;
import com.ttarfall.statusbar.interfac.NavBarVisiableChangeListener;

/**
 * 自定义状态栏与顶部状态栏
 * @author ttarfall
 * @date 2016-08-04 11:52
 */
public class StatusNavActivity extends BaseActivity {

    private Toolbar toolbar;
    private View statusBar;
    private View navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_nav);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actinBar = getSupportActionBar();
        actinBar.setDisplayHomeAsUpEnabled(true);
        statusBar = findViewById(R.id.status_bar);
        navigationBar = findViewById(R.id.navigation_bar);

        //根据版本确定是否显示StatusBar状态栏 系统版本大于等于19才显示
        statusBar.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? View.VISIBLE : View.GONE);
        //添加NavigationBar可见性监听
        addNavBarVisiableChangeListener(new NavBarVisiableChangeListener() {
            @Override
            public void onNavBarVisiableChange(View navBarView, int visibility) {
                if (navigationBar != null) {
                    navigationBar.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
                }
            }
        });
        //设置NavigationBar布局监听，这里主要是因为系统的状态栏不同手机高度不同，所以要监听
        setNavBarLayoutChangeListener(new NavBarLayoutChangeListener() {
            @Override
            public void onLayoutChange(View navBarView, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int navHeight = navBarView.getMeasuredHeight();
                int height = navigationBar.getMeasuredHeight();
                if (navHeight > 0 && navHeight != height) {
                    ViewGroup.LayoutParams lp = navigationBar.getLayoutParams();
                    if (lp != null)
                        lp.height = navHeight;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSystemBar(R.color.colorPrimaryDark, 0.0f);
    }
}
