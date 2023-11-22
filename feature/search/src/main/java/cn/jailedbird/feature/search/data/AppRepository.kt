package cn.jailedbird.feature.search.data

import cn.jailedbird.feature.search.data.entity.AppModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val appDao: AppDao) {

    fun getAppsFlow() = appDao.getAppsFlow()

    suspend fun getApps() = appDao.getApps()

    suspend fun refreshAppModelTable(table: List<AppModel>) {
        appDao.deleteAll()
        appDao.insertAll(table)
    }

    suspend fun refreshAppModel(appModel: AppModel) {
        appModel.onItemClick()
        appDao.insertAppModel(appModel)
    }
}