package cn.jailedbird.smartappsearch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.jailedbird.smartappsearch.model.AppModel

@Dao
interface AppModelDao {
    @Query("SELECT * FROM apps ORDER BY appName")
    suspend fun getApps(): List<AppModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(app: List<AppModel>)
}