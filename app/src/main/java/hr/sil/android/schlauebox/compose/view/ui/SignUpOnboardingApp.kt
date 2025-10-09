
package hr.sil.android.schlauebox.compose.view.ui

import android.R.style.Theme
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import hr.sil.android.schlauebox.compose.view.ui.components.HuberScaffold
import hr.sil.android.schlauebox.compose.view.ui.theme.HuberTheme


import hr.sil.android.schlauebox.compose.view.ui.addSignUpOnboardingGraph

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpOnboardingApp(
    signUpOnboardingViewModel: SignUpOnboardingViewModel
) {
    //val selectedTheme = signUpOnboardingViewModel.selectedTheme.collectAsState(initial = null)

    HuberTheme {
        val appState = rememberSignUpAppState()
        HuberScaffold(scaffoldState = appState.scaffoldState, modifier = Modifier.semantics {
            testTagsAsResourceId = true
        }) { innerPaddingModifier ->
            val modifier = Modifier
            // Box required because there is no background in transition moment when changing screens
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AnimatedNavHost(
                    navController = appState.navController,
                    startDestination = SignUpOnboardingDestinations.ONBOARDING_ROUTE,
                    modifier = Modifier.padding(innerPaddingModifier)
                ) {
                    navGraph(
                        upPress = appState::upPress,
                        modifier = modifier,
                        nextScreen = appState::navigateToRoute,
                        navigateUp = appState::navigateToRouteDeletePreviousComposable,
                        navigateUpWhatsApp = appState::upPress,
                        signUpOnboardingViewModel = signUpOnboardingViewModel,
                    )
                }
            }
        }
    }
}

private fun NavGraphBuilder.navGraph(
    upPress: () -> Unit,
    modifier: Modifier,
    nextScreen: (route: String) -> Unit,
    navigateUp: (route: String) -> Unit,
    navigateUpWhatsApp: () -> Unit,
    signUpOnboardingViewModel: SignUpOnboardingViewModel,
) {
    navigation(
        route = SignUpOnboardingDestinations.ONBOARDING_ROUTE,
        startDestination = SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route
    ) {
        addSignUpOnboardingGraph(
            upPress,
            modifier,
            nextScreen,
            navigateUp,
            navigateUpWhatsApp,
            signUpOnboardingViewModel
        )
    }
}
