package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.databinding.ItemAppListBinding
import cn.jailedbird.smartappsearch.dialog.AppListPopWindow
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.utils.DebouncingUtils
import cn.jailedbird.smartappsearch.utils.hideKeyboard
import cn.jailedbird.smartappsearch.utils.log
import coil.load
import kotlinx.coroutines.*


class AppListAdapter : BaseSimpleListAdapter<ItemAppListBinding, AppModel>(AppModel.Diff()) {
    private lateinit var context: Context
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.message.log()
        }

    private val scope =
        CoroutineScope(SupervisorJob() + coroutineExceptionHandler)


    override fun initLayout(): Int {
        return R.layout.item_app_list
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        scope.cancel("onDetachedFromRecyclerView, cancel this scope")
    }

    override fun bind(bean: AppModel?, binding: ItemAppListBinding) {
        binding.bean = bean
        bean?.let {
            scope.launch(Dispatchers.IO) {
                // Spend time function, place it in IO
                val drawable = context.packageManager.getApplicationIcon(it.appPackageName)
                binding.ivIcon.load(drawable) {
                    placeholder(R.drawable.ic_android)
                    error(R.drawable.ic_android)
                }
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
                AppListPopWindow.open(context, it, binding.bean)
            }
        }

    }


}