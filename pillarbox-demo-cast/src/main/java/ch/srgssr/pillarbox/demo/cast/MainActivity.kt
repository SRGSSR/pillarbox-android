package ch.srgssr.pillarbox.demo.cast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.widget.CastButton
import ch.srgssr.pillarbox.demo.cast.player.CastPlayerView
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import com.google.android.gms.cast.framework.CastContext

/**
 * Activity showing how to use Cast with Pillarbox.
 */
class MainActivity : FragmentActivity() {

    private lateinit var castContext: CastContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force creation CastContext
        // Getting the cast context later than onStart can cause device discovery not to take place.
        castContext = getCastContext()

        enableEdgeToEdge()
        setContent {
            PillarboxTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButtonPosition = FabPosition.Start,
                    floatingActionButton = {
                        CastButton()
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        CastPlayerView(modifier = Modifier.fillMaxSize(), castContext = castContext)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainPreview() {
    PillarboxTheme {
        CastButton()
    }
}
