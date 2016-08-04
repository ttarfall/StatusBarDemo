package com.ttarfall.statusbar.base;/**
 * Created by ttarfall on 2016/8/3.
 */

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ttarfall.statusbar.R;
import com.ttarfall.statusbar.component.SystemBarTintManager;
import com.ttarfall.statusbar.interfac.NavBarLayoutChangeListener;
import com.ttarfall.statusbar.interfac.NavBarVisiableChangeListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ttarfall
 * @date 2016-08-03 10:19
 */
public abstract class BaseActivity extends AppCompatActivity {

    private View mNavBarTintView;//底部状态栏视图
    private boolean mNavBarAvailable = true;//底部状态栏是否可用
    private int navVisiablity = View.GONE;//底部状态栏的可见性
    protected SystemBarTintManager mTintManager;
    private NavBarLayoutChangeListener navBarLayoutChangeListener;
    private List<NavBarVisiableChangeListener> navBarVisiableChangeListeners;
    private static final int CHECK_NAVSTATUS_WHAT = 1000;
    private Handler navHandler;
    private long histtoryTime;
    private boolean hasTask = false;//是否有任务默认没有任务

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addNavBarVisiableChangeListener(navListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSystemBar(R.color.colorPrimaryDark);
        startNavBarTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseNaBarTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelNaBarTask();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @param color 状态栏颜色
     */
    public void initSystemBar(int color) {
        initSystemBar(color, 1.0f);
    }

    /**
     * @param color
     * @param alpha 状态栏透明度
     */
    public void initSystemBar(int color, float alpha) {
        if (mTintManager == null) {
            mTintManager = new SystemBarTintManager(this);
            setTranslucentStatus(true);
            try {
                Field field = mTintManager.getClass().getDeclaredField("mNavBarAvailable");
                field.setAccessible(true);
                Object obj = field.get(mTintManager);
                mNavBarAvailable = obj instanceof Boolean ? (Boolean) obj : false;
                Field field1 = mTintManager.getClass().getDeclaredField("mNavBarTintView");
                field1.setAccessible(true);
                Object view = field1.get(mTintManager);
                mNavBarTintView = view instanceof View ? (View) view : null;
                if(mNavBarTintView!=null){
                    mNavBarTintView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            if(navBarLayoutChangeListener!=null)
                                navBarLayoutChangeListener.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setNavigationBarTintEnabled(true);
        mTintManager.setStatusBarAlpha(alpha);
        mTintManager.setStatusBarTintColor(getResources().getColor(color));
        mTintManager.setNavigationBarAlpha(mNavBarAvailable && navVisiablity == View.VISIBLE ? 1.0f : 0.0f);
        mTintManager.setNavigationBarTintColor(getResources().getColor(R.color.colorPrimaryDark));//底部状态栏默认颜色
    }

    @TargetApi(17)
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * 是否启动检测任务
     *
     * @return
     */
    private boolean isNavBarTask() {
        return navBarVisiableChangeListeners != null;
    }


    /**
     * 此方法在模拟器还是在真机都是完全正确
     * 检测是否存在NavigationBar
     *
     * @return
     */
    @TargetApi(17)
    private boolean isExistNavigationBar() {
        WindowManager windowManager = getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }
        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;
        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 设置NavigationBar的可见性
     */
    private void startNavBarTask() {
        if (mNavBarAvailable) {
            hasTask = false;
            callNavChangeListeners();
            startCheckNavigationBarStatusTask();
        }
    }

    /**
     * 暂停NaBar检测任务
     */
    private void onPauseNaBarTask() {
        if (navHandler != null) {
            hasTask = true;
            navHandler.removeMessages(CHECK_NAVSTATUS_WHAT);
        }
    }

    /**
     * 取消NaBar检测任务
     */
    private void cancelNaBarTask() {
        if (navHandler != null) {
            hasTask = true;
            navHandler.removeMessages(CHECK_NAVSTATUS_WHAT);
            navHandler = null;
        }
    }

    /**
     * 启动检验NavigationBar任务
     * 每300毫秒检测一次
     */
    @TargetApi(17)
    private void startCheckNavigationBarStatusTask() {
        if (mNavBarAvailable && isNavBarTask()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (navHandler == null)
                    navHandler = new Handler(Looper.myLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case CHECK_NAVSTATUS_WHAT:
                                    startNavBarTask();
                                    break;
                                default:
                                    break;
                            }
                            super.handleMessage(msg);
                        }
                    };

                if (!hasTask) {
                    navHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (navHandler != null) {
                                Message msg = new Message();
                                msg.what = CHECK_NAVSTATUS_WHAT;
                                navHandler.sendMessage(msg);
                            }
                        }
                    }, 300);
                    hasTask = true;
                }
            }
        }
    }

    @TargetApi(17)
    public void addNavBarVisiableChangeListener(NavBarVisiableChangeListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (navBarVisiableChangeListeners == null) {
                navBarVisiableChangeListeners = new ArrayList<NavBarVisiableChangeListener>();
            }
            if (listener != null && !navBarVisiableChangeListeners.contains(listener)) {
                navBarVisiableChangeListeners.add(listener);
            }
        }
    }

    public void setNavBarLayoutChangeListener(NavBarLayoutChangeListener listener){
        navBarLayoutChangeListener = listener;
    }

    private void callNavChangeListeners() {
        if (navBarVisiableChangeListeners != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                int visibility = isExistNavigationBar() ? View.VISIBLE : View.GONE;
                if (visibility != navVisiablity) {
                    navVisiablity = visibility;
                    if (mNavBarTintView != null)
                        mNavBarTintView.setVisibility(navVisiablity);
                    for (NavBarVisiableChangeListener l : navBarVisiableChangeListeners) {
                        l.onNavBarVisiableChange(mNavBarTintView, navVisiablity);
                    }
                }
            } else {
                if (navVisiablity != View.GONE) {
                    navVisiablity = View.GONE;
                    if (mNavBarTintView != null)
                        mNavBarTintView.setVisibility(navVisiablity);
                    for (NavBarVisiableChangeListener l : navBarVisiableChangeListeners) {
                        l.onNavBarVisiableChange(mNavBarTintView, navVisiablity);
                    }
                }
            }
        }
    }

    /**
     * 监听回调
     */
    private NavBarVisiableChangeListener navListener = new NavBarVisiableChangeListener() {
        @Override
        public void onNavBarVisiableChange(View navBarView, int visibility) {
            if (visibility == View.VISIBLE) {
                mTintManager.setNavigationBarAlpha(1.0f);
            } else {
                mTintManager.setNavigationBarAlpha(0.0f);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /**
         * 通过分发事件来检测NavBar状态，这里主要是为了时时相应用户操作隐藏显示底部状态，其实这个地方并不是必须的。
         */
        if (mNavBarAvailable && isNavBarTask() && ev.getAction() == MotionEvent.ACTION_UP) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - histtoryTime > 3000) {
                histtoryTime = currentTime;
                hasTask = false;
                startCheckNavigationBarStatusTask();

            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
