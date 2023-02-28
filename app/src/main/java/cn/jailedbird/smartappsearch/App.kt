package cn.jailedbird.smartappsearch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.jailedbird.smartappsearch.data.AppRepository
import cn.jailedbird.smartappsearch.utils.AppUtils
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {
    companion object {
        @Suppress("ObjectPropertyName")
        private var _applicationContext: Context? = null
        val applicationContext: Context
            get() = _applicationContext!!
        const val appName = "SmartAppSearch"
    }

    override fun onCreate() {
        super.onCreate()
        _applicationContext = this
        listenApkChange()
        MMKV.initialize(this)
    }

    @Inject
    lateinit var appRepository: AppRepository

    private fun listenApkChange() {
        /** Dynamic broadcast for apk install and uninstall
         * [StackOverflow](https://stackoverflow.com/questions/7470314/receiving-package-install-and-uninstall-events)*/
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(Dispatchers.IO) {
                    appRepository.refreshAppModelTable(
                        AppUtils.updateMeta(this@App, appRepository.getApps())
                    )
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }

        registerReceiver(broadcastReceiver, intentFilter)
    }

}