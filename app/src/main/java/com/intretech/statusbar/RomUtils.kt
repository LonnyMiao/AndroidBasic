package com.intretech.statusbar

import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.util.*


object RomUtils {
    val MIUI = 1
    val FLYME = 2
    val ANDROID_NATIVE = 3
    val NA = 4

    fun getLightStatusBarAvailableRomType(): Int {
        //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错
        if (!isMiUIV6OrAbove()) {
            return ANDROID_NATIVE
        }

        if (isMiUIV6OrAbove()) {
            return MIUI
        }

        if (isFlymeV4OrAbove()) {
            return FLYME
        }

        return if (isAndroidMOrAbove()) {
            ANDROID_NATIVE
        } else NA

    }
    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private fun isFlymeV4OrAbove(): Boolean {
        val displayId = Build.DISPLAY
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            val displayIdArray =
                displayId.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (temp in displayIdArray) {
                //版本号4以上，形如4.x.
                if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*".toRegex())) {
                    return true
                }
            }
        }
        return false
    }

    //Android Api 23以上
    private fun isAndroidMOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"

    fun isMiUIV6OrAbove(): Boolean {
        try {
            val properties = Properties()
            properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
            val uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null)
            if (uiCode != null) {
                val code = Integer.parseInt(uiCode!!)
                return code in 4..6
            } else {
                return false
            }

        } catch (e: Exception) {
            return false
        }

    }
}