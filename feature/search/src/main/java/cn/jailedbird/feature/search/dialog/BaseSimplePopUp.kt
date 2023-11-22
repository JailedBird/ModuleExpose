package cn.jailedbird.feature.search.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.PopupWindow

/**
 * Popup document: [Popup document](https://www.jianshu.com/p/6c32889e6377)
 * */
abstract class BaseSimplePopUp(context: Context) : PopupWindow() {
    companion object {
        fun makeDropDownMeasureSpec(measureSpec: Int): Int {
            val mode: Int = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.UNSPECIFIED
            } else {
                MeasureSpec.EXACTLY
            }
            return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(measureSpec), mode)
        }
    }

    init {
        innerInit(context)
    }

    private fun innerInit(context: Context) {
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        /**
         *  Avoid touch-event transparent to bottom view
         *  [解决PopupWindow点击外部区域消失后事件透传](https://www.jianshu.com/p/6a65107b19a1)
         */
        // isFocusable = true // TODO 这种方式会导致输入框失去焦点 键盘闪烁
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        @SuppressLint("InflateParams")
        val contentView: View = LayoutInflater.from(context).inflate(
            getLayout(), null, false
        )
        setContentView(contentView)
        initView(contentView)
        initEvent(contentView)
    }

    open fun initView(root: View) {

    }

    open fun initEvent(root: View) {

    }

    abstract fun getLayout(): Int
}