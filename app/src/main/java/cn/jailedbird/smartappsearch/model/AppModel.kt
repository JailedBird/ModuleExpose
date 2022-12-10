package cn.jailedbird.smartappsearch.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.jailedbird.smartappsearch.utils.finishProcess
import cn.jailedbird.smartappsearch.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Entity(tableName = "apps")
data class AppModel(
    @PrimaryKey  /*@ColumnInfo(name = "id")*/
    val id: Int,
    /*Full package*/
    val packageName: String,
    /*App name*/
    val appName: String,
    /*App ping yin (if chinese)*/
    val appNamePinyin: String? = null,
    val appIcon: Drawable? = null,
) {
    fun launch(context: Context) {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            context.startActivity(it)
        }
        if (context is LifecycleOwner) {
            context.lifecycleScope.launch(Dispatchers.IO) {
                withTimeout(1000) {
                    //  TODO save recently select result
                    delay(200)
                    "save ok".log()
                }
                "kill process".log()
                context.finishProcess()
            }
        } else {
            context.finishProcess()
        }

    }

    fun match(key: String?): Boolean {
        return if (key.isNullOrEmpty()) {
            true
        } else {
            appName.startsWith(key) ||
                    (!appNamePinyin.isNullOrEmpty() && appNamePinyin.startsWith(key))
        }
    }

    class Diff() : DiffUtil.ItemCallback<AppModel>() {
        override fun areItemsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.packageName == newItem.packageName &&
                    oldItem.appName == newItem.appName
        }

    }
}