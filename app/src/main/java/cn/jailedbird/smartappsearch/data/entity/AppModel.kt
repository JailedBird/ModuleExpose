package cn.jailedbird.smartappsearch.data.entity

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.jailedbird.smartappsearch.utils.launchApk

@Entity(tableName = "apps")
data class AppModel(
    @PrimaryKey @ColumnInfo(name = "id")
    var appId: Int,
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
            return oldItem.appId == newItem.appId
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.appId == newItem.appId &&
                    oldItem.appPackageName == newItem.appPackageName &&
                    oldItem.appName == newItem.appName
        }

    }
}