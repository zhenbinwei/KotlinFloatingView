package com.example.weizhenbin.floatingview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout

/**
 * Created by weizhenbin on 2018/9/12.
 *
 * 背景放大效果
 */

class FloatingContentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    private var pathRect: Path? = null
    private var pathCircle: Path? = null
    private var paint: Paint? = null

    private var defRadius: Int = 0

    private var circleX = defRadius
    private var circleY = defRadius
    private var circleRadius = defRadius

    private var animator: ValueAnimator? = null


    var animDuration=300L

    init {
        setWillNotDraw(false)
        init()
    }

    private fun init() {
        pathRect = Path()
        pathCircle = Path()
        paint = Paint().apply {
            xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            isAntiAlias = true
        }
    }


    override fun draw(canvas: Canvas) {
        canvas.saveLayer(0f, 0f, this.width.toFloat(), this.height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        super.draw(canvas)
        /**
         * 取路径区域重叠的部分
         */
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        pathRect?.reset()
        pathRect?.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        pathCircle?.reset()
        pathCircle?.addCircle(circleX.toFloat(), circleY.toFloat(), circleRadius.toFloat(), Path.Direction.CW)
        pathRect?.op(pathCircle, Path.Op.XOR)
        canvas.drawPath(pathRect, paint)

    }

    /**
     * 计算半径最大值
     */
    private fun radiusMax(): Int {
        val w = width
        val h = height
        return if (circleX < w / 2 && circleY < h / 2) {
            //第一象限
            Math.sqrt(((w - circleX) * (w - circleX) + (h - circleY) * (h - circleY)).toDouble()).toInt()
        } else if (circleX >= w / 2 && circleY < h / 2) {
            //第二象限
            Math.sqrt((circleX * circleX + (h - circleY) * (h - circleY)).toDouble()).toInt()
        } else if (circleX < w / 2 && circleY >= h / 2) {
            //第三象限
            Math.sqrt(((w - circleX) * (w - circleX) + circleY * circleY).toDouble()).toInt()
        } else {
            //第四象限
            Math.sqrt((circleX * circleX + circleY * circleY).toDouble()).toInt()
        }
    }

    private fun zoom(listener: Animator.AnimatorListener?, vararg values: Int) {

        animator?.run {
            if (isRunning){
                cancel()
            }
            null
        }
        animator = ValueAnimator.ofInt(*values).apply {
            interpolator= LinearInterpolator()
            duration=animDuration
            addUpdateListener {
                animation ->
                val value = animation.animatedValue as Int
                circleRadius = value
                postInvalidate()
            }
            if (listener!=null) {
                addListener(listener)
            }
            start()
        }

    }


    fun setCircleRadius(circleRadius: Int) {
        if (circleRadius == this.circleRadius) {
            return
        }
        this.circleRadius = circleRadius
        //invalidate()
        postInvalidate()
    }

    /**
     * 放大
     */
    fun zoomIn(circleX: Int, circleY: Int, defRadius: Int, listener: Animator.AnimatorListener?) {
        this.circleY = circleY
        this.circleX = circleX
        this.defRadius = defRadius
        zoom(listener, defRadius, radiusMax())
    }

    /**
     * 缩小
     */
    fun zoomOut(circleX: Int, circleY: Int, defRadius: Int, listener: Animator.AnimatorListener?) {
        this.circleY = circleY
        this.circleX = circleX
        this.defRadius = defRadius
        zoom(listener, radiusMax(), defRadius)
    }

    fun zoomOut(listener: Animator.AnimatorListener) {
        zoom(listener, radiusMax(), defRadius)
    }
}
