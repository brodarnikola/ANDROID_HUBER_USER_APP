package hr.sil.android.schlauebox.compose.view.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object MainDestinations {
    const val HOME = "Home"
    const val ALERTS = "Alerts"
    const val LOCATION = "Location"
    const val SETTINGS = "Settings"
    const val MOVIE_DETAILS = "MovieDetails"
    const val ANDROID_POSSIBILITIES = "Android possibilities"
    const val RX_JAVA_EXAMPLES = "RxJavaExamples"
    const val OBJECT_DETECTION = "ObjectDetection"
    const val ANIMATED_CARD = "AnimatedCard"
}

object NavArguments {
    const val MOVIE_ID = "movieId"
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

    fun navigateToMovieDetails(route: String, movieId: Long) {
        if (route != currentRoute) {
            navController.navigate("$route/$movieId") {
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

    fun navigateToRxJava3Examples(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
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


