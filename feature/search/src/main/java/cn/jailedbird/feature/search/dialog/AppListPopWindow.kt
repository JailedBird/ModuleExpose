package cn.jailedbird.feature.search.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.widget.PopupWindowCompat
import cn.jailedbird.feature.search.R
import cn.jailedbird.feature.search.data.entity.AppModel
import cn.jailedbird.feature.search.databinding.PopUpAppListBinding
import cn.jailedbird.feature.search.utils.gotoApkSettings
import cn.jailedbird.core.common.utils.setDebouncingClick
import cn.jailedbird.core.common.utils.toast
import cn.jailedbird.feature.search.utils.uninstallApk
import java.util.*


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
        override fun showInfo(appModel: AppModel) {
            context.gotoApkSettings(appModel.appPackageName)
        }

        override fun unInstall(appModel: AppModel) {
            context.uninstallApk(appModel.appPackageName)
        }

        override fun showDebugInfo(appModel: AppModel) {
            "count is ${appModel.count} ; timestamp is ${Date(appModel.timestamp)}".toast()
        }
    }

    override fun getLayout(): Int {
        return R.layout.pop_up_app_list
    }

    override fun initView(root: View) {
        binding = PopUpAppListBinding.bind(root)
    }

    override fun initEvent(root: View) {
        binding.tvInfo.setDebouncingClick {
            appModel?.let {
                listener.showInfo(it)
            }
            dismiss()
        }

        binding.tvUnInstall.setDebouncingClick {
            appModel?.let {
                listener.unInstall(it)
            }
            dismiss()
        }

        /*if (BuildConfig.DEBUG) {
            binding.tvDebug.visibility = View.VISIBLE
            binding.tvDebug.setDebouncingClick {
                appModel?.let {
                    listener.showDebugInfo(it)
                }
                dismiss()
            }
        }*/

    }

    interface Listener {
        fun showInfo(appModel: AppModel)
        fun unInstall(appModel: AppModel)
        fun showDebugInfo(appModel: AppModel)
    }

}