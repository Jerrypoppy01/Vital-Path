package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class HealthLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Format: "yyyy-MM-dd"
    val type: String, // "water", "steps", "sleep", "active_minutes"
    val value: Double, // The tracked quantity (e.g., ml, count, hours, minutes)
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val type: String, // "water", "steps", "sleep", "active_minutes"
    val value: Double // Target value (e.g., 2000.0, 10000.0, 8.0, 30.0)
)
