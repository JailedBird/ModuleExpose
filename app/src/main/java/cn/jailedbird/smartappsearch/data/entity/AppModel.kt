package cn.jailedbird.smartappsearch.data.entity

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import cn.jailedbird.smartappsearch.data.AppRepository
import cn.jailedbird.smartappsearch.model.AppConfig
import cn.jailedbird.smartappsearch.utils.launchApk
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*

@Entity(tableName = "apps", primaryKeys = ["appPackageName", "appName"])
data class AppModel(
    val appPackageName: String,
    val appName: String,
    val appNamePinyin: String? = null,
    val activityName: String? = null,
    var count: Int = 0,
    var timestamp: Long = 0,
) {
    companion object {
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface AppRepositoryEntryPoint {
            fun appRepository(): AppRepository
        }

        /**
         *  3 days if not open start reduce count (equal days)
         * */
        const val PITCH_ADD_COUNT = 10
        const val ONE_DAY_MILS = 1000 * 60 * 60 * 24
        const val REDUCE_GAP = 3
    }

    fun launch(context: Context) {
        if (context.launchApk(appPackageName, activityName)) {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                EntryPointAccessors.fromApplication<AppRepositoryEntryPoint>(context.applicationContext)
                    .appRepository().refreshAppModel(this@AppModel)
                delay(AppConfig.LAUNCH_DELAY_TIME)
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }

    fun onItemClick() {
        this.count += PITCH_ADD_COUNT
        this.timestamp = System.currentTimeMillis()
    }

    fun reduce() {
        if (this.timestamp == 0L) {
            return
        }
        val currentMils = System.currentTimeMillis()
        val dis = currentMils - this.timestamp
        if (dis <= 0) {
            return
        }
        val days = dis / ONE_DAY_MILS
        if (days >= REDUCE_GAP) {
            this.count = (this.count - ONE_DAY_MILS).coerceAtLeast(0)
        }
    }

    fun match(key: String?, matchCenter: Boolean = false): Boolean {
        return if (key.isNullOrEmpty()) {
            true
        } else {
            if (matchCenter) {
                appName.indexOf(key, ignoreCase = true) != -1 ||
                        (!appNamePinyin.isNullOrEmpty() &&
                                appNamePinyin.indexOf(key, ignoreCase = true) != -1)
            } else {
                appName.startsWith(key, ignoreCase = true) ||
                        (!appNamePinyin.isNullOrEmpty() && appNamePinyin.startsWith(key))
            }
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

    override fun equals(other: Any?): Boolean {
        return (other is AppModel) && other.appName == appName && other.appPackageName == appPackageName
    }

    override fun hashCode(): Int {
        var result = appPackageName.hashCode()
        result = 31 * result + appName.hashCode()
        return result
    }
}