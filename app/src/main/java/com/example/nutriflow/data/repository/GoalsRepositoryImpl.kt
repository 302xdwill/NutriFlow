package com.example.nutriflow.data.repository

import com.example.nutriflow.data.local.dao.GoalDao
import com.example.nutriflow.data.local.entity.GoalEntity
import com.example.nutriflow.domain.model.Goal
import com.example.nutriflow.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalsRepositoryImpl(private val goalDao: GoalDao) : GoalsRepository {

    override suspend fun insertGoals(goals: List<Goal>) {
        val entities = goals.map { GoalEntity.fromDomain(it) }
        goalDao.insertGoals(entities)
    }

    override suspend fun updateGoal(goal: Goal) {
        goalDao.insertGoal(GoalEntity.fromDomain(goal))
    }

    override fun getGoalsByUserId(userId: String): Flow<List<Goal>> {
        return goalDao.getAllGoalsByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}