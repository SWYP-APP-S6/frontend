package com.swyp.mangro.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
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
import com.swyp.mangro.data.consumer.notification.ConsumerNotificationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class ConsumerTokenWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        return try {
            if (dependencies.authStore().authKey.first() == null) return Result.success()
            if (FirebaseApp.initializeApp(applicationContext) == null) return Result.failure()
            val token = FirebaseMessaging.getInstance().token.await()
            val result = if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                dependencies.repository().registerToken(token).first()
            } else {
                dependencies.repository().deleteToken(token).first()
            }
            val error = result.exceptionOrNull()
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

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun authStore(): AuthStore
        fun repository(): ConsumerNotificationRepository
    }

    companion object {
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "consumer-fcm-token",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ConsumerTokenWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build(),
            )
        }
    }
}
