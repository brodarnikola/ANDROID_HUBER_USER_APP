
package hr.sil.android.schlauebox.compose.view.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.components.HuberScaffold
import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.SecondOnboardingScreen
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpOnboardingApp(
    signUpOnboardingViewModel: SignUpOnboardingViewModel
) {
    //val selectedTheme = signUpOnboardingViewModel.selectedTheme.collectAsState(initial = null)

    val appState = rememberSignUpAppState()
    val navBackStackEntry =
        appState.navController.currentBackStackEntryAsState() // navController.currentBackStackEntryAsState()


    AppTheme {
        HuberScaffold(scaffoldState = appState.scaffoldState, modifier = Modifier.semantics {
            testTagsAsResourceId = true
        }) { innerPaddingModifier ->
            val modifier = Modifier
            // Box required because there is no background in transition moment when changing screens
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                NavHost(
                    navController = appState.navController,
                    startDestination = SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route,
                    modifier = Modifier.padding(innerPaddingModifier)
                ) {
                    navGraph(
                        navBackStackEntry = navBackStackEntry,
                        modifier = modifier,
                        nextScreen = appState::navigateToRoute,
                        goToFirstOnboardingScreen =  { route ->
                            //appState.navigateToAnimatedCreditCard(route = route)
                        },
                        goToSecondOnboardingScreen =  { route ->
                            //appState.navigateToMovieDetails(route = route, movieId = movieId)
                        },
                        navigateUp = {
                            appState.upPress()
                        }
                    )
                }
            }
        }
    }
}

fun NavGraphBuilder.navGraph(
    navBackStackEntry: State<NavBackStackEntry?>,
    modifier: Modifier,
    nextScreen: (route: String) -> Unit,
    goToFirstOnboardingScreen: (route: String ) -> Unit,
    goToSecondOnboardingScreen: (route: String) -> Unit,
    navigateUp:() -> Unit
) {
    composable(
        SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route,
//        popEnterTransition = {
//            slideInHorizontally(initialOffsetX = { -1800 })
//        }
    ) {
//        FirstOnboardingScreen(
//            modifier = modifier,
//            nextScreen = nextScreen
//        )
        SecondOnboardingScreen(
            titleText = stringResource(R.string.login_title),
            descriptionText = stringResource(R.string.login_title),
            buttonText = stringResource(id = R.string.login_title),
            textAlign = TextAlign.Center,
            firstImage = R.drawable.bg_home,
            modifier = modifier,
            nextScreen = nextScreen,
            nextScreenRoute = SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route
        )
    }

    composable(
        SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route,
//        popEnterTransition = {
//            slideInHorizontally(initialOffsetX = { -1800 })
//        }
    ) {
        SecondOnboardingScreen(
            titleText = stringResource(R.string.login_title),
            descriptionText = stringResource(R.string.login_title),
            buttonText = stringResource(id = R.string.login_title),
            textAlign = TextAlign.Start,
            pageNumber = "1",
            firstImage = R.drawable.bg_home,
            secondeImage = R.drawable.bg_home,
            modifier = modifier,
            nextScreen = nextScreen,
            nextScreenRoute = SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route
        )
    }

//    composable(SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route) {
//        MoviesScreen(
//            viewModel = hiltViewModel(), // viewModel,
//            onMovieClick = { movieId ->
//                if (navBackStackEntry.value?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
//                    goToMovieDetails(MainDestinations.MOVIE_DETAILS,  movieId)
//                }
//            })
//    }
//    composable(MainDestinations.ALERTS) {
//        AlertsScreen(viewModel = hiltViewModel())
//    }
//    composable(
//        SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route
//    ) {
//        AnimatedCard( )
//    }
//

}

//private fun NavGraphBuilder.navGraph(
//    upPress: () -> Unit,
//    modifier: Modifier,
//    nextScreen: (route: String) -> Unit,
//    navigateUp: (route: String) -> Unit,
//    navigateUpWhatsApp: () -> Unit,
//    signUpOnboardingViewModel: SignUpOnboardingViewModel,
//) {
//    navigation(
//        route = SignUpOnboardingDestinations.ONBOARDING_ROUTE,
//        startDestination = SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route
//    ) {
//        addSignUpOnboardingGraph(
//            upPress,
//            modifier,
//            nextScreen,
//            navigateUp,
//            navigateUpWhatsApp,
//            signUpOnboardingViewModel
//        )
//    }
//}
