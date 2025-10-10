
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
import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.FirstOnboardingScreen
import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.HorizontalPager
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
    ) {
//        FirstOnboardingScreen(
//            modifier = modifier,
//            nextScreen = nextScreen
//        )
        HorizontalPager(
            modifier = modifier
        )
//        FirstOnboardingScreen(
//            titleText = stringResource(R.string.intro_welcome_slide_title),
//            descriptionText = stringResource(R.string.intro_welcome_slide_content),
//            buttonText = stringResource(id = R.string.app_generic_next),
//            textAlign = TextAlign.Center,
//            firstImage = R.drawable.img_onboarding_welcome,
//            pageNumber = "1",
//            modifier = modifier,
//            nextScreen = nextScreen,
//            nextScreenRoute = SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route
//        )
    }

    composable(
        SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route,
    ) {
//        FirstOnboardingScreen(
//            modifier = modifier,
//            nextScreen = nextScreen
//        )
        FirstOnboardingScreen(
            titleText = stringResource(R.string.intro_welcome_slide_title),
            descriptionText = stringResource(R.string.intro_welcome_slide_content),
            buttonText = stringResource(id = R.string.app_generic_next),
            textAlign = TextAlign.Center,
            firstImage = R.drawable.img_onboarding_welcome,
            pageNumber = "1",
            modifier = modifier,
            nextScreen = nextScreen,
            nextScreenRoute = SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route
        )
    }

    composable(
        SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route,
    ) {
        SecondOnboardingScreen(
            titleText = stringResource(R.string.intro_pickup_slide_title),
            descriptionText = stringResource(R.string.intro_pickup_slide_content),
            buttonText = stringResource(id = R.string.app_generic_next),
            textAlign = TextAlign.Start,
            pageNumber = "2",
            firstImage = R.drawable.img_onboarding_send,
            secondeImage = R.drawable.schlauebox_logo_invert,
            modifier = modifier,
            nextScreen = nextScreen,
            nextScreenRoute = SignUpOnboardingSections.THIRD_ONBOARDING_SCREEN.route
        )
    }

    composable(
        SignUpOnboardingSections.THIRD_ONBOARDING_SCREEN.route,
    ) {
        SecondOnboardingScreen(
            titleText = stringResource(R.string.intro_key_sharing_slide_title),
            descriptionText = stringResource(R.string.intro_key_sharing_slide_content),
            buttonText = stringResource(id = R.string.app_generic_next),
            textAlign = TextAlign.Start,
            pageNumber = "3",
            firstImage = R.drawable.img_onboarding_key,
            secondeImage = R.drawable.schlauebox_logo_invert,
            modifier = modifier,
            nextScreen = nextScreen,
            nextScreenRoute = SignUpOnboardingSections.FOURTH_ONBOARDING_SCREEN.route
        )
    }

    composable(
        SignUpOnboardingSections.FOURTH_ONBOARDING_SCREEN.route,
    ) {
        SecondOnboardingScreen(
            titleText = stringResource(R.string.intro_pickup_slide_title),
            descriptionText = stringResource(R.string.intro_pickup_slide_content),
            buttonText = stringResource(id = R.string.app_generic_sign_in),
            textAlign = TextAlign.Start,
            pageNumber = "4",
            firstImage = R.drawable.img_onboarding_start,
            secondeImage = R.drawable.schlauebox_logo_invert,
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
