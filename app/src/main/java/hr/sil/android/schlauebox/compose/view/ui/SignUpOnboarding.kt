package hr.sil.android.schlauebox.compose.view.ui


// import androidx.navigation.compose.composable
import hr.sil.android.schlauebox.R
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable



//import androidx.navigation.compose.composable
import androidx.navigation.navArgument
//import com.google.accompanist.navigation.animation.composable

import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.SecondOnboardingScreen


import hr.sil.android.schlauebox.compose.view.ui.onboarding_screens.FirstOnboardingScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.addSignUpOnboardingGraph(
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
    nextScreen: (route: String) -> Unit,
    navigateUp: (route: String) -> Unit,
    navigateUpWhatsApp: () -> Unit,
    signUpOnboardingViewModel: SignUpOnboardingViewModel
) {
    composable(
        SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route,
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1800 })
        }
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
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1800 })
        }
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

}

