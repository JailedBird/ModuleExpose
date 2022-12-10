package cn.jailedbird.smartappsearch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.jailedbird.smartappsearch.model.AppModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppModelDao {
    @Query("SELECT * FROM apps ORDER BY appName")
    fun getPlants(): Flow<List<AppModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(app: List<AppModel>)
}