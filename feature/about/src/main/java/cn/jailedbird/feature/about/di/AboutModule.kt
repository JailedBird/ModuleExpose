package cn.jailedbird.feature.about.di

import cn.jailedbird.feature.about.expose.AboutExpose
import cn.jailedbird.feature.about.exposeimpl.AboutExposeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AboutModule {
    @Singleton
    @Binds
    abstract fun bindAboutExpose(aboutExposeImpl: AboutExposeImpl): AboutExpose
}