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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hr.sil.android.schlauebox.compose.view.ui.help.HelpContentScreen
import hr.sil.android.schlauebox.compose.view.ui.help.HelpScreen
import hr.sil.android.schlauebox.compose.view.ui.home_screens.DeviceDetailsScreen
import hr.sil.android.schlauebox.compose.view.ui.home_screens.NavHomeScreen
import hr.sil.android.schlauebox.compose.view.ui.pickup_parcel.PickupParcelScreen
import kotlin.collections.forEachIndexed


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainComposeApp(appState: MainAppState, navBackStackEntry: State<NavBackStackEntry?>) {

//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        //color = MaterialTheme.colorScheme.background
//    ) {

//        Scaffold(
//            bottomBar = {
//                if (showBottomBar.value) {
//                    TabView(
//                        bottomNavigationItems,
//                        navBackStackEntry)
//                    { route ->
//                        Log.d("MENU", "route is: $route")
//                        appState.navigateToRoute(route)
//                    }
//                }
//            },
//        ) { paddingValues ->
            NavHost(
                navController = appState.navController,
                startDestination = MainDestinations.HOME,
                //modifier = Modifier.padding(paddingValues)
            ) {
                mainNavGraph(
                    navBackStackEntry = navBackStackEntry,
                    goToPickup =  { route, macAddress ->
                        appState.goToPickup(route = route, macAddress)
                    },
                    goToDeviceDetails =  { route, deviceId ->
                        appState.navigateToDeviceDetails(route = route, deviceId = deviceId)
                    },
                    goToHelp = {
                        appState.goToHelp(it)
                    },
                    goToHelpContent = {
                        appState.goToHelpContent(it)
                    },
                    navigateUp = {
                        appState.upPress()
                    }
                )
            }
        //}
    //}
}

fun NavGraphBuilder.mainNavGraph(
    navBackStackEntry: State<NavBackStackEntry?>,
    goToDeviceDetails: (route: String, deviceId: String) -> Unit,
    goToPickup: (route: String, macAddress: String) -> Unit,
    goToHelp: (route: String) -> Unit,
    goToHelpContent: (route: String ) -> Unit,
    navigateUp:() -> Unit
) {
    composable(MainDestinations.HOME) {
        NavHomeScreen(
            viewModel = hiltViewModel(), // viewModel,
            onDeviceClick = { deviceId ->
                if (navBackStackEntry.value?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    goToDeviceDetails(MainDestinations.DEVICE_DETAILS, deviceId)
                }
            }
//            onMovieClick = { movieId ->
//                if (navBackStackEntry.value?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
//                    goToMovieDetails(MainDestinations.MOVIE_DETAILS,  movieId)
//                }
//            }
        )
    }
    composable(
        "${MainDestinations.DEVICE_DETAILS}/{${NavArguments.DEVICE_ID}}",
        arguments = listOf(navArgument(NavArguments.DEVICE_ID) {
            type = NavType.StringType
        })
    ) {
        DeviceDetailsScreen(
            macAddress = it.arguments?.getString(NavArguments.DEVICE_ID) ?: "",
            nameOfDevice = "",
            viewModel = hiltViewModel(),
            onNavigateToPickup = { macAddress ->
                goToPickup(MainDestinations.PARCEL_PICKUP, macAddress)
            },
            onNavigateToHelp = {
                goToHelp(MainDestinations.HELP_SCREEN)
            }
//            navigateUp = {
//                navigateUp()
//            }
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
@Composable
fun TabView(
    tabBarItems: List<BottomNavigationBarItem>,
    navBackStackEntry: State<NavBackStackEntry?>,
    goToNextScreen: (route: String) -> Unit
) {

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { _, tabBarItem ->
            NavigationBarItem(
                selected = tabBarItem.title == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                onClick = {
                    goToNextScreen(tabBarItem.title)
                },
                icon = {
                    TabBarIconView(
                        isSelected = tabBarItem.title == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = { Text(tabBarItem.title) })
        }
    }
}

// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = {
        TabBarBadgeView(badgeAmount)
    }) {
        Icon(
            imageVector = if (isSelected) {
                selectedIcon
            } else {
                unselectedIcon
            },
            contentDescription = title
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


