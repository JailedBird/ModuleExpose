package cn.jailedbird.smartappsearch.dialog

import android.content.Context
import android.view.View
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.PopUpAppSettingBinding
import cn.jailedbird.smartappsearch.utils.setDebouncedClick

class AppSettingsPopWindow(
    context: Context,
    private val listener: Listener
) :
    BaseSimplePopUp(context) {
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