package hr.sil.android.schlauebox.compose.view.ui.main_activity


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
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
    val icon: Int,
    val badgeAmount: Int? = null
)


fun bottomNavigationItems(): List<BottomNavigationBarItem> {
    // setting up the individual tabs
    val homeTab = BottomNavigationBarItem(
        title = "Home",
        route = MainDestinations.HOME,
        icon = R.drawable.ic_bottom_home
    )
    val tcTab = BottomNavigationBarItem(
        title = "Privacy Policy",
        route = MainDestinations.TERMS_AND_CONDITION_SCREEN,
        icon = R.drawable.ic_bottom_tc
    )
    val settingsTab = BottomNavigationBarItem(
        title = "Settings",
        route = MainDestinations.SETTINGS,
        icon = R.drawable.ic_bottom_settings
    )

    // creating a list of all the tabs
    val tabBarItems = listOf(homeTab, tcTab, settingsTab)
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

    val currentRoute = navBackStackEntry.value?.destination?.route

    showBottomBar.value = when {
        currentRoute == null -> true
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.HOME) == true ||
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.TERMS_AND_CONDITION_SCREEN) == true ||
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.SETTINGS) == true  -> true
        else -> false
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val imageLogoPadding = if( !showBottomBar.value ) {
                        50.dp
                    } else {
                        20.dp
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(end = imageLogoPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.schlauebox_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.height(40.dp)
                        )
                    }
                },
                navigationIcon = {
                    if (!showBottomBar.value) {
                        IconButton(onClick = {
                            appState.upPress()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colorResource(R.color.colorBlack)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if(showBottomBar.value)
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

            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            MainComposeApp(appState, navBackStackEntry)

            // Overlays - shown in priority order
            when {
                !systemState.bluetoothAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_ble),
                        backgroundDrawable = R.drawable.bg_bluetooth
                    )
                }

                !systemState.networkAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_network),
                        backgroundDrawable = R.drawable.bg_wifi_internet
                    )
                }

                !systemState.locationGPSAvailable -> {
                    LocationGPSOverlay()
                }
            }

        }
    }
}

@Composable
fun SystemOverlay(
    message: String,
    backgroundDrawable: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundDrawable),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )

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
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rectangle_transparent_dark),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
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
                textAlign = TextAlign.Center
            )
        }
    }
}
