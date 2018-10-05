package com.anwesh.uiprojects.linkedbarvertstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.anwesh.uiprojects.barvertstepview.BarVertStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarVertStepView.create(this)
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}