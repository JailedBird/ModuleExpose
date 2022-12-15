package cn.jailedbird.smartappsearch.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.widget.PopupWindowCompat
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.databinding.PopUpAppListBinding
import cn.jailedbird.smartappsearch.utils.gotoApkSettings
import cn.jailedbird.smartappsearch.utils.setDebouncedClick
import cn.jailedbird.smartappsearch.utils.uninstallApk


class AppListPopWindow(
    context: Context,
    private val appModel: AppModel?,
) : BaseSimplePopUp(context) {
    companion object {
        fun open(
            context: Context,
            anchor: View,
            appModel: AppModel?,
        ) {
            val popWindow = AppListPopWindow(context, appModel)
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

    private lateinit var binding: PopUpAppListBinding

    private val listener = object : Listener {
        override fun showInfo(appModel: AppModel?) {
            context.gotoApkSettings(appModel?.appPackageName)
        }

        override fun unInstall(appModel: AppModel?) {
            context.uninstallApk(appModel?.appPackageName)
        }
    }

    override fun getLayout(): Int {
        return R.layout.pop_up_app_list
    }

    override fun initView(root: View) {
        binding = PopUpAppListBinding.bind(root)
    }

    override fun initEvent(root: View) {
        binding.tvInfo.setDebouncedClick {
            listener.showInfo(appModel)
            dismiss()
        }
        binding.tvUnInstall.setDebouncedClick {
            listener.unInstall(appModel)
            dismiss()
        }
    }

    interface Listener {
        fun showInfo(appModel: AppModel?)
        fun unInstall(appModel: AppModel?)
    }


}