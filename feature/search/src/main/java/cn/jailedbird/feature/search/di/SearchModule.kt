package cn.jailedbird.feature.search.di

import cn.jailedbird.feature.search.expose.SearchExpose
import cn.jailedbird.feature.search.exposeimpl.SearchExposeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SearchModule {
    @Singleton
    @Binds
    abstract fun bindSearchExpose(searchExposeImpl: SearchExposeImpl): SearchExpose

}