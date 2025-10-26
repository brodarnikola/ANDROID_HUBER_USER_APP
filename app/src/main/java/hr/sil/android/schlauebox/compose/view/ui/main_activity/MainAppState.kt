package hr.sil.android.schlauebox.compose.view.ui.main_activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlin.let

object MainDestinations {
    const val HOME = "Home"
    const val ALERTS = "Alerts"
    const val SETTINGS = "Settings"
    const val DEVICE_DETAILS = "DeviceDetails"
    const val PARCEL_PICKUP = "ParcelPickup"
}

object NavArguments {
    const val DEVICE_ID = "deviceId"
    const val MAC_ADDRESS = "macAddress"
}

@Composable
fun rememberMainAppState(
    navController: NavHostController = rememberNavController()
) =
    remember(navController) {
        MainAppState(navController)
    }

@Stable
class MainAppState(
    val navController: NavHostController
) {

    val currentRoute: String?
        get() = navController.currentDestination?.route

    fun upPress() {
        navController.currentBackStackEntry?.let {
            navController.navigateUp()
        }
    }

    fun navigateToDeviceDetails(route: String, deviceId: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$deviceId") {
                launchSingleTop = true
                restoreState = true
//                if (popPreviousScreen) {
//                    popUpTo(navController.currentBackStackEntry?.destination?.route ?: return@navigate) {
//                        inclusive = true
//                    }
//                }
            }
        }
    }

    fun goToPickup(route: String, macAddress: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$macAddress") {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun navigateToRoute(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    }
}


