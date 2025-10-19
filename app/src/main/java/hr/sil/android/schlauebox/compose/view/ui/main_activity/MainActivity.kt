package hr.sil.android.schlauebox.compose.view.ui.main_activity


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.mvi_compose.ui.theme.MVI_ComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() { // AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainComposeApp()
                // A surface container using the 'background' color from the theme
            }
        }
    }

}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MVI_ComposeTheme {
//    }
//}