package com.example.weizhenbin.floatingview

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by weizhenbin on 2018/11/26.
 */
object Tools {
    private var STATUS_BAR_HEIGHT: Int = 0
    fun getStatusBarHeight(context: Context): Int {
        if (STATUS_BAR_HEIGHT <= 0) {
            try {
                val c = Class.forName("com.android.internal.R\$dimen")
                val obj = c.newInstance()
                val field = c.getField("status_bar_height")
                val x = Integer.parseInt(field.get(obj).toString())
                STATUS_BAR_HEIGHT = context.resources.getDimensionPixelSize(x)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }
        return STATUS_BAR_HEIGHT
    }

    fun screenHeight(context: Context): Int{
        return context.resources.displayMetrics.heightPixels
    }
    fun screenWidth(context:Context): Int{
        return context.resources.displayMetrics.widthPixels
    }

    fun dip2px(context: Context,dpValue: Float): Int {

        val scale = context.resources.displayMetrics.density

        return (dpValue * scale + 0.5f).toInt()

    }

    fun px2dip(context: Context,pxValue: Float): Int {

        val scale = context.resources.displayMetrics.density

        return (pxValue / scale + 0.5f).toInt()

    }

    /**
     * 隐藏软键盘(可用于Activity，Fragment)
     */
    fun hideSoftKeyboard(context: Context, view: View?) {
        if (view == null) return

        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}