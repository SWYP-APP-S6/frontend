package com.swyp.mangro.navigation

import androidx.lifecycle.ViewModel
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import com.swyp.mangro.data.consumer.home.repository.ConsumerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive

private const val POLLING_INTERVAL_MILLIS = 5_000L

@HiltViewModel
class HoldCompletionViewModel @Inject constructor(
    private val homeRepository: ConsumerHomeRepository,
    private val holdRepository: HoldRepository,
) : ViewModel() {

    private var watchingHoldId: Long? = null

    private val _completedHoldId = Channel<Long>(Channel.BUFFERED)
    val completedHoldId = _completedHoldId.receiveAsFlow()

    suspend fun watch() {
        while (currentCoroutineContext().isActive) {
            checkActiveHold()
            delay(POLLING_INTERVAL_MILLIS.milliseconds)
        }
    }

    private suspend fun checkActiveHold() {
        val activeResult = homeRepository.fetchActiveHold().first()
        val activeHold = activeResult.getOrElse { return }

        if (activeHold != null) {
            watchingHoldId = activeHold.holdId
            return
        }

        val holdId = watchingHoldId ?: return
        val detailResult = holdRepository.fetchHold(holdId).first()
        val detail = detailResult.getOrElse { return }

        when (detail.status) {
            "COMPLETED" -> {
                watchingHoldId = null
                _completedHoldId.send(holdId)
            }
            "CANCELED", "EXPIRED" -> watchingHoldId = null
        }
    }
}
