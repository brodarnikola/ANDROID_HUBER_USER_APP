//package hr.sil.android.schlauebox.compose.view.ui
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import kotlinx.coroutines.launch
//
//
//@Composable
//fun SettingsScreen() {
//
//    Log.d("SettingsScreen", "SettingsScreen enter")
//    val settingsState = viewModel.state.collectAsStateWithLifecycle().value
//    val context = LocalContext.current
//
//    LaunchedEffect(key1 = Unit) {
//        viewModel.uiEffect.collect { event ->
//            when (event) {
//                is UiEffect.ShowToast -> {
//                    Toast.makeText(context, event.message, event.toastLength).show()
//                }
//            }
//        }
//    }
//
//    settingsState.let {
//        GithubListScreen(githubState = settingsState)
//    }
//}
//
//@Composable
//fun GithubListScreen(githubState: SettingsState) {
//    val githubList = remember { githubState.githubResponseApi }
//    val githubListState = rememberLazyListState()
//
//    val isEnabledDerivedStateCase by remember { derivedStateOf { githubListState.firstVisibleItemIndex != 0 } }
//    val coroutineScope = rememberCoroutineScope()
//
//    Log.d("GITHUB", "githubResponseApi draw data is 1: ${githubList}")
//    LazyColumn(
//        state = githubListState,
//        modifier = Modifier
//            .fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(
//            items = githubList,
//            key = { github ->
//                // Return a stable, unique key for the github repository
//                github.id
//            }
//        ) { github ->
//            Column(
//                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
//            ) {
//                androidx.compose.material.Text(
//                    modifier = Modifier
//                        .wrapContentHeight()
//                        .wrapContentWidth(),
//                    text = "Language: ${github.language}"
//                )
//                androidx.compose.material.Text(
//                    modifier = Modifier
//                        .wrapContentHeight(Alignment.CenterVertically)
//                        .wrapContentWidth(),
//                    text = "Description: ${github.description}",
//                    maxLines = 3
//                )
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentAlignment = Alignment.BottomEnd
//    ) {
//        AnimatedVisibility(
//            visible = isEnabledDerivedStateCase,
//            enter = fadeIn(),
//            exit = fadeOut(),
//        ) {
//            ScrollToTopButton(onClick = {
//                coroutineScope.launch {
//                    githubListState.animateScrollToItem(0)
//                }
//            })
//        }
//    }
//}
//
