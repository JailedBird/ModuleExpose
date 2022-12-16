package cn.jailedbird.smartappsearch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
abstract class BaseSimpleListAdapter<DB : ViewDataBinding, T>(
    df: DiffUtil.ItemCallback<T>,
) : ListAdapter<T, BaseSimpleListAdapter.ViewHolder<DB>>(df) {

    abstract fun initLayout(): Int
    abstract fun bind(bean: T?, binding: DB)
    abstract fun event(binding: DB)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<DB> {
        return ViewHolder.create(parent, initLayout(), ::event)
    }

    override fun onBindViewHolder(holder: ViewHolder<DB>, position: Int) {
        bind(getItem(position), holder.binding)
        holder.binding.executePendingBindings()
    }

    class ViewHolder<DB : ViewDataBinding>(val binding: DB) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun <DB : ViewDataBinding> create(
                parent: ViewGroup,
                layoutId: Int, listener: ((DB) -> Unit)?
            ): ViewHolder<DB> {
                /** LayoutInflater item view*/
                val binding =
                    DataBindingUtil.inflate<DB>(
                        LayoutInflater.from(parent.context),
                        layoutId,
                        parent,
                        false
                    )

                listener?.invoke(binding)
                return ViewHolder(binding)
            }
        }
    }

}

