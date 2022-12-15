package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.databinding.ItemAppListBinding
import cn.jailedbird.smartappsearch.dialog.AppListPopWindow
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
            // TODO 子线程加载可能会存在图片时序问题 由于本地加载可能不会出现
            // 但是这场景换做了网络加载是否会出现？ 使用本地添加job的方案即是取消上次任务
            binding.job?.cancel()
            val job = scope.launch(Dispatchers.IO) {
                // Spend time function, place it in IO
                // Make spend function getApplicationIcon can response cancel
                val drawable = runInterruptible {
                    context.packageManager.getApplicationIcon(it.appPackageName)
                }
                binding.ivIcon.load(drawable) {
                    placeholder(R.drawable.ic_android)
                    error(R.drawable.ic_android)
                }
            }
            binding.job = job
        }
        binding.executePendingBindings()
    }

    override fun event(binding: ItemAppListBinding) {
        binding.root.setOnClickListener {
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