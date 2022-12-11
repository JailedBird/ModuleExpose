package cn.jailedbird.smartappsearch.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import cn.jailedbird.smartappsearch.utils.finishProcess
import cn.jailedbird.smartappsearch.utils.log
import cn.jailedbird.smartappsearch.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Entity(tableName = "apps")
data class AppModel(
    @PrimaryKey @ColumnInfo(name = "id")
    var appId: Int,
    /*Full package*/
    var appPackageName: String,
    /*App name*/
    var appName: String,
    /*App ping yin (if chinese)*/
    var appNamePinyin: String? = null,
) {
    @Ignore
    var appIcon: Drawable? = null
    var appIconRes: Int? = null
    fun launch(context: Context) {
        context.packageManager.getLaunchIntentForPackage(appPackageName)?.let {
            try {
                context.startActivity(it)
            } catch (e: Exception) {
                e.message.toast()
                return
            }
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
                    (!appNamePinyin.isNullOrEmpty() && appNamePinyin!!.startsWith(key))
        }
    }

    class Diff() : DiffUtil.ItemCallback<AppModel>() {
        override fun areItemsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.appId == newItem.appId &&
                    oldItem.appPackageName == newItem.appPackageName
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.appId == newItem.appId &&
                    oldItem.appPackageName == newItem.appPackageName &&
                    oldItem.appName == newItem.appName
        }

    }
}