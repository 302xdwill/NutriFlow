package com.example.nutriflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.nutriflow.domain.model.Goal

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["email"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "type"], unique = true)] // Una meta por tipo y usuario
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val type: String,
    val value: Double
) {
    companion object {
        fun fromDomain(goal: Goal): GoalEntity {
            return GoalEntity(
                userId = goal.userId,
                type = goal.type,
                value = goal.value
            )
        }
    }

    fun toDomain(): Goal {
        return Goal(
            id = this.id,
            userId = this.userId,
            type = this.type,
            value = this.value
        )
    }
}