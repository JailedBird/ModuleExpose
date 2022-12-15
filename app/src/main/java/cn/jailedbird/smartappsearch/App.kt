package cn.jailedbird.smartappsearch

import android.app.Application
import android.content.Context
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
    }
}