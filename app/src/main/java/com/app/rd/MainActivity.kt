package com.app.rd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.app.rd.player.MultiAudioPlayer
import com.app.rd.ui.audio.AudioViewModel
import com.app.rd.ui.theme.RDTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false
    private lateinit var multiAudioPlayer: MultiAudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = listOf("https://www.computerhope.com/jargon/m/example.mp3", "https://www.computerhope.com/jargon/m/example.mp3", "https://www.computerhope.com/jargon/m/example.mp3", "https://www.computerhope.com/jargon/m/example.mp3", "https://www.computerhope.com/jargon/m/example.mp3")
        val uriList = list.map { it.toUri() }
        multiAudioPlayer = MultiAudioPlayer(this, uriList)

        setContent {
            RDTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    ExoPlayerView()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        multiAudioPlayer.initializePlayers()
        multiAudioPlayer.playAll()
    }

    override fun onPause() {
        super.onPause()
        multiAudioPlayer.stopAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        multiAudioPlayer.stopAll()
    }

}

//
//@Composable
//fun ExoPlayerView() {
//
//    // Get the current context
//    val context = LocalContext.current
//
//
//    // Initialize ExoPlayer
//    val exoPlayer = ExoPlayer.Builder(context)
//        .setHandleAudioBecomingNoisy(true)
//        .build()
//
//    // Create a MediaSource
//    val mediaSource = remember("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") {
//        MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
//    }
//
//    // Set MediaSource to ExoPlayer
//    LaunchedEffect(mediaSource) {
//        exoPlayer.setMediaItem(mediaSource)
//        exoPlayer.prepare()
//    }
//
//    // Manage lifecycle events
//    DisposableEffect(Unit) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    //  Use AndroidView to embed an Android View (PlayerView) into Compose
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//    ) {
//        AndroidView(
//            factory = { ctx ->
//                PlayerView(ctx).apply {
//                    player = exoPlayer
//                    layoutParams = ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//                }
//            },
//            modifier = Modifier
//                .heightIn(max = 250.dp) // Set your desired height
//                .align(Alignment.TopCenter)
//        )
//    }
//}
//

/*

Yesterday:
Other:
Android Core:
- Video Playlist - offline video playing( completed )

GameOn App Improvement:
- Reducing repeated api call ( completed )

Today:
Other:
Android Core:
- Video Playlist - online video playing
- Exoplayer Player Events

*/

