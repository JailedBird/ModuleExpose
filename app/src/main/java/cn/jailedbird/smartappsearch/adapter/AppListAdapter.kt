package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.ItemAppListBinding
import cn.jailedbird.smartappsearch.dialog.AppListPopWindow
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.utils.DebouncingUtils
import cn.jailedbird.smartappsearch.utils.hideKeyboard
import cn.jailedbird.smartappsearch.utils.toast
import coil.load


class AppListAdapter : BaseSimpleListAdapter<ItemAppListBinding, AppModel>(AppModel.Diff()) {
    private lateinit var context: Context
    private val listener = object : AppListPopWindow.Listener {
        override fun showInfo(appModel: AppModel?) {
            appModel?.appPackageName?.let {
                "show info $it".toast()

            }
        }

        /**
         * Remove apk method [link](https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents)
         * */
        override fun unInstall(appModel: AppModel?) {
            appModel?.appPackageName?.let {
                "uninstall $it".toast()
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:$it")
                context.startActivity(intent)
            }
        }

    }

    override fun initLayout(): Int {
        return R.layout.item_app_list
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
    }

    override fun bind(bean: AppModel?, binding: ItemAppListBinding) {
        binding.bean = bean
        bean?.let {
            try {
                binding.ivIcon.load(context.packageManager.getApplicationIcon(it.appPackageName)) {
                    placeholder(R.drawable.ic_android)
                    error(R.drawable.ic_android)
                }
            } catch (e: Exception) {
                binding.ivIcon.load(R.drawable.ic_android)
            }
        }
        binding.executePendingBindings()
    }

    override fun event(binding: ItemAppListBinding) {
        binding.tvContent.setOnClickListener {
            if (DebouncingUtils.isValid(it)) {
                context.hideKeyboard()
                binding.bean?.launch(context)
            }
        }

        binding.ivMore.setOnClickListener {
            if (DebouncingUtils.isValid(it)) {
                AppListPopWindow.open(context, it, binding.bean, listener)
            }
        }

    }


}