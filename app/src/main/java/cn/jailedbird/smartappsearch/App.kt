package cn.jailedbird.smartappsearch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.jailedbird.smartappsearch.utils.log
import cn.jailedbird.smartappsearch.utils.toast
import dagger.hilt.android.HiltAndroidApp


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
        /*@Suppress("EXPERIMENTAL_API_USAGE")
        GlobalScope.launch(Dispatchers.IO) {bas

        }*/
    }

    private fun listenApkChange() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.toString().log()
                intent.toString().toast()
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_INSTALL)
        }
        registerReceiver(br, intentFilter)
    }
}