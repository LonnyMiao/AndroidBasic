package com.intretech.statusbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setLightStatusBar(this,true)
        StatusBarUtil.setStatusBarColor(this,R.color.white)
    }
}
