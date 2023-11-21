package cn.jailedbird.feature.settings.exposeimpl

import android.content.Context
import cn.jailedbird.feature.settings.SettingsActivity
import cn.jailedbird.feature.settings.expose.SettingExpose
import javax.inject.Inject


class SettingExposeImpl @Inject constructor() : SettingExpose {
    override fun startSettingActivity(context: Context) {
        SettingsActivity.start(context)
    }
}