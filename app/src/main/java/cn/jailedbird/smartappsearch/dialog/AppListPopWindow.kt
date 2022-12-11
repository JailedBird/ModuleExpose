package cn.jailedbird.smartappsearch.dialog

import android.content.Context
import android.view.View
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.PopUpAppListBinding
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.utils.setDebouncedClick


class AppListPopWindow(
    context: Context,
    private val appModel: AppModel?,
    private val listener: Listener
) :
    BaseSimplePopUp(context) {
    private lateinit var binding: PopUpAppListBinding
    override fun getLayout(): Int {
        return R.layout.pop_up_app_list
    }

    override fun initView(root: View) {
        binding = PopUpAppListBinding.bind(root)
    }

    override fun initEvent(root: View) {
        binding.tvInfo.setDebouncedClick { listener.showInfo(appModel) }
        binding.tvUnInstall.setDebouncedClick { listener.unInstall(appModel) }
    }

    interface Listener {
        fun showInfo(appModel: AppModel?)
        fun unInstall(appModel: AppModel?)
    }

}