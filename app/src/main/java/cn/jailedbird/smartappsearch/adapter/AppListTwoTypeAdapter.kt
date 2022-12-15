package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.databinding.ItemAppListBinding
import cn.jailedbird.smartappsearch.databinding.ItemAppListFirstBinding
import cn.jailedbird.smartappsearch.dialog.AppListPopWindow
import cn.jailedbird.smartappsearch.utils.DebouncingUtils
import cn.jailedbird.smartappsearch.utils.hideKeyboard
import cn.jailedbird.smartappsearch.utils.log
import coil.load
import kotlinx.coroutines.*


class AppListTwoTypeAdapter :
    ListAdapter<AppModel, AppListTwoTypeAdapter.ViewHolder>(AppModel.Diff()) {

    companion object {
        const val ITEM_TYPE_FIRST = 0
        const val ITEM_TYPE_NOT_FIRST = 1
    }

    class ViewHolder(val binding: ViewDataBinding, private val viewType: Int) :
        RecyclerView.ViewHolder(binding.root) {

        val holderRoot = binding.root

        val holderIvMore: View
            get() = if (viewType == R.layout.item_app_list_first) {
                (binding as ItemAppListFirstBinding).ivMore
            } else {
                (binding as ItemAppListFirstBinding).ivMore
            }

        fun executeBind() {
            binding.executePendingBindings()
        }

        companion object {
            /*fun create(): ViewHolder {

            }*/
        }
    }

    private lateinit var context: Context

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.message.log()
        }

    private val scope =
        CoroutineScope(SupervisorJob() + coroutineExceptionHandler)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        scope.cancel("onDetachedFromRecyclerView, cancel this scope")
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_app_list_first
        } else {
            R.layout.item_app_list
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        

        return if (viewType == ITEM_TYPE_FIRST) {
            val binding = DataBindingUtil.inflate<ItemAppListFirstBinding>(
                LayoutInflater.from(context),
                R.layout.item_app_list_first,
                parent,
                false
            )
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
            ViewHolder(binding, viewType)
        } else {
            val binding = DataBindingUtil.inflate<ItemAppListBinding>(
                LayoutInflater.from(context),
                R.layout.item_app_list,
                parent,
                false
            )
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
            ViewHolder(binding, viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == ITEM_TYPE_FIRST) {
            val binding = holder.binding as ItemAppListFirstBinding
            val bean = getItem(position)
            binding.bean = bean
            bean?.let {
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
        } else {
            val binding = holder.binding as ItemAppListBinding
            val bean = getItem(position)
            binding.bean = bean
            bean?.let {
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
    }
}