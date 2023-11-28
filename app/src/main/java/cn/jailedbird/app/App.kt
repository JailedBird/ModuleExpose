package cn.jailedbird.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.jailedbird.core.common.utils.log
import cn.jailedbird.core.settings.Settings
import cn.jailedbird.feature.search.data.AppRepository
import cn.jailedbird.feature.search.data.entity.AppModel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        "App create".log()
        listenApkChange()
        Settings.init(this)
    }

    @Inject
    lateinit var appRepository: AppRepository

    /** Dynamic broadcast for apk install and uninstall
     * [StackOverflow](https://stackoverflow.com/questions/7470314/receiving-package-install-and-uninstall-events)*/
    private fun listenApkChange() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(Dispatchers.IO) {
                    appRepository.refreshAppModelTable(
                        AppModel.updateMeta(
                            this@App,
                            appRepository.getApps()
                        )
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