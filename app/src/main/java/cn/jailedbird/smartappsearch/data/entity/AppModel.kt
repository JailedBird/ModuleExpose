package cn.jailedbird.smartappsearch.data.entity

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.jailedbird.smartappsearch.data.AppDao
import cn.jailedbird.smartappsearch.utils.AppUtils
import cn.jailedbird.smartappsearch.utils.launchApk
import cn.jailedbird.smartappsearch.utils.log
import kotlinx.coroutines.*

@Entity(tableName = "apps")
data class AppModel(
    @PrimaryKey @ColumnInfo(name = "id")
    var appId: Int,
    var appPackageName: String,
    var appName: String,
    var appNamePinyin: String? = null,
    /* var launch: Intent? = null*/
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun launch(context: Context, appDao: AppDao) {
        if (!context.launchApk(appPackageName)) {
            return
        }
        // Use [GlobalScope] rather than LifecycleOwner to ensure [launch] must be execute
        GlobalScope.launch(Dispatchers.IO) {
            withTimeout(3000) {
                val apps = AppUtils.refresh(context, appDao)
                "Save ok items(${apps.size})".log()
            }
            "kill process".log()
            // context.finishProcess()
        }
        // When launch success
        // if (context is LifecycleOwner) {
        //     // refresh room job
        //     context.lifecycleScope.launch(Dispatchers.IO) {
        //         // Set time out 3000
        //         withTimeout(3000) {
        //             val apps = AppUtils.refresh(context)
        //             "Save ok items(${apps.size})".log()
        //         }
        //         "kill process".log()
        //         context.finishProcess()
        //     }
        // } else {
        //     context.finishProcess()
        // }

    }

    fun match(key: String?): Boolean {
        return if (key.isNullOrEmpty()) {
            true
        } else {
            appName.startsWith(key, ignoreCase = true) ||
                    (!appNamePinyin.isNullOrEmpty() && appNamePinyin!!.startsWith(key))
        }
    }

    class Diff : DiffUtil.ItemCallback<AppModel>() {
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