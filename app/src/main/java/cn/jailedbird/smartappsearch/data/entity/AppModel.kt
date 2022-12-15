package cn.jailedbird.smartappsearch.data.entity

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import cn.jailedbird.smartappsearch.utils.launchApk

@Entity(tableName = "apps", primaryKeys = ["appPackageName", "appName"])
data class AppModel(
    var appPackageName: String,
    var appName: String,
    var appNamePinyin: String? = null,
    val activityName: String? = null
) {
    fun launch(context: Context) {
        context.launchApk(appPackageName, activityName)
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
            return oldItem.appPackageName == newItem.appPackageName &&
                    oldItem.appName == newItem.appName
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.appPackageName == newItem.appPackageName &&
                    oldItem.appName == newItem.appName
        }

    }
}