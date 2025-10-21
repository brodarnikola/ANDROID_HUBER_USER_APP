package hr.sil.android.schlauebox.compose.view.ui.main_activity


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import hr.sil.android.schlauebox.R


data class BottomNavigationBarItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)


fun bottomNavigationItems(): List<BottomNavigationBarItem> {
    // setting up the individual tabs
    val homeTab = BottomNavigationBarItem(
        title = "Home",
        route =  MainDestinations.HOME,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    val alertsTab = BottomNavigationBarItem(
        title = "Alerts",
        route =  MainDestinations.ALERTS,
        selectedIcon = Icons.Filled.Email,
        unselectedIcon = Icons.Outlined.Email,
        badgeAmount = 7
    )
    val locationTab = BottomNavigationBarItem(
        title = "Location",
        route =  MainDestinations.LOCATION,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    // creating a list of all the tabs
    val tabBarItems = listOf(homeTab, alertsTab, locationTab )
    return tabBarItems
}

// Main Composable with Overlays
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    systemStateViewModel: SystemStateViewModel,
    onNavigateToLogin: () -> Unit
) {
    val systemState by systemStateViewModel.systemState.collectAsState()

    val appState = rememberMainAppState()

    val bottomNavigationItems = bottomNavigationItems()

    val showBottomBar = rememberSaveable { mutableStateOf(true) }
    val navBackStackEntry =
        appState.navController.currentBackStackEntryAsState() // navController.currentBackStackEntryAsState()

    showBottomBar.value = when {
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.MOVIE_DETAILS) == true -> false
        else -> true
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.schlauebox_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.height(40.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
                TabView(
                    bottomNavigationItems,
                    navBackStackEntry)
                { route ->
                    Log.d("MENU", "route is: $route")
                    appState.navigateToRoute(route)
                }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            MainComposeApp(appState, navBackStackEntry)

            // Overlays - shown in priority order
            when {
                !systemState.bluetoothAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_ble),
                        backgroundColor = Color(0xFF1E88E5) // Blue for Bluetooth
                    )
                }

                !systemState.networkAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_network),
                        backgroundColor = Color(0xFFE53935) // Red for Network
                    )
                }

                !systemState.locationGPSAvailable -> {
                    LocationGPSOverlay()
                }
            }

            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        }
    }
}

@Composable
fun SystemOverlay(
    message: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LocationGPSOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_no_location_services),
                contentDescription = "No Location",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 20.dp)
            )

            Text(
                text = stringResource(R.string.no_location_services).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
