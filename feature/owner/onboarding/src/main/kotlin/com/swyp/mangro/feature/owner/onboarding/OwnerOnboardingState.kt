package com.swyp.mangro.feature.owner.onboarding

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class OwnerOnboardingState internal constructor(
    val name: TextFieldState,
    val detailAddress: TextFieldState,
    val phone: TextFieldState,
) {
    var step by mutableIntStateOf(1)
        internal set
    var category by mutableStateOf<StoreCategory?>(null)
    var address by mutableStateOf<StoreAddress?>(null)
    var openingMinutes by mutableStateOf<Int?>(null)
    var closingMinutes by mutableStateOf<Int?>(null)

    /** Sunday = 0, Saturday = 6. */
    var businessDays by mutableStateOf<Set<Int>>(emptySet())

    fun registration(): StoreRegistration = StoreRegistration(
        name = name.text.toString().trim(),
        category = category,
        address = address,
        detailAddress = detailAddress.text.toString().trim(),
        phone = phone.text.toString().filter(Char::isDigit),
        openingMinutes = openingMinutes,
        closingMinutes = closingMinutes,
        businessDays = businessDays.toSet(),
    )

    internal companion object {
        val Saver = listSaver<OwnerOnboardingState, Any>(
            save = {
                listOf(
                    it.name.text.toString(), it.detailAddress.text.toString(), it.phone.text.toString(),
                    it.step, it.category?.id.orEmpty(), it.category?.label.orEmpty(),
                    it.address?.postalCode.orEmpty(), it.address?.address.orEmpty(),
                    it.openingMinutes ?: -1, it.closingMinutes ?: -1,
                    it.businessDays.fold(0) { mask, day -> mask or (1 shl day) },
                )
            },
            restore = {
                OwnerOnboardingState(TextFieldState(it[0] as String), TextFieldState(it[1] as String), TextFieldState(it[2] as String)).apply {
                    step = it[3] as Int
                    category = (it[4] as String).takeIf(String::isNotEmpty)?.let { id -> StoreCategory(id, it[5] as String) }
                    address = (it[6] as String).takeIf(String::isNotEmpty)?.let { code -> StoreAddress(code, it[7] as String) }
                    openingMinutes = (it[8] as Int).takeIf { minutes -> minutes >= 0 }
                    closingMinutes = (it[9] as Int).takeIf { minutes -> minutes >= 0 }
                    businessDays = (0..6).filter { day -> (it[10] as Int) and (1 shl day) != 0 }.toSet()
                }
            },
        )
    }
}

@Composable
fun rememberOwnerOnboardingState(): OwnerOnboardingState = rememberSaveable(saver = OwnerOnboardingState.Saver) {
    OwnerOnboardingState(TextFieldState(), TextFieldState(), TextFieldState())
}
