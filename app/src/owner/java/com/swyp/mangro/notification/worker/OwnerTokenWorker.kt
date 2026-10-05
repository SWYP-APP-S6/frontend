package com.swyp.mangro.notification.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.data.owner.notification.OwnerNotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

@HiltWorker
class OwnerTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authStore: AuthStore,
    private val notificationRepository: OwnerNotificationRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val authKey = runCatching {
            authStore.authKey.first()
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            return Result.retry()
        }

        if (authKey == null) return Result.success()

        val result = try {
            if (FirebaseApp.initializeApp(applicationContext) == null) return Result.failure()
            val token = FirebaseMessaging.getInstance().token.await()
            val throwable = if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                notificationRepository.registerToken(token).first()
            } else {
                notificationRepository.deleteToken(token).first()
            }.exceptionOrNull()

            when (throwable) {
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
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "owner-fcm-token",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<OwnerTokenWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build(),
            )
        }
    }
}
