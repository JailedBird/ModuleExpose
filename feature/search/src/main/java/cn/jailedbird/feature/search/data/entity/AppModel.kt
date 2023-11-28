package cn.jailedbird.feature.search.data.entity

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import cn.jailedbird.core.common.utils.finishProcess
import cn.jailedbird.core.common.utils.log
import cn.jailedbird.feature.search.data.AppRepository
import cn.jailedbird.feature.search.utils.LAUNCH_DELAY_TIME
import cn.jailedbird.feature.search.utils.launchApk
import cn.jailedbird.feature.search.utils.packageManagerAppList
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import okhttp3.internal.wait

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

        suspend fun updateMeta(
            context: Context,
            old: List<AppModel>,
            withReduce: Boolean = false
        ): List<AppModel> {
            return mergeMemoryWithRoom(context.packageManagerAppList(), old, withReduce)
        }

        private fun mergeMemoryWithRoom(
            new: List<AppModel>,
            old: List<AppModel>,
            withReduce: Boolean = false
        ): List<AppModel> {
            val newSet = new.toMutableSet()
            val mixed = mutableListOf<AppModel>()
            for (item in old) {
                if (newSet.remove(item)) {
                    mixed.add(item)
                }
            }
            newSet.addAll(mixed)
            if (withReduce) {
                for (it in newSet) {
                    it.reduce()
                }
            }
            return newSet.toList()
        }
    }

    fun launch(context: Context) {
        if (context.launchApk(appPackageName, activityName)) {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                EntryPointAccessors.fromApplication<AppRepositoryEntryPoint>(context.applicationContext)
                    .appRepository().refreshAppModel(this@AppModel)
                // 延时200ms后杀死进程存在问题 如小米等手机优化 首次启动其他App会出现确认弹窗 无语！
                // 提示 是否允许启动xxx 等用户点击是 但是此时 搜索进程已被杀死 无法再启动其他App
                // delay(LAUNCH_DELAY_TIME)
                // if (context is Activity) {
                //     context.finishProcess()
                // }
                val time = if (count > PITCH_ADD_COUNT) {
                    LAUNCH_DELAY_TIME
                }else{
                    // 方案仍然存在缺陷
                    6000 // 保证确认时间重组 但首次启动可能导致返回是search页面仍在
                }
                "${this@AppModel} delay $time".log()
                // 根据情况判定
                delay(time)
                // 另外直接杀死 也不是很合适 冷启动不是很快
                (context as? Activity)?.moveTaskToBack(false)
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