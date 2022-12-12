package cn.jailedbird.smartappsearch.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import androidx.core.widget.PopupWindowCompat
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.PopUpAppListBinding
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.utils.setDebouncedClick
import cn.jailedbird.smartappsearch.utils.toast


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

    private val listener = object : AppListPopWindow.Listener {
        override fun showInfo(appModel: AppModel?) {
            appModel?.appPackageName?.let {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$it")
                }
                context.startActivity(intent)
            }
        }

        /**
         * Remove apk method [link](https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents)
         * */
        override fun unInstall(appModel: AppModel?) {
            appModel?.appPackageName?.let {
                try {
                    context.startActivity(Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:$it")
                    })
                } catch (e: Exception) {
                    e.message?.toast()
                }
            }
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