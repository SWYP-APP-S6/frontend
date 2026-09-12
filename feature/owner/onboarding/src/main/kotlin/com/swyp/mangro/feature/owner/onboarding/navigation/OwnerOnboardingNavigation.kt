package com.swyp.mangro.feature.owner.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.swyp.mangro.feature.owner.onboarding.Constants.BASIC_INFO
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.screen.address.AddressSearchDestination
import com.swyp.mangro.feature.owner.onboarding.screen.address.AddressSearchWebView
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoRoute
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDestination
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoRoute
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoViewModel
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
                val owner = remember(entry) {
                    navController.getBackStackEntry(OnboardingGraph)
                }

                val viewModel = hiltViewModel<OwnerBasicInfoViewModel>(owner)
                LaunchedEffect(entry, viewModel) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        entry.savedStateHandle.getStateFlow<StoreAddressModel?>(ADDRESS_RESULT, null).collect { result ->
                            if (result != null) {
                                viewModel.handleAction(OwnerBasicInfoAction.AddressSelected(result))
                                entry.savedStateHandle[ADDRESS_RESULT] = null
                            }
                        }
                    }
                }

                OwnerBasicInfoRoute(
                    navigateNext = { basicInfo ->
                        owner.savedStateHandle[BASIC_INFO] = basicInfo
                        navController.navigate(OwnerOperatingInfoDestination) { launchSingleTop = true }
                    },
                    navigateToAddressSearch = {
                        navController.navigate(AddressSearchDestination) { launchSingleTop = true }
                    },
                    navigateBack = onBack,
                    viewModel = viewModel,
                )
            }

            composable<OwnerOperatingInfoDestination> { entry ->
                val owner = remember(entry) { navController.getBackStackEntry(OnboardingGraph) }

                OwnerOperatingInfoRoute(
                    onComplete = onComplete,
                    navigateBack = { navController.popBackStack() },
                    viewModel = hiltViewModel<OwnerOperatingInfoViewModel, OwnerOperatingInfoViewModel.Factory>(owner) { factory ->
                        factory.create(owner.savedStateHandle)
                    },
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
