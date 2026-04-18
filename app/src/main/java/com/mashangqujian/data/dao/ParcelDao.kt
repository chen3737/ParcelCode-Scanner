package com.mashangqujian.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mashangqujian.data.model.Parcel
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(parcel: Parcel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parcels: List<Parcel>)
    
    @Update
    suspend fun update(parcel: Parcel)
    
    @Delete
    suspend fun delete(parcel: Parcel)
    
    @Query("DELETE FROM parcels WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    @Query("SELECT * FROM parcels ORDER BY smsDate DESC")
    fun getAllParcels(): Flow<List<Parcel>>
    
    @Query("SELECT * FROM parcels WHERE id = :id")
    suspend fun getParcelById(id: Int): Parcel?
    
    @Query("SELECT * FROM parcels WHERE isCollected = 0 ORDER BY smsDate ASC")
    fun getUncollectedParcels(): Flow<List<Parcel>>
    
    @Query("SELECT * FROM parcels WHERE isCollected = 1 ORDER BY smsDate DESC")
    fun getCollectedParcels(): Flow<List<Parcel>>
    
    @Query("SELECT * FROM parcels WHERE courierCompany LIKE '%' || :company || '%' ORDER BY smsDate DESC")
    fun getParcelsByCompany(company: String): Flow<List<Parcel>>
    
    @Query("SELECT * FROM parcels WHERE smsDate >= :startDate AND smsDate <= :endDate ORDER BY smsDate DESC")
    fun getParcelsByDateRange(startDate: Long, endDate: Long): Flow<List<Parcel>>
    
    @Query("SELECT DISTINCT courierCompany FROM parcels ORDER BY courierCompany")
    fun getAllCompanies(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM parcels WHERE isCollected = 0")
    fun getUncollectedCount(): Flow<Int>
    
    @Query("UPDATE parcels SET isCollected = :isCollected WHERE id = :id")
    suspend fun updateCollectionStatus(id: Int, isCollected: Boolean)

    @Query("UPDATE parcels SET isCollected = :isCollected, collected_at = :collectedAt WHERE id = :id")
    suspend fun updateCollectionStatusWithTime(id: Int, isCollected: Boolean, collectedAt: Long?)
    
    @Query("UPDATE parcels SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Int, notes: String)
    
    @Query("DELETE FROM parcels WHERE smsDate < :beforeDate")
    suspend fun deleteOldParcels(beforeDate: Long)
    
    @Query("SELECT * FROM parcels WHERE parcelCode LIKE '%' || :code || '%' ORDER BY smsDate DESC")
    fun searchByParcelCode(code: String): Flow<List<Parcel>>
    
    @Query("SELECT * FROM parcels WHERE address LIKE '%' || :address || '%' ORDER BY smsDate DESC")
    fun searchByAddress(address: String): Flow<List<Parcel>>
    
    @Query("SELECT parcelCode FROM parcels ORDER BY smsDate DESC")
    fun getAllParcelCodes(): List<String>

    @Query("DELETE FROM parcels")
    suspend fun deleteAllParcels()
}