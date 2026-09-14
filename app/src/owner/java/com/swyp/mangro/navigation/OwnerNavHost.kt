package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.home.navigation.ownerHomeNavGraph
import com.swyp.mangro.feature.owner.home.screen.OwnerHomePickupState
import com.swyp.mangro.feature.owner.onboarding.navigation.OnboardingGraph
import com.swyp.mangro.feature.owner.onboarding.navigation.ownerOnboardingNavGraph
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.ownerPickupNavGraph
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.list.ProductListTab
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerSettingDestination
import com.swyp.mangro.feature.owner.setting.navigation.ownerSettingNavGraph
import com.swyp.mangro.feature.splash.navigation.Splash
import com.swyp.mangro.feature.splash.navigation.splashNavGraph
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun OwnerNavHost(
    products: List<OwnerProductModel>,
    homePickups: OwnerHomePickupState,
    storeClosingTime: String,
    storeOpeningTime: String,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    onCompletePickup: (String) -> Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val destination = navController.currentBackStackEntryAsState().value?.destination
    HideNavigationBarWhileVisible(
        hidden = destination?.hasRoute<Splash>() == true || destination?.hasRoute<Login>() == true,
    )

    NavHost(navController, startDestination = Splash) {
        splashNavGraph(
            navigateToLogin = {
                navController.navigate(Login) {
                    popUpTo<Splash> { inclusive = true }
                    launchSingleTop = true
                }
            },
            navigateToHome = { navController.enterOwnerHome() },
        )
        authNavGraph(
            navController = navController,
            navigateToHome = { navController.enterOwnerHome() },
            navigateToOnboarding = {
                navController.navigate(OnboardingGraph) {
                    popUpTo<Login> { inclusive = true }
                    launchSingleTop = true
                }
            },
            navigateToPrivacyPolicy = {
                navController.navigate(OwnerPolicyDestination(OwnerPolicy.PRIVACY_POLICY)) { launchSingleTop = true }
            },
        )
        ownerOnboardingNavGraph(
            navController = navController,
            onComplete = {
                if (!navController.popBackStack<OwnerHomeDestination>(inclusive = false)) {
                    navController.enterOwnerHome()
                }
            },
            onBack = { navController.popBackStack() },
        )
        ownerHomeNavGraph(
            products = products.map {
                OwnerProduct(it.id, it.photos.firstOrNull().orEmpty(), it.name, it.salePrice, it.remainingQuantity, it.reservedQuantity, it.pickedUpQuantity)
            }.toPersistentList(),
            pickups = homePickups,
            navigateToStore = { navController.openOwnerTab(OwnerProductListDestination()) },
            navigateToProducts = { navController.openProductList() },
            navigateToSettings = { navController.openOwnerTab(OwnerSettingDestination) },
            navigateToRegisterStore = { navController.navigate(OnboardingGraph) { launchSingleTop = true } },
            navigateToProduct = { navController.navigate(OwnerProductDetailDestination(it)) },
            navigateToRegisterProduct = { navController.navigate(OwnerProductEditorDestination) },
            navigateToPickups = { navController.openProductList(ProductListTab.PICKUPS) },
            navigateToCompletedPickups = { navController.openProductList(ProductListTab.PICKUPS, ProductListFilter.COMPLETED) },
            navigateToExpiredPickups = { navController.openProductList(ProductListTab.PICKUPS, ProductListFilter.EXPIRED) },
            navigateToCancellations = { navController.navigate(OwnerPickupCancellationDestination()) },
            navigateToPickup = { navController.navigate(OwnerPickupDetailDestination(it)) },
            onCompletePickup = onCompletePickup,
        )
        ownerProductNavGraph(
            navController = navController,
            products = products,
            storeClosingTime = storeClosingTime,
            storeOpeningTime = storeOpeningTime,
            onSaveProducts = onSaveProducts,
            onCancelReservations = { productIds -> navController.navigate(OwnerPickupCancellationDestination(productIds)) },
            onMenuClick = { menu ->
                when (menu) {
                    OwnerMenu.HOME -> navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true)
                    OwnerMenu.STORE -> Unit
                    OwnerMenu.SETTINGS -> navController.openOwnerTab(OwnerSettingDestination)
                }
            },
            onPickupClick = { navController.navigate(OwnerPickupDetailDestination(it)) },
        )
        ownerSettingNavGraph(
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true) },
            navigateToProducts = { navController.openOwnerTab(OwnerProductListDestination()) },
            navigateToPolicy = { navController.navigate(OwnerPolicyDestination(it)) { launchSingleTop = true } },
            navigateBack = { navController.popBackStack() },
        )
        ownerPickupNavGraph(
            navigateBack = { navController.popBackStack() },
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false) },
        )
    }
}

private fun NavHostController.enterOwnerHome() {
    navigate(OwnerHomeDestination) {
        popUpTo(graph.id) { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavHostController.openOwnerTab(destination: Any) {
    navigate(destination) {
        popUpTo<OwnerHomeDestination> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** A shortcut explicitly selects a tab/filter instead of restoring an earlier selection. */
private fun NavHostController.openProductList(
    tab: ProductListTab = ProductListTab.PRODUCTS,
    filter: ProductListFilter = ProductListFilter.ALL,
) {
    clearBackStack<OwnerProductListDestination>()
    navigate(OwnerProductListDestination(tab, filter)) {
        popUpTo<OwnerHomeDestination> { inclusive = false }
        launchSingleTop = true
    }
}
