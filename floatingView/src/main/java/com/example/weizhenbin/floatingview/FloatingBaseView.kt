package com.example.weizhenbin.floatingview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout

/**
 * Created by weizhenbin on 18/9/16.
 */

class FloatingBaseView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private var mOnKeyListener: View.OnKeyListener? = null
    var delTextSize: Float = 32f
        set(value) {
            field = value
            paint.textSize = value
        }
    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        textSize=delTextSize
    }

    private var pfilter: PaintFlagsDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    var srcRadius = Tools.dip2px(context, 140.toFloat())

    var delRadius = Tools.dip2px(context, 160.toFloat())


    var delAreaBgColor: Int = resources.getColor(R.color.colorAccent)

    var delTextColor: Int = Color.WHITE


    var delText="移除"


    private val textRect = Rect()
    var isInside = false
        private set
    private var oldState = false

    var showDelArea = false
        set(value) {
            field = value
            postInvalidate()
        }

    init {
        //有背景 canvas才能绘制
        setBackgroundColor(Color.parseColor("#00000000"))
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (mOnKeyListener != null) {
                    mOnKeyListener?.onKey(this, KeyEvent.KEYCODE_BACK, event)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun setOnKeyListener(l: View.OnKeyListener) {
        mOnKeyListener = l

        super.setOnKeyListener(l)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawFilter = pfilter
            if (showDelArea) {
                paint.color = delAreaBgColor
                if (isInside) {
                    drawCircle(width.toFloat(), height.toFloat(), delRadius.toFloat(), paint)
                } else {
                    drawCircle(width.toFloat(), height.toFloat(), srcRadius.toFloat(), paint)
                }
                paint.color = delTextColor
                paint.getTextBounds(delText, 0, delText.length, textRect)
                drawText(delText, width.toFloat() - (srcRadius-textRect.width())/2-textRect.width(), height.toFloat() -(srcRadius-textRect.height())/2, paint)
            }
        }
    }


    fun setLocation(x: Int, y: Int) {
        val scrollRadius = Math.sqrt(Math.pow((width - x).toDouble(), 2.toDouble()) + Math.pow((height - y).toDouble(), 2.toDouble()))
        isInside = scrollRadius < srcRadius
        if (isInside != oldState) {
            postInvalidate()
            oldState = isInside
        }
    }
}
