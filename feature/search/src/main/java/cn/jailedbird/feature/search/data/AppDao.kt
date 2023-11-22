package cn.jailedbird.feature.search.data

import androidx.room.*
import cn.jailedbird.feature.search.data.entity.AppModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY count DESC,timestamp DESC, appName, appNamePinyin")
    fun getAppsFlow(): Flow<List<AppModel>>

    @Query("SELECT * FROM apps")
    suspend fun getApps(): List<AppModel>

    @Query("SELECT * FROM apps WHERE appName=:appName AND appPackageName=:appPackageName LIMIT 1")
    suspend fun queryAppModel(appPackageName: String, appName: String): AppModel?

    @Query("UPDATE apps SET count=:count WHERE appName=:appName AND appPackageName=:appPackageName")
    suspend fun replaceAppModel(appPackageName: String, appName: String, count: Int)

    @Query("DELETE FROM apps")
    suspend fun deleteAll()

    @Delete(entity = AppModel::class)
    suspend fun deleteAppList(toDelete: List<AppModel>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAppList(app: List<AppModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(app: List<AppModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppModel(appModel: AppModel)


}