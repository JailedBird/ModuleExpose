package cn.jailedbird.smartappsearch.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import cn.jailedbird.smartappsearch.R


class AppListPopWindow(context: Context) : PopupWindow() {
    init {
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        @SuppressLint("InflateParams")
        val contentView: View = LayoutInflater.from(context).inflate(
            R.layout.pop_up_app_list,
            null, false
        )
        setContentView(contentView)
    }


}