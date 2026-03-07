package com.flxw.repository

import com.flxw.data.db.VariableDao
import com.flxw.data.model.AppVariable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariableRepository @Inject constructor(
    private val variableDao: VariableDao
) {
    fun getAll(): Flow<List<AppVariable>> = variableDao.getAll()

    suspend fun get(key: String): String? = variableDao.getByKey(key)?.value

    suspend fun set(key: String, value: String) =
        variableDao.upsert(AppVariable(key = key, value = value))

    suspend fun delete(key: String) = variableDao.deleteByKey(key)
}