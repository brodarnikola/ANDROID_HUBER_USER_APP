package hr.sil.android.schlauebox.compose.view.ui.main_activity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hr.sil.android.schlauebox.compose.view.ui.access_sharing.AccessSharingAddUserScreen
import hr.sil.android.schlauebox.compose.view.ui.access_sharing.AccessSharingScreen
import hr.sil.android.schlauebox.compose.view.ui.help.HelpContentScreen
import hr.sil.android.schlauebox.compose.view.ui.help.HelpScreen
import hr.sil.android.schlauebox.compose.view.ui.home_screens.DeviceDetailsScreen
import hr.sil.android.schlauebox.compose.view.ui.home_screens.NavHomeScreen
import hr.sil.android.schlauebox.compose.view.ui.home_screens.SettingsScreen
import hr.sil.android.schlauebox.compose.view.ui.pickup_parcel.PickupParcelScreen
import kotlin.collections.forEachIndexed

import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.home_screens.TccScreen
import hr.sil.android.schlauebox.compose.view.ui.send_parcel.SelectParcelSizeScreen
import hr.sil.android.schlauebox.compose.view.ui.send_parcel.SendParcelDeliveryScreen
import hr.sil.android.schlauebox.compose.view.ui.send_parcel.SendParcelsOverviewScreen


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainComposeApp(appState: MainAppState, navBackStackEntry: State<NavBackStackEntry?>) {
    NavHost(
        navController = appState.navController,
        startDestination = MainDestinations.HOME,
        //modifier = Modifier.padding(paddingValues)
    ) {
        mainNavGraph(
            navBackStackEntry = navBackStackEntry,
            goToPickup = { route, macAddress ->
                appState.goToPickup(route = route, macAddress)
            },
            goToDeviceDetails = { route, deviceId, nameOfDevice ->
                appState.navigateToDeviceDetails(
                    route = route,
                    deviceId = deviceId,
                    nameOfDevice = nameOfDevice
                )
            },
            goToHelp = {
                appState.goToHelp(it)
            },
            goToHelpContent = {
                appState.goToHelpContent(it)
            },
            goToAccessSharing = { route, macAddress, nameOfDevice ->
                appState.goToAccessSharing(
                    route = route,
                    macAddress = macAddress,
                    nameOfDevice = nameOfDevice
                )
            },
            goToAccessSharingAddUser = { route, macAddress, nameOfDevice ->
                appState.goToAccessSharingAddUser(
                    route = route,
                    macAddress = macAddress,
                    nameOfDevice = nameOfDevice
                )
            },
            goToAccessSharingForgetPreviousScreen = { route, macAddress, nameOfDevice ->
                appState.goToAccessSharingForgetPreviousScreen(
                    route = route,
                    macAddress = macAddress,
                    nameOfDevice = nameOfDevice
                )
            },
            goToSelectParcelSize = { route, macAddress ->
                appState.goToSelectParcelSize(route, macAddress)
            },
            goToSendParcelOverview = { route, macAddress ->
                appState.goToSendParcelOverview(route, macAddress)
            },
            goToSendParcelSize = { route, macAddress, pin, size ->
                appState.goToSendParcelSize(route, macAddress, pin, size)
            },
            navigateUp = {
                appState.upPress()
            }
        )
    }
}

fun NavGraphBuilder.mainNavGraph(
    navBackStackEntry: State<NavBackStackEntry?>,
    goToDeviceDetails: (route: String, deviceId: String, nameOfDevice: String) -> Unit,
    goToPickup: (route: String, macAddress: String) -> Unit,
    goToHelp: (route: String) -> Unit,
    goToHelpContent: (route: String) -> Unit,
    goToAccessSharing: (route: String, macAddress: String, nameOfDevice: String) -> Unit,
    goToAccessSharingAddUser: (route: String, macAddress: String, nameOfDevice: String) -> Unit,
    goToAccessSharingForgetPreviousScreen: (route: String, macAddress: String, nameOfDevice: String) -> Unit,
    goToSelectParcelSize: (route: String, macAddress: String) -> Unit,
    goToSendParcelSize: (route: String, macAddress: String, pin: Int, size: String) -> Unit,
    goToSendParcelOverview: (route: String, macAddress: String) -> Unit,
    navigateUp: () -> Unit
) {
    composable(MainDestinations.HOME) {
        NavHomeScreen(
            viewModel = hiltViewModel(), // viewModel,
            onDeviceClick = { deviceId, nameOfDevice ->
                if (navBackStackEntry.value?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    goToDeviceDetails(MainDestinations.DEVICE_DETAILS, deviceId, nameOfDevice)
                }
            }
        )
    }

    composable(MainDestinations.SETTINGS) {
        SettingsScreen(
            viewModel = hiltViewModel()
        )
    }

    composable(MainDestinations.TERMS_AND_CONDITION_SCREEN) {
        TccScreen(
            viewModel = hiltViewModel()
        )
    }

    composable(
        "${MainDestinations.SELECT_PARCEL_OVERVIEW}/{${NavArguments.MAC_ADDRESS}}",
        arguments = listOf(
            navArgument(NavArguments.MAC_ADDRESS) {
                type = NavType.StringType
            }
        )
    ) {
        SendParcelsOverviewScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            viewModel = hiltViewModel(),
        )
    }

    composable(
        "${MainDestinations.DEVICE_DETAILS}/{${NavArguments.DEVICE_ID}}/{${NavArguments.NAME_OF_DEVICE}}",
        arguments = listOf(
            navArgument(NavArguments.DEVICE_ID) {
                type = NavType.StringType
            },
            navArgument(NavArguments.NAME_OF_DEVICE) {
                type = NavType.StringType
            }
        )
    ) {
        DeviceDetailsScreen(
            macAddress = it.arguments?.getString(NavArguments.DEVICE_ID) ?: "",
            nameOfDevice = it.arguments?.getString(NavArguments.NAME_OF_DEVICE) ?: "",
            viewModel = hiltViewModel(),
            onNavigateToPickup = { macAddress ->
                goToPickup(MainDestinations.PARCEL_PICKUP, macAddress)
            },
            onNavigateToHelp = {
                goToHelp(MainDestinations.HELP_SCREEN)
            },
            onNavigateToSelectParcelSize = { macAddress ->
                goToSelectParcelSize(MainDestinations.SELECT_PARCEL_SIZE, macAddress)
            },
            onNavigateToSendParcelsOverviewActivity = { macAddress ->
                goToSendParcelOverview(MainDestinations.SELECT_PARCEL_OVERVIEW, macAddress)
            },
            onNavigateToAccessSharing = { macAddress, nameOfDevice ->
                goToAccessSharing(MainDestinations.ACCESS_SHARING_SCREEN, macAddress, nameOfDevice)
            }
        )
    }

    composable(
        "${MainDestinations.SELECT_PARCEL_SIZE}/{${NavArguments.MAC_ADDRESS}}",
        arguments = listOf(navArgument(NavArguments.MAC_ADDRESS) {
            type = NavType.StringType
        })
    ) {
        SelectParcelSizeScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            viewModel = hiltViewModel(),
            onNavigateToDelivery = { macAddress, pin, size ->
                goToSendParcelSize(
                    MainDestinations.SEND_PARCEL_SIZE,
                    macAddress,
                    pin,
                    size
                )
            }
        )
    }

    composable(
        "${MainDestinations.SEND_PARCEL_SIZE}/{${NavArguments.MAC_ADDRESS}}/{${NavArguments.PIN_OF_DEVICE}}/{${NavArguments.SIZE_OF_DEVICE}}",
        arguments = listOf(
            navArgument(NavArguments.MAC_ADDRESS) {
                type = NavType.StringType
            },
            navArgument(NavArguments.PIN_OF_DEVICE) {
                type = NavType.IntType
            },
            navArgument(NavArguments.SIZE_OF_DEVICE) {
                type = NavType.StringType
            }
        )
    ) {
        SendParcelDeliveryScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            pin = it.arguments?.getInt(NavArguments.PIN_OF_DEVICE) ?: 0,
            size = it.arguments?.getString(NavArguments.SIZE_OF_DEVICE) ?: "",
            viewModel = hiltViewModel()
        )
    }

    composable(
        "${MainDestinations.PARCEL_PICKUP}/{${NavArguments.MAC_ADDRESS}}",
        arguments = listOf(navArgument(NavArguments.MAC_ADDRESS) {
            type = NavType.StringType
        })
    ) {
        PickupParcelScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            viewModel = hiltViewModel(),
            onFinish = {
                navigateUp()
            }
        )
    }

    composable(
        "${MainDestinations.ACCESS_SHARING_SCREEN}/{${NavArguments.MAC_ADDRESS}}/{${NavArguments.NAME_OF_DEVICE}}",
        arguments = listOf(
            navArgument(NavArguments.MAC_ADDRESS) {
                type = NavType.StringType
            },
            navArgument(NavArguments.NAME_OF_DEVICE) {
                type = NavType.StringType
            }
        )
    ) {
        AccessSharingScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            nameOfDevice = it.arguments?.getString(NavArguments.NAME_OF_DEVICE) ?: "CHANGE_THIS",
            viewModel = hiltViewModel(),
            onNavigateToAddUser = { macAddress, nameOfDevice ->
                goToAccessSharingAddUser(
                    MainDestinations.ACCESS_SHARING_ADD_USER_SCREEN,
                    macAddress,
                    nameOfDevice
                )
            }
        )
    }

    composable(
        "${MainDestinations.ACCESS_SHARING_ADD_USER_SCREEN}/{${NavArguments.MAC_ADDRESS}}/{${NavArguments.NAME_OF_DEVICE}}",
        arguments = listOf(
            navArgument(NavArguments.MAC_ADDRESS) {
                type = NavType.StringType
            },
            navArgument(NavArguments.NAME_OF_DEVICE) {
                type = NavType.StringType
            }
        )
    ) {
        AccessSharingAddUserScreen(
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
            nameOfDevice = it.arguments?.getString(NavArguments.NAME_OF_DEVICE) ?: "CHANGE_THIS",
            viewModel = hiltViewModel(),
            navigateToAccessSharingActivity = { macAddress, nameOfDevice ->
                goToAccessSharingForgetPreviousScreen(
                    MainDestinations.ACCESS_SHARING_SCREEN,
                    macAddress,
                    nameOfDevice
                )
            }
        )
    }

    composable(
        MainDestinations.HELP_SCREEN
    ) {
        HelpScreen(
            viewModel = hiltViewModel(),
            onNavigateToHelpContent = { titleResId, contentResId, picturePosition ->
                goToHelpContent("${MainDestinations.HELP_CONTENT_SCREEN}/$titleResId/$contentResId/$picturePosition")
            }
        )
    }

    composable(
        "${MainDestinations.HELP_CONTENT_SCREEN}/{${NavArguments.TITLE_HELP}}/{${NavArguments.CONTENT_HELP}}/{${NavArguments.PICTURE_POSITION}}",
        arguments = listOf(navArgument(NavArguments.TITLE_HELP) {
            type = NavType.IntType
        }, navArgument(NavArguments.CONTENT_HELP) {
            type = NavType.IntType
        }, navArgument(NavArguments.PICTURE_POSITION) {
            type = NavType.IntType
        }
        )
    ) {
        val titleResId = it.arguments?.getInt(NavArguments.TITLE_HELP) ?: 0
        val contentResId = it.arguments?.getInt(NavArguments.CONTENT_HELP) ?: 0
        val picturePosition = it.arguments?.getInt(NavArguments.PICTURE_POSITION) ?: 0

        HelpContentScreen(
            titleResId,
            contentResId,
            picturePosition,
            viewModel = hiltViewModel()
        )
    }

//
//    composable(MainDestinations.SETTINGS) {
//        SettingsScreen(viewModel = hiltViewModel())
//    }

//    composable(
//        MainDestinations.ANIMATED_CARD
//    ) {
//        AnimatedCard( )
//    }

}


// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
@Composable
fun TabView(
    tabBarItems: List<BottomNavigationBarItem>,
    navBackStackEntry: State<NavBackStackEntry?>,
    goToNextScreen: (route: String) -> Unit
) {

    NavigationBar(
        containerColor = colorResource(R.color.colorWhite)
    ) {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { _, tabBarItem ->
            NavigationBarItem(
                selected = tabBarItem.route == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                onClick = {
                    goToNextScreen(tabBarItem.route)
                },
                icon = {
                    TabBarIconView(
                        isSelected = tabBarItem.route == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                        icon = tabBarItem.icon,
                        title = tabBarItem.route,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(R.color.colorPrimary),
                    unselectedIconColor = colorResource(R.color.colorGray),
                    indicatorColor = colorResource(R.color.colorPrimary).copy(alpha = 0.1f)
                )
            )
        }
    }
}

// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    icon: Int,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = {
        TabBarBadgeView(badgeAmount)
    }) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = if (isSelected) {
                colorResource(R.color.colorPrimary)
            } else {
                colorResource(R.color.colorGray)
            }
        )
    }
}

// This component helps to clean up the API call from our TabBarIconView above,
// but could just as easily be added inside the TabBarIconView without creating this custom component
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}
// end of the reusable components that can be copied over to any new projects
// ----------------------------------------


