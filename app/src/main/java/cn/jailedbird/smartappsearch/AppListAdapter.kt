package cn.jailedbird.smartappsearch

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.adapter.BaseSimpleListAdapter
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
        binding.ivIcon.load(bean?.appIcon)
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
                toast("${binding.bean?.packageName} more info")
            }
        }

    }
}