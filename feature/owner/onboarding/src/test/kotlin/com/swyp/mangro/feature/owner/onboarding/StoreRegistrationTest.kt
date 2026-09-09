package com.swyp.mangro.feature.owner.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreRegistrationTest {
    private val valid = StoreRegistration(
        "청과마을",
        StoreCategory("fruit", "과채류"),
        StoreAddress("03965", "서울 마포구 망원로 12"),
        "",
        "021234567",
        540,
        1200,
        setOf(1, 2, 3, 4, 5),
    )

    @Test fun requiresBasicFieldsButAllowsEmptyDetail() {
        assertTrue(valid.isValid)
        assertFalse(valid.copy(name = "  ").isBasicInfoValid)
        assertFalse(valid.copy(category = null).isBasicInfoValid)
        assertFalse(valid.copy(address = null).isBasicInfoValid)
        assertFalse(valid.copy(address = StoreAddress("123", "서울")).isBasicInfoValid)
    }

    @Test fun acceptsLocalMobileAndInternetNumbers() {
        listOf("021234567", "0212345678", "01012345678", "07012345678", "0311234567").forEach {
            assertTrue(it, valid.copy(phone = it).isPhoneValid)
        }
        listOf("", "123", "1012345678", "010123456789", "02abcdefgh").forEach {
            assertFalse(it, valid.copy(phone = it).isPhoneValid)
        }
    }

    @Test fun rejectsEqualOvernightAndOutOfRangeTimes() {
        assertFalse(valid.copy(openingMinutes = null).isTimeValid)
        assertFalse(valid.copy(closingMinutes = 540).isTimeValid)
        assertFalse(valid.copy(closingMinutes = 60).isTimeValid)
        assertFalse(valid.copy(openingMinutes = -1).isTimeValid)
        assertFalse(valid.copy(closingMinutes = 1440).isTimeValid)
        assertTrue(valid.copy(openingMinutes = 0, closingMinutes = 1439).isTimeValid)
    }

    @Test fun requiresAtLeastOneValidBusinessDay() {
        assertFalse(valid.copy(businessDays = emptySet()).isValid)
        assertFalse(valid.copy(businessDays = setOf(7)).isValid)
        assertTrue(valid.copy(businessDays = setOf(0, 6)).isValid)
    }
}
