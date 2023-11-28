package cn.jailedbird.feature.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.core.common.utils.DebouncingUtils
import cn.jailedbird.core.common.utils.hideKeyboard
import cn.jailedbird.feature.search.R
import cn.jailedbird.feature.search.data.entity.AppModel
import cn.jailedbird.feature.search.dialog.AppListPopWindow
import cn.jailedbird.core.common.utils.log
import coil.load
import kotlinx.coroutines.*

class AppListTwoTypeAdapter(private val callback:(appModel:AppModel)->Unit) :
    ListAdapter<AppModel, AppListTwoTypeAdapter.ViewHolder>(AppModel.Diff()) {

    class ViewHolder(private val root: View, private val callback:(appModel:AppModel)->Unit) :
        RecyclerView.ViewHolder(root) {
        val ivIcon: ImageView = root.findViewById(R.id.ivIcon)
        val tvContent: TextView = root.findViewById(R.id.tvContent)
        private val ivMore: ImageView = root.findViewById(R.id.ivMore)

        init {
            root.setOnClickListener {
                if (DebouncingUtils.isValid(it)) {
                    root.context.hideKeyboard()
                    if (bean != null) {
                        callback.invoke(bean!!)
                    }
                }
            }

            ivMore.setOnClickListener {
                if (cn.jailedbird.core.common.utils.DebouncingUtils.isValid(it)) {
                    it.context.hideKeyboard()
                    AppListPopWindow.open(root.context, it, bean)
                }
            }
        }

        var job: Job?
            get() = root.getTag(R.id.tag_job) as? Job
            set(value) {
                root.setTag(R.id.tag_job, value)
            }

        var bean: AppModel?
            get() = root.getTag(R.id.tag_bean) as? AppModel
            set(value) {
                root.setTag(R.id.tag_bean, value)
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
        @Suppress("UnnecessaryVariable")
        val layoutRes = viewType
        val view = LayoutInflater.from(context).inflate(layoutRes, parent, false)
        return ViewHolder(view, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = getItem(position) ?: return
        holder.tvContent.text = bean.appName
        holder.bean = bean
        holder.job?.cancel()
        // Spend time function, place it in IO
        // Notes: Please cancel the last job to avoid the removal task being out of order;
        // also can ensure performance
        holder.job = scope.launch(Dispatchers.IO) {
            // cooperative response coroutine cancel
            // https://developer.android.com/kotlin/coroutines/coroutines-best-practices?hl=zh-cn#coroutine-cancellable
            // https://medium.com/androiddevelopers/cancellation-in-coroutines-aa6b90163629
            ensureActive()
            // Make spend function getApplicationIcon can response coroutine cancel
            val drawable =
                context.packageManager.getApplicationIcon(bean.appPackageName)
            ensureActive()
            holder.ivIcon.load(drawable) {
                placeholder(R.drawable.ic_android)
                error(R.drawable.ic_android)
            }
        }
    }
}