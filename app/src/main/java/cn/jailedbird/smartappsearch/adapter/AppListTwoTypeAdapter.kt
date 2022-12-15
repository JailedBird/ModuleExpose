package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
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

    class ViewHolder(private val view: View, private val viewType: Int) :
        RecyclerView.ViewHolder(view) {

        private val bindingFirst: ItemAppListFirstBinding by lazy(LazyThreadSafetyMode.NONE) {
            DataBindingUtil.bind(view)!!
        }
        private val bindingNotFirst: ItemAppListBinding by lazy(LazyThreadSafetyMode.NONE) {
            DataBindingUtil.bind(view)!!
        }

        val holderIcon: ImageView
            get() = if (viewType == R.layout.item_app_list_first) {
                bindingFirst.ivIcon
            } else {
                bindingNotFirst.ivIcon
            }
        val holderMore: ImageView
            get() = if (viewType == R.layout.item_app_list_first) {
                bindingFirst.ivMore
            } else {
                bindingNotFirst.ivMore
            }

        var bean: AppModel?
            get() {
                return if (viewType == R.layout.item_app_list_first) {
                    bindingFirst.bean
                } else {
                    bindingNotFirst.bean
                }
            }
            set(value) {
                if (viewType == R.layout.item_app_list_first) {
                    bindingFirst.bean = value
                } else {
                    bindingNotFirst.bean = value
                }
            }

        var job: Job?
            get() {
                return if (viewType == R.layout.item_app_list_first) {
                    bindingFirst.job
                } else {
                    bindingNotFirst.job
                }
            }
            set(value) {
                if (viewType == R.layout.item_app_list_first) {
                    bindingFirst.job = value
                } else {
                    bindingNotFirst.job = value
                }
            }


        fun executeBind() {
            if (viewType == R.layout.item_app_list_first) {
                bindingFirst.executePendingBindings()
            } else {
                bindingNotFirst.executePendingBindings()
            }
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
        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
        return ViewHolder(view, viewType).apply {
            view.setOnClickListener {
                if (DebouncingUtils.isValid(it)) {
                    context.hideKeyboard()
                    bean?.launch(context)
                }
            }

            holderMore.setOnClickListener {
                if (DebouncingUtils.isValid(it)) {
                    AppListPopWindow.open(context, it, bean)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = getItem(position)
        holder.bean = bean
        bean?.let {
            holder.job?.cancel()
            val job = scope.launch(Dispatchers.IO) {
                // Spend time function, place it in IO
                // Make spend function getApplicationIcon can response cancel
                val drawable = runInterruptible {
                    context.packageManager.getApplicationIcon(it.appPackageName)
                }
                holder.holderIcon.load(drawable) {
                    placeholder(R.drawable.ic_android)
                    error(R.drawable.ic_android)
                }
            }
            holder.job = job
        }
        holder.executeBind()
    }
}