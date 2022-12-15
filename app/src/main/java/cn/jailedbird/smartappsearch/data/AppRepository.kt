package cn.jailedbird.smartappsearch.data

import cn.jailedbird.smartappsearch.data.entity.AppModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val appDao: AppDao) {
    fun getAppsFlow() = appDao.getAppsFlow()
    suspend fun insertAll(app: List<AppModel>) = appDao.insertAll(app)
}