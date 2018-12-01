package com.example.weizhenbin.floatingview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView


/**
 * Created by weizhenbin on 2018/9/12.
 * 悬浮窗
 */

class FloatingWindow(private var context: Context) : View.OnTouchListener {

    private var windowManager: WindowManager? = null
    private var controlLayoutParams: WindowManager.LayoutParams? = null//活动控制层
    private var baseLayoutParams: WindowManager.LayoutParams? = null//基础层 底层
    private val floatingContentView: FloatingContentView
    private val floatingBaseView: FloatingBaseView//基础层


    private val controller: ImageView//控制器 透明 用于接受触摸事件 浮窗位置

    /**
     * 悬浮窗大小
     */
    private var windowMiniWidth = Tools.dip2px(context, 48.toFloat())

    private var windowMiniHeight = Tools.dip2px(context, 48.toFloat())


    /**
     * 按下相对View的坐标
     */
    private var downViewX: Int = 0
    private var downViewY: Int = 0

    /**
     * 按下相对屏幕的坐标
     * */
    private var downScreenX:Int=0
    private var downScreenY:Int=0

    /**
     * 屏幕宽高
     */
    private val screenW: Int
    private val screenH: Int

    /**
     * 状态栏高度
     */
    private val statusBarHeight: Int


    /**
     * 属性动画 用于回弹效果
     */
    private var animator: ValueAnimator? = null

    private var mGestureDetector: GestureDetector? = null

    private var isAddView = false

    /**迷你状态 */
    private var isMini = true


     var iWindowChangeListener: IWindowChangeListener? = null
     var iRemoveListener: IRemoveListener?=null
     var iMiniClickListener: IMiniClickListener?=null
    /**
     * 只作为悬浮球
     * */
     var singleMode=false//

     var scrollTopZoomIn: Boolean = true

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        initWindowManager(context)
        floatingBaseView = FloatingBaseView(context)
        //contentView = LayoutInflater.from(context).inflate(R.layout.floating_window_layout, baseView, false) as ViewGroup
        floatingContentView = FloatingContentView(context).apply {
            isClickable = true
        }
        floatingBaseView.addView(floatingContentView)
        controller = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        Log.d("FloatingWindow", "int$controller")
        screenW = Tools.screenWidth(context)
        screenH = Tools.screenHeight(context)
        statusBarHeight = Tools.getStatusBarHeight(context)
        initEvent()
    }


    /**
     * 添加实际可操作的布局
     */
    fun addRealContentView(view: View?) {
        if (view != null && view.parent == null) {
            this.floatingContentView.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    /**
     * 设置可缩放布局大小
     */
    fun setContentViewLayoutParams(layoutParams: FrameLayout.LayoutParams) {
        floatingContentView.layoutParams = layoutParams
    }

    /**
     * 设置悬浮窗大小
     */
    fun setMiniWindowSize(width: Int, height: Int) {
        controlLayoutParams?.run {
            this.height = height
            this.width = width
        }
        windowMiniHeight = height
        windowMiniWidth = width
        windowManager?.updateViewLayout(controller, controlLayoutParams)
    }

    /**
     * 设置悬浮窗背景
     */
    fun setMiniWindowBackground(background: Drawable) {
        controller.setBackgroundDrawable(background)
    }

    /**
     * 设置悬浮窗图标
     */
    fun setMiniWindowIcon(resId: Int) {
        controller.setImageResource(resId)
    }

    /**
     * 设置悬浮窗图标
     */
    fun setMiniWindowIcon(drawable: Drawable) {
        controller.setImageDrawable(drawable)
    }


    /**
     * 设置移除区域背景颜色
     * */
    fun setDelAreaBgColor(color:Int){
        floatingBaseView.delAreaBgColor=color
    }

    /**
     * 设置移除区域文本颜色
     * */
    fun setDelAreaTextColor(color: Int){
        floatingBaseView.delTextColor=color
    }

    /**
     * 设置移除区域文本字体大小 单位px
      * */
    fun setDelAreaTextSize(size :Float){
        floatingBaseView.delTextSize=size
    }

   /**
    * 设置移除区域文字
    * */
   fun setDelAreaText(text:String){
       floatingBaseView.delText=text
   }

    /**
     * 设置移除区域大小 半径
     * */
    fun setDelAreaRadius(srcRadius:Int,delRadius:Int){
        floatingBaseView.srcRadius=srcRadius
        floatingBaseView.delRadius=delRadius
    }

    /**
     * 设置缩放动画时长
     * */
    fun setZoomDuration(duration:Long){
        floatingContentView.animDuration=duration
    }


    private fun initEvent() {
        controller.apply {
            setOnTouchListener(this@FloatingWindow)
            setOnClickListener {
                iMiniClickListener?.onMiniClick()

                if (singleMode){
                    return@setOnClickListener
                }

                if (!isMini) {
                    zoomOutWindow()
                } else {
                    zoomInWindow()
                }
            }
        }
        floatingBaseView.apply {
            setOnClickListener {
                if (!isMini) {
                    viewZoomOut()
                }
            }
            setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!isMini) {
                        viewZoomOut()
                    }

                    return@OnKeyListener true
                }
                false
            })
        }
    }

    fun zoomInWindow() {
        if (isMini) {
            controlLayoutParams?.apply {

                //回弹到左上角或右上角之后展开
                if (scrollTopZoomIn) {
                    if (y > 0 || x > 0 && x < screenW - windowMiniWidth) {
                        val end = if (x + windowMiniWidth / 2 > screenW / 2) {
                            PointF((screenW - windowMiniWidth).toFloat(), 0f)
                        } else {
                            PointF(0f, 0f)
                        }
                        scroll(PointF(x.toFloat(), y.toFloat()), end, object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                viewZoomIn()
                            }
                        })
                    } else {
                        viewZoomIn()
                    }
                } else {
                    viewZoomIn()
                }
            }

        }
    }

    fun zoomOutWindow() {
        if (!isMini) {
            viewZoomOut()
        }
    }

    /**
     * 扩展动画展开
     */
    private fun viewZoomIn() {

        val animatorListenerAdapter = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                baseLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                controlLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager?.run {
                    updateViewLayout(floatingBaseView, baseLayoutParams)
                    updateViewLayout(controller, controlLayoutParams)
                }
                iWindowChangeListener?.onWindowZoomIn()
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                controller.visibility = View.INVISIBLE
            }
        }
        if (isMini) {
            Log.d("tag", windowMiniWidth.toString())
            isMini = false
            floatingContentView.zoomIn((controlLayoutParams?.x
                    ?: 0) + windowMiniWidth / 2, (controlLayoutParams?.y
                    ?: 0) + windowMiniHeight / 2, windowMiniWidth / 2, animatorListenerAdapter)
        }
    }

    private fun viewZoomOut() {
        Tools.hideSoftKeyboard(context, floatingBaseView)
        if (!isMini) {
            windowManager?.updateViewLayout(controller, controlLayoutParams)
            floatingContentView.zoomOut(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isMini = true
                    baseLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    controlLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    windowManager?.run {
                        updateViewLayout(floatingBaseView, baseLayoutParams)
                        updateViewLayout(controller, controlLayoutParams)
                    }
                    controller.visibility = View.VISIBLE
                    floatingContentView.setCircleRadius(0)
                    iWindowChangeListener?.onWindowZoomOut()
                }
            })
        }
    }


    fun addFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Log.d("FloatingWindow", "没有悬浮窗权限")
                return
            }
        }
        if (!isAddView) {
            isAddView = true
            windowManager?.apply {
                addView(floatingBaseView, baseLayoutParams)
                addView(controller, controlLayoutParams)
            }
        }
    }

    fun removeFloatingWindow() {
        if (isAddView) {
            isAddView = false
            windowManager?.apply {
                removeView(floatingBaseView)
                removeView(controller)
                iRemoveListener?.onRemove()
            }
        }
    }


    private fun initWindowManager(context: Context) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        controlLayoutParams = WindowManager.LayoutParams().apply {
            /**
             * 8.0以上 没有授权会直接闪退 8.0以下部分手机没有授权 home切换到桌面 悬浮窗会消失 系统会提示禁止了弹窗 应用内能提示
             */
            type = if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            width = windowMiniWidth
            height = windowMiniHeight
            gravity = Gravity.TOP or Gravity.START
        }

        baseLayoutParams = WindowManager.LayoutParams().apply {
            /**
             * 8.0以上 没有授权会直接闪退 8.0以下部分手机没有授权 home切换到桌面 悬浮窗会消失 系统会提示禁止了弹窗 应用内能提示
             */
            type = if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            //基础层初始状态不接收触摸事件
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP or Gravity.START
        }


    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        if (mGestureDetector == null) {
            mGestureDetector = GestureDetector(v.context, GestureListener(v))
        }
        //长按 点击和onTouch 冲突问题  借助GestureDetector来解决
        mGestureDetector!!.onTouchEvent(event)
        if (!isMini) {
            return true
        }

        controlLayoutParams?.run {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downViewX = event.x.toInt()
                    downViewY = event.y.toInt()
                    downScreenX=event.rawX.toInt()
                    downScreenY=event.rawY.toInt()
                    floatingContentView.setCircleRadius(0)
                    windowManager?.updateViewLayout(controller, this)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    y = event.rawY.toInt() - statusBarHeight - downViewY
                    x = event.rawX.toInt() - downViewX
                    windowManager?.updateViewLayout(controller, this)
                    floatingBaseView.setLocation(event.rawX.toInt(), event.rawY.toInt() - statusBarHeight)
                    if ((Math.abs(event.rawX-downScreenX) > touchSlop) or (Math.abs(event.rawY-downScreenY) > touchSlop)) {
                        if (!floatingBaseView.showDelArea) {
                            floatingBaseView.showDelArea = true
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    floatingBaseView.showDelArea = false
                    if (floatingBaseView.isInside) {
                        x = 0
                        y = 0
                        floatingBaseView.setLocation(x, y)
                        removeFloatingWindow()
                        return true
                    }

                    val w = width.toFloat()
                    val h = height.toFloat()
                    if (x < 0) {
                        x = 0
                    } else if (x > screenW - width) {
                        x = screenW - width
                    }
                    if (y < 0) {
                        y = 0
                    } else if (y > screenH - statusBarHeight - height) {
                        y = screenH - statusBarHeight - height
                    }

                    val centerX = x + w / 2  //用中心点来决定位置
                    val centerY = y + h / 2

                    if (centerX >= screenW / 2 && y <= (screenH + statusBarHeight) / 2) {
                        //第一象限
                        if (screenW - centerX > centerY) {
                            scroll(PointF(x.toFloat(), y.toFloat()), PointF(x.toFloat(), 0f))
                        } else {
                            scroll(PointF(x.toFloat(), y.toFloat()), PointF((screenW - width).toFloat(), y.toFloat()))
                        }
                    } else if (centerX < screenW / 2 && centerX < (screenH + statusBarHeight) / 2) {
                        //第二象限
                        if (centerX > centerY) {
                            scroll(PointF(x.toFloat(), y.toFloat()), PointF(x.toFloat(), 0f))
                        } else {
                            scroll(PointF(x.toFloat(), y.toFloat()), PointF(0f, y.toFloat()))
                        }
                    } else

                        if (centerX <= screenW / 2 && centerY >= (screenH + statusBarHeight) / 2) {
                            //第三象限
                            if (centerX > screenH - centerY) {
                                scroll(PointF(x.toFloat(), y.toFloat()), PointF(x.toFloat(), (screenH - statusBarHeight - height).toFloat()))
                            } else {
                                scroll(PointF(x.toFloat(), y.toFloat()), PointF(0f, y.toFloat()))
                            }
                        } else if (centerX > screenW / 2 && centerY > (screenH + statusBarHeight) / 2) {
                            //第四象限
                            if (screenW - centerX > screenH - centerY) {
                                scroll(PointF(x.toFloat(), y.toFloat()), PointF(x.toFloat(), (screenH - statusBarHeight - height).toFloat()))
                            } else {
                                scroll(PointF(x.toFloat(), y.toFloat()), PointF((screenW - width).toFloat(), y.toFloat()))
                            }
                        }
                }
            }
        }

        return false
    }


    private inner class PointTypeEvaluator : TypeEvaluator<PointF> {

        override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
            startValue.x = startValue.x + fraction * (endValue.x - startValue.x)
            startValue.y = startValue.y + fraction * (endValue.y - startValue.y)
            return startValue
        }
    }

    /**
     * 使用属性动画 实现缓慢回弹效果
     */
    private fun scroll(start: PointF, end: PointF, listener: Animator.AnimatorListener? = null) {
        animator?.run {
            if (isRunning) {
                cancel()
            }
            null
        }
        animator = ValueAnimator.ofObject(PointTypeEvaluator(), start, end).apply {
            duration = 300
            addUpdateListener {
                val point = animatedValue as PointF
                controlLayoutParams!!.x = point.x.toInt()
                controlLayoutParams!!.y = point.y.toInt()
                windowManager?.updateViewLayout(controller, controlLayoutParams)
            }
            if (listener != null) {
                addListener(listener)
            }
            start()
        }
    }

    private inner class GestureListener(private val view: View?) : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.d("FloatingWindow", "onSingleTapConfirmed")
            return view?.performClick() ?: false
        }

        override fun onLongPress(e: MotionEvent) {
            view?.performLongClick()
        }
    }



    interface IWindowChangeListener {
        fun onWindowZoomIn()
        fun onWindowZoomOut()
    }

    interface IRemoveListener{
        fun onRemove()
    }

    interface IMiniClickListener{
        fun onMiniClick()
    }
}
