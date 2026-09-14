package com.swyp.mangro.feature.owner.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navOptions
import com.swyp.mangro.feature.owner.onboarding.Constants.BASIC_INFO
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.screen.address.AddressSearchDestination
import com.swyp.mangro.feature.owner.onboarding.screen.address.AddressSearchWebView
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoRoute
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoRoute
import kotlinx.serialization.Serializable

private const val ADDRESS_RESULT = "onboarding_address_result"

@Serializable
data object OnboardingGraph

@Composable
fun OwnerOnboardingNavigation(
    navController: NavHostController,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    NavHost(navController = navController, startDestination = OnboardingGraph) {
        navigation<OnboardingGraph>(startDestination = OwnerBasicInfoDestination) {
            composable<OwnerBasicInfoDestination> { entry ->
                val addressResult by entry.savedStateHandle
                    .getStateFlow<StoreAddressModel?>(ADDRESS_RESULT, null)
                    .collectAsStateWithLifecycle(lifecycleOwner = entry, minActiveState = Lifecycle.State.RESUMED)

                OwnerBasicInfoRoute(
                    navigateNext = { basicInfo ->
                        val destination = requireNotNull(entry.destination.parent?.findNode<OwnerOperatingInfoDestination>())

                        navController.navigate(
                            destination.id,
                            bundleOf(BASIC_INFO to basicInfo),
                            navOptions { launchSingleTop = true },
                        )
                    },
                    navigateToAddressSearch = {
                        navController.navigate(AddressSearchDestination) { launchSingleTop = true }
                    },
                    navigateBack = onBack,
                    addressResult = addressResult,
                    onAddressResultConsumed = { entry.savedStateHandle[ADDRESS_RESULT] = null },
                )
            }

            composable<OwnerOperatingInfoDestination> {
                OwnerOperatingInfoRoute(
                    onComplete = onComplete,
                    navigateBack = { navController.popBackStack() },
                )
            }

            composable<AddressSearchDestination> {
                AddressSearchWebView(
                    onDismiss = { navController.popBackStack() },
                    onSelect = { address ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(ADDRESS_RESULT, address)
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
