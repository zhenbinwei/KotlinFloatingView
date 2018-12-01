package com.example.weizhenbin.kotlinfloatingview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.weizhenbin.floatingview.FloatingWindow

class MainActivity : AppCompatActivity() {

    var floatingWindow:FloatingWindow?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       floatingWindow= FloatingWindow(this)
        floatingWindow?.apply {
            scrollTopZoomIn=false
            setMiniWindowIcon(R.mipmap.ic_launcher_round)
            addRealContentView(View.inflate(this@MainActivity,R.layout.test,null))
        }
    }


    fun openWindow(view:View?){

        floatingWindow?.addFloatingWindow()

    }
    fun closeWindow(view:View?){

        floatingWindow?.removeFloatingWindow()

    }
}
