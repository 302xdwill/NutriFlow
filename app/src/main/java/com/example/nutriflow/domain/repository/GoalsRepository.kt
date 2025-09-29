package com.example.nutriflow.domain.repository

import com.example.nutriflow.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    suspend fun insertGoals(goals: List<Goal>)
    suspend fun updateGoal(goal: Goal)
    fun getGoalsByUserId(userId: String): Flow<List<Goal>>
}