
package hr.sil.android.schlauebox.compose.view.ui

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sunbird.ui.setup.login.ForgotPasswordScreen
import com.sunbird.ui.setup.login.ForgotPasswordUpdateScreen
import com.sunbird.ui.setup.login.ForgotPasswordUpdateViewModel
import com.sunbird.ui.setup.login.ForgotPasswordViewModel
import com.sunbird.ui.setup.login.LoginScreen
import com.sunbird.ui.setup.login.LoginViewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.components.HuberScaffold
import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.HorizontalPager
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme
import hr.sil.android.schlauebox.util.SettingsHelper


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
                val firstScreen = SettingsHelper.firstRun
                val routeFirstScreen = if (firstScreen) {
                    SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route
                }
                else {
                    SignUpOnboardingSections.LOGIN_SCREEN.route
                }
                NavigationStack(routeFirstScreen, modifier)
//                NavHost(
//                    navController = appState.navController,
//                    startDestination = routeFirstScreen,
//                    modifier = Modifier.padding(innerPaddingModifier)
//                ) {
//                    navGraph(
//                        navBackStackEntry = navBackStackEntry,
//                        modifier = modifier,
//                        nextScreen = appState::navigateToRoute,
//                        goToFirstOnboardingScreen =  { route ->
//                            //appState.navigateToAnimatedCreditCard(route = route)
//                        },
//                        goToSecondOnboardingScreen =  { route ->
//                            //appState.navigateToMovieDetails(route = route, movieId = movieId)
//                        },
//                        navigateUp = {
//                            appState.upPress()
//                        }
//                    )
//                }
            }
        }
    }
}

@Composable
fun NavigationStack(routeFirstScreen: String, modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = routeFirstScreen) {
        composable(
            SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route,
        ) {
            HorizontalPager(
                modifier = modifier,
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                            //launchSingleTop = true
                            //restoreState = true

                            // Pop up backstack to the first destination and save state. This makes going back
                            // to the start destination when pressing back in any other bottom tab.
                            // popUpTo(findStartDestination(navController.graph).id) { saveState = true }

                    }
                    //nextScreen(route, navBackStackEntry)
                }
            )
        }
        composable(
            SignUpOnboardingSections.LOGIN_SCREEN.route,
        ) {
            LoginScreen(
                modifier = modifier,
                viewModel = hiltViewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(
            SignUpOnboardingSections.FORGOT_PASSWORD_SCREEN.route,
        ) {
            ForgotPasswordScreen(
                modifier = modifier,
                viewModel = hiltViewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { email ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route + "/$email")
                    }

                    //nextScreen(route, navBackStackEntry )
                }
            )
        }

        composable(
            "${SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route}/{${NavArguments.EMAIL}}",
            arguments = listOf(navArgument(NavArguments.EMAIL) {
                type = NavType.StringType
            })
            //SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route,
        ) {
            ForgotPasswordUpdateScreen(
                modifier = modifier,
                viewModel = hiltViewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                    }
                }
            )
        }

    }
}

object NavArguments {
    const val EMAIL = "emailAddress"
}

fun NavGraphBuilder.navGraph(
    navBackStackEntry: State<NavBackStackEntry?>,
    modifier: Modifier,
    nextScreen: (route: String, navBackStackEntry: State<NavBackStackEntry?>) -> Unit,
    goToFirstOnboardingScreen: (route: String ) -> Unit,
    goToSecondOnboardingScreen: (route: String) -> Unit,
    navigateUp:() -> Unit
) {
    composable(
        SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route,
    ) {
        HorizontalPager(
            modifier = modifier,
            nextScreen = { route ->
                nextScreen(route, navBackStackEntry)
            }
        )
    }

    composable(
        SignUpOnboardingSections.LOGIN_SCREEN.route,
    ) {
        LoginScreen(
             modifier = modifier,
             viewModel = LoginViewModel(),
             navigateUp = {
                 navigateUp()
             },
             nextScreen = { route ->
                nextScreen(route, navBackStackEntry)
             }
        )
    }

    composable(
        SignUpOnboardingSections.FORGOT_PASSWORD_SCREEN.route,
    ) {
        ForgotPasswordScreen(
            modifier = modifier,
            viewModel = ForgotPasswordViewModel(),
            navigateUp = {
                navigateUp()
            },
            nextScreen = { route ->
                nextScreen(route, navBackStackEntry )
            }
        )
    }

//    composable(
//        SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route,
//    ) {
//        ForgotPasswordUpdateScreen(
//            modifier = modifier,
//            viewModel = ForgotPasswordUpdateViewModel(),
//            navigateUp = {
//                navigateUp()
//            },
//            nextScreen = { route ->
//                nextScreen(route, navBackStackEntry )
//            }
//        )
//    }

//    composable(
//        SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route,
//    ) {
//        SecondOnboardingScreen(
//            titleText = stringResource(R.string.intro_key_sharing_slide_title),
//            descriptionText = stringResource(R.string.intro_key_sharing_slide_content),
//            buttonText = stringResource(id = R.string.app_generic_next),
//            textAlign = TextAlign.Start,
//            pageNumber = "3",
//            firstImage = R.drawable.img_onboarding_key,
//            secondeImage = R.drawable.schlauebox_logo_invert,
//            modifier = modifier,
//            nextScreen = nextScreen,
//            nextScreenRoute = SignUpOnboardingSections.FOURTH_ONBOARDING_SCREEN.route
//        )
//    }
//
//    composable(
//        SignUpOnboardingSections.FOURTH_ONBOARDING_SCREEN.route,
//    ) {
//        SecondOnboardingScreen(
//            titleText = stringResource(R.string.intro_pickup_slide_title),
//            descriptionText = stringResource(R.string.intro_pickup_slide_content),
//            buttonText = stringResource(id = R.string.app_generic_sign_in),
//            textAlign = TextAlign.Start,
//            pageNumber = "4",
//            firstImage = R.drawable.img_onboarding_start,
//            secondeImage = R.drawable.schlauebox_logo_invert,
//            modifier = modifier,
//            nextScreen = nextScreen,
//            nextScreenRoute = SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route
//        )
//    }

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



enum class SignUpOnboardingSections(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
    FIRST_ONBOARDING_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/firstOnboarding"),
    SECOND_ONBOARDING_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/secondOnboarding"),
    LOGIN_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/loginScreen"),
    FORGOT_PASSWORD_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/forgotPasswordScreen"),
    FORGOT_PASSWORD_UPDATE_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/forgotPasswordUpdateScreen"),
    FOURTH_ONBOARDING_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/fourthOnboarding"),
    FIFTH_ONBOARDING_SCREEN(R.string.login_title, Icons.Outlined.Search, "splash/fifthOnboarding"),
//    PRO_ONBOARDING_SCREEN(R.string.btn_continue, Icons.Outlined.Search, "splash/proOnboarding"),
//    INTRODUCTION(R.string.btn_continue, Icons.Outlined.Search, "splash/introduction"),
//    LOGIN(R.string.btn_continue, Icons.Outlined.Search, "splash/login"),
//    FORGOT_PASSWORD(R.string.btn_continue, Icons.Outlined.Search, "splash/forgotPassword"),
//    FORGOT_PASSWORD_SUCCESS(R.string.btn_continue, Icons.Outlined.Search,
}