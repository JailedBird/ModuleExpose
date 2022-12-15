package cn.jailedbird.smartappsearch.data

import cn.jailedbird.smartappsearch.data.entity.AppModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val appDao: AppDao) {
    fun getAppsFlow() = appDao.getAppsFlow()

    /**
     * Primary key is random id, not the package name, avoid two same app item,
     * delete all table content before insert
     * */
    suspend fun insertAll(app: List<AppModel>) {
        // 存在问题 如何删除旧数据? 新数据添加时会被覆盖 但是旧数据删除时 不会清空里面的老数据
        appDao.deleteAll()
        appDao.insertAll(app)
    }
}