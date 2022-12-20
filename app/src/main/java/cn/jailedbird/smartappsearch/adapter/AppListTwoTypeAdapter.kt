package cn.jailedbird.smartappsearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.jailedbird.smartappsearch.R
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.dialog.AppListPopWindow
import cn.jailedbird.smartappsearch.utils.DebouncingUtils
import cn.jailedbird.smartappsearch.utils.hideKeyboard
import cn.jailedbird.smartappsearch.utils.log
import coil.load
import kotlinx.coroutines.*

class AppListTwoTypeAdapter :
    ListAdapter<AppModel, AppListTwoTypeAdapter.ViewHolder>(AppModel.Diff()) {

    class ViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        private val ivMore: ImageView = view.findViewById(R.id.ivMore)
        val tvContent: TextView = view.findViewById(R.id.tvContent)

        init {
            view.setOnClickListener {
                if (DebouncingUtils.isValid(it)) {
                    view.context.hideKeyboard()
                    bean?.launch(view.context)
                }
            }

            ivMore.setOnClickListener {
                if (DebouncingUtils.isValid(it)) {
                    it.context.hideKeyboard()
                    AppListPopWindow.open(view.context, it, bean)
                }
            }
        }

        var job: Job?
            get() = view.getTag(R.id.tag_job) as? Job
            set(value) {
                view.setTag(R.id.tag_job, value)
            }

        var bean: AppModel?
            get() = view.getTag(R.id.tag_bean) as? AppModel
            set(value) {
                view.setTag(R.id.tag_bean, value)
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
        return ViewHolder(view)
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
            // Make spend function getApplicationIcon can response coroutine cancel
            val drawable = runInterruptible {
                context.packageManager.getApplicationIcon(bean.appPackageName)
            }
            holder.ivIcon.load(drawable) {
                placeholder(R.drawable.ic_android)
                error(R.drawable.ic_android)
            }
        }
    }
}