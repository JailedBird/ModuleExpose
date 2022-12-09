package cn.jailedbird.smartappsearch.model

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import cn.jailedbird.smartappsearch.utils.finishProcess
import cn.jailedbird.smartappsearch.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


data class AppModel(
    val id: Int,
    /*Full package*/
    val packageName: String,
    /*App name*/
    val appName: String,
    /*App ping yin (if chinese)*/
    val appNamePinyin: String? = null,
) {
    fun launch(context: Context) {
        if (context is AppCompatActivity) {
            context.lifecycleScope.launch(Dispatchers.IO) {
                withTimeout(1000) {
                    //  TODO save recently select result
                    delay(200)
                    "save ok".log()
                }
                // 进程退出
                "kill process".log()
                context.finishProcess()
            }
        }
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            context.startActivity(it)
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
            return oldItem.id == newItem.id && oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem.id == newItem.id && oldItem.packageName == newItem.packageName && oldItem.appName == newItem.appName
        }

    }
}