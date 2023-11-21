package cn.jailedbird.feature.settings.di

import cn.jailedbird.feature.settings.expose.SettingExpose
import cn.jailedbird.feature.settings.exposeimpl.SettingExposeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingModule {
    @Binds
    @Singleton
    abstract fun bindSettingExpose(settingExposeImpl: SettingExposeImpl): SettingExpose
}