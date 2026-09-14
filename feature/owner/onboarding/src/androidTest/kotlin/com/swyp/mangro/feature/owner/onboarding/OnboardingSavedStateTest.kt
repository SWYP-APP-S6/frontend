package com.swyp.mangro.feature.owner.onboarding

import android.annotation.SuppressLint
import android.os.Parcel
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.createGraph
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreRegistrationModel
import com.swyp.mangro.feature.owner.onboarding.navigation.OnboardingGraph
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoState
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoViewModel
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

@SuppressLint("RestrictedApi") // Test the actual SavedStateHandle persistence format across a Parcel boundary.
class OnboardingSavedStateTest {
    @Test
    fun nestedObjectsAndLatestSnapshotSurviveSavedStateRoundTrip() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val original = StoreBasicInfoModel("가게 / & ?", StoreCategoryModel("fruit", "과채류"), StoreAddressModel("03965", "서울 마포구 망원로 12"), "1층")
            val latest = original.copy(name = "수정한 가게", detailedAddress = "2층")
            val handle = SavedStateHandle()
            handle["basicInfo"] = original
            handle["basicInfo"] = latest
            handle["addressResult"] = latest.address
            val restored = roundTrip(handle)
            assertEquals(latest, restored.get<StoreBasicInfoModel>("basicInfo"))
            assertNotSame(latest, restored.get<StoreBasicInfoModel>("basicInfo"))
            assertEquals(latest.address, restored.get<StoreAddressModel>("addressResult"))
            restored["addressResult"] = null
            assertEquals(null, roundTrip(restored).get<StoreAddressModel>("addressResult"))
        }
    }

    @Test
    fun emptyBasicInfoSurvivesSavedStateRoundTrip() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val handle = SavedStateHandle(mapOf("basicInfo" to StoreBasicInfoModel()))
            assertEquals(StoreBasicInfoModel(), roundTrip(handle).get<StoreBasicInfoModel>("basicInfo"))
        }
    }

    @Test
    fun registrationWithBusinessDaysSurvivesSavedStateRoundTrip() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val state = OwnerOperatingInfoState(
                basicInfo = StoreBasicInfoModel("가게", StoreCategoryModel("fruit", "과채류"), StoreAddressModel("03965", "서울 마포구 망원로 12")),
                phoneNumber = "02-1234-5678",
                openingMinutes = 540,
                closingMinutes = 1200,
                businessDays = persistentSetOf(1, 3, 5),
            )
            val registration = state.registration
            val handle = SavedStateHandle(mapOf("registration" to registration))
            assertEquals(registration, roundTrip(handle).get<StoreRegistrationModel>("registration"))
        }
    }

    @Test
    fun viewModelInitializesFromRestoredArgumentsAndDraft() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val latest = StoreBasicInfoModel("수정한 가게", StoreCategoryModel("fruit", "과채류"), StoreAddressModel("03965", "서울 마포구 망원로 12"))
            val draft = OwnerOperatingInfoState(
                basicInfo = latest.copy(name = "이전 가게"),
                openingMinutes = 540,
                closingMinutes = 1200,
                businessDays = persistentSetOf(1, 3),
            )
            val handle = roundTrip(SavedStateHandle(mapOf(Constants.BASIC_INFO to latest, "registration" to draft.registration)))
            val store = ViewModelStore()
            try {
                val model = OwnerOperatingInfoViewModel(handle)
                store.put("operating", model)
                assertEquals(latest, model.uiState.value.basicInfo)
                assertEquals(540, model.uiState.value.openingMinutes)
                assertEquals(1200, model.uiState.value.closingMinutes)
                assertEquals(setOf(1, 3), model.uiState.value.businessDays)
            } finally {
                store.clear()
            }
        }
    }

    @Test
    fun navigationArgumentsInitializeScreenScopedViewModel() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val owner = object : LifecycleOwner {
                override val lifecycle = LifecycleRegistry(this).apply { currentState = Lifecycle.State.RESUMED }
            }
            val store = ViewModelStore()
            try {
                val navController = NavHostController(InstrumentationRegistry.getInstrumentation().targetContext).apply {
                    setViewModelStore(store)
                    setLifecycleOwner(owner)
                    navigatorProvider.addNavigator(ComposeNavigator())
                    graph = createGraph(startDestination = OnboardingGraph) {
                        navigation<OnboardingGraph>(startDestination = OwnerBasicInfoDestination) {
                            composable<OwnerBasicInfoDestination> {}
                            composable<OwnerOperatingInfoDestination> {}
                        }
                    }
                }
                val factory = viewModelFactory {
                    initializer { OwnerBasicInfoViewModel(createSavedStateHandle()) }
                    initializer { OwnerOperatingInfoViewModel(createSavedStateHandle()) }
                }
                val basicEntry = requireNotNull(navController.currentBackStackEntry)
                val basicModel = ViewModelProvider(basicEntry, factory)[OwnerBasicInfoViewModel::class.java]
                val destination = requireNotNull(basicEntry.destination.parent?.findNode<OwnerOperatingInfoDestination>())
                val original = StoreBasicInfoModel("첫 가게", StoreCategoryModel("fruit", "과채류"), StoreAddressModel("03965", "서울 마포구 망원로 12"))
                navController.navigate(destination.id, bundleOf(Constants.BASIC_INFO to original))
                val operatingEntry = requireNotNull(navController.currentBackStackEntry)
                val first = ViewModelProvider(operatingEntry, factory)[OwnerOperatingInfoViewModel::class.java]
                assertEquals(original, first.uiState.value.basicInfo)
                navController.popBackStack()
                assertEquals(basicModel, ViewModelProvider(requireNotNull(navController.currentBackStackEntry), factory)[OwnerBasicInfoViewModel::class.java])
                val edited = original.copy(name = "수정한 가게")
                navController.navigate(destination.id, bundleOf(Constants.BASIC_INFO to edited))
                val second = ViewModelProvider(requireNotNull(navController.currentBackStackEntry), factory)[OwnerOperatingInfoViewModel::class.java]
                assertNotSame(first, second)
                assertEquals(edited, second.uiState.value.basicInfo)
            } finally {
                store.clear()
                owner.lifecycle.currentState = Lifecycle.State.DESTROYED
            }
        }
    }

    private fun roundTrip(handle: SavedStateHandle): SavedStateHandle {
        val parcel = Parcel.obtain()
        try {
            parcel.writeBundle(handle.savedStateProvider().saveState())
            val bytes = parcel.marshall()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val bundle = requireNotNull(parcel.readBundle(StoreBasicInfoModel::class.java.classLoader))
            return SavedStateHandle.createHandle(bundle, null)
        } finally {
            parcel.recycle()
        }
    }
}
