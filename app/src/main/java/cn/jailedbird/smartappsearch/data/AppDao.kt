package cn.jailedbird.smartappsearch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.jailedbird.smartappsearch.data.entity.AppModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY appName and appNamePinyin")
    fun getAppsFlow(): Flow<List<AppModel>>


    @Query("DELETE FROM apps")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(app: List<AppModel>)
}