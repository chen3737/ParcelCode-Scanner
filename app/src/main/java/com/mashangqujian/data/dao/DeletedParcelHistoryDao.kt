package com.mashangqujian.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mashangqujian.data.model.DeletedParcelHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedParcelHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: DeletedParcelHistory)

    @Query("SELECT * FROM deleted_parcels ORDER BY deleted_at DESC")
    fun getAll(): Flow<List<DeletedParcelHistory>>

    @Query("SELECT * FROM deleted_parcels WHERE deleted_at >= :since ORDER BY deleted_at DESC")
    fun getRecent(since: Long): Flow<List<DeletedParcelHistory>>

    @Query("DELETE FROM deleted_parcels WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM deleted_parcels")
    suspend fun deleteAll()

    @Query("DELETE FROM deleted_parcels WHERE deleted_at < :beforeDate")
    suspend fun deleteOld(beforeDate: Long)
}
