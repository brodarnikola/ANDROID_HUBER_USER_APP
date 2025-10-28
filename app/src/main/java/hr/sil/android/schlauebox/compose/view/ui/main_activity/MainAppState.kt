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
    const val TERMS_AND_CONDITION_SCREEN = "TermsAndConditionScreen"
    const val HELP_SCREEN = "HelpScreen"
    const val ACCESS_SHARING_SCREEN = "AccessSharingScreen"
    const val ACCESS_SHARING_ADD_USER_SCREEN = "AccessSharingAddUserScreen"
    const val HELP_CONTENT_SCREEN = "HelpContentScreen"
    const val SETTINGS = "Settings"
    const val DEVICE_DETAILS = "DeviceDetails"
    const val PARCEL_PICKUP = "ParcelPickup"
}

object NavArguments {
    const val DEVICE_ID = "deviceId"
    const val MAC_ADDRESS = "macAddress"
    const val NAME_OF_DEVICE = "nameOfDevice"
    const val TITLE_HELP = "titleHelp"
    const val CONTENT_HELP = "contentHelp"
    const val PICTURE_POSITION = "picturePosition"
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

    fun navigateToDeviceDetails(route: String, deviceId: String, nameOfDevice: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$deviceId/$nameOfDevice") {
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

    fun goToHelp(route: String ) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun goToHelpContent(route: String ) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun goToAccessSharing(route: String, macAddress: String, nameOfDevice: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$macAddress/$nameOfDevice") {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun goToAccessSharingAddUser(route: String, macAddress: String, nameOfDevice: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$macAddress/$nameOfDevice") {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun goToAccessSharingForgetPreviousScreen(route: String, macAddress: String, nameOfDevice: String) {
        if (route != currentRoute) {
            navController.navigate("$route/$macAddress/$nameOfDevice") {
                launchSingleTop = true
                restoreState = true
                popUpTo("$route/$macAddress/$nameOfDevice")
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


