package com.tai.dataprocessing;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

class Tools {
    static String TAG = "ToolsTAG";

    /**
     * 设置状态栏字体颜色为黑色.
     *
     * 仅API 23+（android 6.0及以上）有效，在其之下
     * 若需要看到状态栏文字，将会设置状态栏为灰色
     *
     * @param activity 欲设置的Activity
     */
    public static void settingStatusBlackWord(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * 移除状态栏设置透明后的阴影层.
     *
     * ！调用该方法需在setContentView之前
     * 仅API 24+（android 7.0及以上）在设置状态栏透明后会产生阴影层，
     * 故该方法仅对API 24+有效
     *
     * @param activity 欲设置的Activity
     */
    public static void removeStatusShadow(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Class<?> decorViewClass = activity.getWindow().getDecorView().getClass();
                Field field = decorViewClass.getDeclaredField("mSemiTransparentStatusBarColor");
                field.setAccessible(true);
                field.setInt(activity.getWindow().getDecorView(), Color.TRANSPARENT);
            } catch (NoSuchFieldException e) {
                Log.w(TAG, "获得类DecorView的mSemiTransparentStatusBarColor成员的反射器失败");
                //activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            } catch (IllegalAccessException e) {
                Log.w(TAG, "修改类DecorView的mSemiTransparentStatusBarColor成员的值失败");
                //activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    }
}
