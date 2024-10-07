package com.app.ocs.ui.magnification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.app.rd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardMagnificationScreen(
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Card magnification") })
        }
    ) { innerPadding ->

        val ctx = LocalContext.current
        val imageSampleOrg = remember { ImageBitmap.imageResource(ctx.resources, R.drawable.psa1) }

        ImageWithThumbnail(
            imageBitmap = imageSampleOrg,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .height(400.dp),
            contentScale = ContentScale.Fit,
            drawOriginalImage = true,
            thumbnailState = rememberThumbnailState(
                shadow = MaterialShadow(8.dp, spotColor = Color.Black),
                size = DpSize(100.dp, 60.dp),
                shape = RoundedCornerShape(50)
            ),
            contentDescription = null
        ) {
            Box(
                modifier = Modifier.size(imageWidth, imageHeight)
            )
        }
    }

}
