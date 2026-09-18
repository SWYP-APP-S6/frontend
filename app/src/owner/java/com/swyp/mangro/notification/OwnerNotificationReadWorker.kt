package com.swyp.mangro.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class OwnerNotificationReadWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong("notificationId", 0)
        if (id <= 0) return Result.failure()
        val dependencies = EntryPointAccessors.fromApplication(applicationContext, OwnerTokenWorker.Dependencies::class.java)
        return try {
            if (dependencies.authStore().authKey.first() == null) return Result.success()
            val error = dependencies.repository().markAsRead(id).first().exceptionOrNull()
            when {
                error == null -> Result.success()
                error is HttpException && error.code() in 400..499 && error.code() != 429 -> Result.failure()
                else -> Result.retry()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context, notificationId: Long) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "owner-notification-read-$notificationId",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<OwnerNotificationReadWorker>()
                    .setInputData(workDataOf("notificationId" to notificationId))
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build(),
            )
        }
    }
}
