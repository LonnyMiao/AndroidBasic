package com.intretech.statusbar

import android.view.WindowManager
import android.os.Build
import android.app.Activity
import android.graphics.Color
import android.view.View
import com.readystatesoftware.systembartint.SystemBarTintManager
import android.annotation.TargetApi

object StatusBarUtil {
    /**
     * 通过设置全屏，设置状态栏透明
     *
     * @param activity
     */
    fun fullScreen(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                val decorView = window.decorView
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                val option =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = option
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                //设置状态栏为透明，否则在部分手机上会呈现系统默认的浅灰色
                window.statusBarColor = Color.TRANSPARENT
                //导航栏颜色也可以考虑设置为透明色
                //window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                val window = activity.window
                val attributes = window.attributes
                val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                val flagTranslucentNavigation =
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                attributes.flags = attributes.flags or flagTranslucentStatus
                //                attributes.flags |= flagTranslucentNavigation;
                window.attributes = attributes
            }
        }
    }

    @TargetApi(19)
    fun transparencyBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }
    /**
     * 修改状态栏颜色，支持4.4以上版本
     * @param activity
     * @param colorId
     */
     fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(colorId)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            transparencyBar(activity)
            val tintManager = SystemBarTintManager(activity)
            tintManager.isStatusBarTintEnabled = true
            tintManager.setStatusBarTintResource(colorId)
        }
    }


    /**
     * 修改状态栏文字颜色，这里小米，魅族区别对待。
     * @dark true:黑色,false:白色
     */
    fun setLightStatusBar(activity: Activity, dark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            when (RomUtils.getLightStatusBarAvailableRomType()) {
                RomUtils.MIUI -> MIUISetStatusBarLightMode(activity, dark)

                RomUtils.FLYME -> setFlymeLightStatusBar(activity, dark)

                RomUtils.ANDROID_NATIVE -> setAndroidNativeLightStatusBar(
                    activity,
                    dark
                )
            }
        }
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        val decor = activity.window.decorView
        if (dark) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    fun MIUISetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !RomUtils.isMiUIV6OrAbove()) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    }
                }
            } catch (e: Exception) {

            }

        }
        return result
    }

    private fun setFlymeLightStatusBar(activity: Activity?, dark: Boolean): Boolean {
        var result = false
        if (activity != null) {
            try {
                val lp = activity.window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                activity.window.attributes = lp
                result = true
            } catch (e: Exception) {
            }

        }
        return result
    }
}