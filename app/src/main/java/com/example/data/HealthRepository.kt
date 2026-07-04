package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HealthRepository(private val healthDao: HealthDao) {

    val allLogs: Flow<List<HealthLog>> = healthDao.getAllLogs()
    val allGoals: Flow<List<Goal>> = healthDao.getAllGoals()

    fun getLogsByDate(date: String): Flow<List<HealthLog>> {
        return healthDao.getLogsByDate(date)
    }

    suspend fun insertLog(log: HealthLog) {
        healthDao.insertLog(log)
    }

    suspend fun deleteLogById(id: Int) {
        healthDao.deleteLogById(id)
    }

    suspend fun insertGoal(goal: Goal) {
        healthDao.insertGoal(goal)
    }

    suspend fun clearAll() {
        healthDao.clearAllLogs()
    }

    suspend fun initializeDefaultGoalsIfEmpty() {
        // Collect current goals once to see if we need to insert defaults
        val currentGoals = allGoals.firstOrNull() ?: emptyList()
        if (currentGoals.isEmpty()) {
            val defaults = listOf(
                Goal("water", 2000.0), // 2000 ml
                Goal("steps", 10000.0), // 10000 steps
                Goal("sleep", 8.0), // 8 hours
                Goal("active_minutes", 30.0) // 30 minutes
            )
            for (goal in defaults) {
                healthDao.insertGoal(goal)
            }
        }
    }
}
