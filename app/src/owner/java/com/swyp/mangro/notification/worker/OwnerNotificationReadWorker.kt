package com.swyp.mangro.notification.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.data.owner.notification.OwnerNotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

@HiltWorker
class OwnerNotificationReadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authStore: AuthStore,
    private val notificationRepository: OwnerNotificationRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong("notificationId", 0)
        if (id <= 0) return Result.failure()

        val authKey = runCatching {
            authStore.authKey.first()
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            return Result.retry()
        }

        if (authKey == null) return Result.success()

        val result = try {
            when (val throwable = notificationRepository.markAsRead(id).first().exceptionOrNull()) {
                null -> Result.success()
                is HttpException if (throwable.code() in 400..499 && throwable.code() != 429) -> Result.failure()
                else -> Result.retry()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.retry()
        }

        return result
    }

    companion object {
        fun enqueue(context: Context, id: Long) {
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    uniqueWorkName = "owner-notification-read-$id",
                    existingWorkPolicy = ExistingWorkPolicy.KEEP,
                    request = OneTimeWorkRequestBuilder<OwnerNotificationReadWorker>()
                        .setInputData(workDataOf("notificationId" to id))
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build(),
                )
        }
    }
}
