package com.flxw.data.db

import androidx.room.*
import com.flxw.data.model.AppVariable
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {

    @Query("SELECT * FROM app_variables ORDER BY key ASC")
    fun getAll(): Flow<List<AppVariable>>

    @Query("SELECT * FROM app_variables WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): AppVariable?

    @Upsert
    suspend fun upsert(variable: AppVariable)

    @Query("DELETE FROM app_variables WHERE `key` = :key")
    suspend fun deleteByKey(key: String)
}