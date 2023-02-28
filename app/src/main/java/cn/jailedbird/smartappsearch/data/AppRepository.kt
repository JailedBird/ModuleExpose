package cn.jailedbird.smartappsearch.data

import cn.jailedbird.smartappsearch.data.entity.AppModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val appDao: AppDao) {
    fun getAppsFlow() = appDao.getAppsFlow()

    suspend fun getApps() = appDao.getApps()


    suspend fun updateRoom(pair: Pair<List<AppModel>, List<AppModel>>) {
        updateRoom(pair.first, pair.second)
    }

    private suspend fun updateRoom(toAdd: List<AppModel>, toDelete: List<AppModel>) {
        appDao.deleteAppList(toDelete)
        appDao.insertAppList(toAdd)
    }

    suspend fun updateAppModelCount(appPackageName: String, appName: String) {
        appDao.queryAppModel(appPackageName, appName)?.let {
            appDao.replaceAppModel(appPackageName, appName, it.count + 1)
        }
    }
}