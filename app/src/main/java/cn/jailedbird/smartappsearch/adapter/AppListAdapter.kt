package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.ItemAppListBinding
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.utils.DebouncingUtils
import cn.jailedbird.smartappsearch.utils.toast
import coil.load

class AppListAdapter : BaseSimpleListAdapter<ItemAppListBinding, AppModel>(AppModel.Diff()) {
    private lateinit var context: Context
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
                binding.bean?.launch(context)
            }
        }

        binding.ivMore.setOnClickListener {
            if (DebouncingUtils.isValid(it)) {
                "${binding.bean?.appPackageName} more info".toast()
            }
        }

    }
}