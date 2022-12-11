package cn.jailedbird.smartappsearch.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

/**
 * Popup document: [Popup document](https://www.jianshu.com/p/6c32889e6377)
 * */
abstract class BaseSimplePopUp(context: Context) : PopupWindow() {
    init {
        innerInit(context)
    }

    private fun innerInit(context: Context) {
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        @SuppressLint("InflateParams")
        val contentView: View = LayoutInflater.from(context).inflate(
            getLayout(),
            null, false
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