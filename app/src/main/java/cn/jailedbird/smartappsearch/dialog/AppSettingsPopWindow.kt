package cn.jailedbird.smartappsearch.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.widget.PopupWindowCompat
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.PopUpAppSettingBinding
import cn.jailedbird.smartappsearch.utils.setDebouncedClick

class AppSettingsPopWindow(
    context: Context,
    private val listener: Listener
) : BaseSimplePopUp(context) {
    companion object {
        fun open(
            context: Context,
            anchor: View,
            listener: Listener,
        ) {
            val popWindow = AppSettingsPopWindow(context, listener)
            popWindow.contentView.measure(
                makeDropDownMeasureSpec(popWindow.width),
                makeDropDownMeasureSpec(popWindow.height)
            )
            // Right(center)
            val offsetX: Int = -(popWindow.contentView.measuredWidth - anchor.width / 2)
            val offsetY = 0
            PopupWindowCompat.showAsDropDown(popWindow, anchor, offsetX, offsetY, Gravity.START)
        }
    }


    private lateinit var binding: PopUpAppSettingBinding
    override fun getLayout(): Int {
        return R.layout.pop_up_app_setting
    }

    override fun initView(root: View) {
        binding = PopUpAppSettingBinding.bind(root)
    }

    override fun initEvent(root: View) {
        binding.tvRefreshApp.setDebouncedClick {
            listener.refreshApp()
            dismiss()
        }
        binding.tvRate.setDebouncedClick {
            listener.rate()
            dismiss()
        }
        binding.tvShare.setDebouncedClick {
            listener.share()
            dismiss()
        }
        binding.tvClearHistory.setDebouncedClick {
            listener.clearHistory()
            dismiss()
        }
        binding.tvSettings.setDebouncedClick {
            listener.settings()
            dismiss()
        }
    }

    interface Listener {
        fun refreshApp()
        fun rate()
        fun share()
        fun clearHistory()
        fun settings()
    }
}