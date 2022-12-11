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
        binding.tvRefreshApp.setDebouncedClick { listener.refreshApp() }
        binding.tvRate.setDebouncedClick { listener.rate() }
        binding.tvShare.setDebouncedClick { listener.share() }
        binding.tvClearHistory.setDebouncedClick { listener.clearHistory() }
        binding.tvSettings.setDebouncedClick { listener.settings() }
    }

    interface Listener {
        fun refreshApp()
        fun rate()
        fun share()
        fun clearHistory()
        fun settings()
    }
}