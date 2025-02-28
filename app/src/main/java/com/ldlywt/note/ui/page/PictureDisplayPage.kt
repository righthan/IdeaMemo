package com.ldlywt.note.ui.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ldlywt.note.utils.BlurTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureDisplayPage(
    pathList: List<String>, index: Int,
    onBack: (() -> Unit)? = null
) {
    val pagerState = rememberPagerState(pageCount = { pathList.size }, initialPage = index) // 定义10个页面

    BackHandler {
        onBack?.invoke()
    }

    Box {
        HorizontalPager(state = pagerState) { page ->
            Surface(modifier = Modifier.fillMaxSize()) {
                DetailContent(
                    imgUrl = pathList[page],
                    requestImage = {
                        Image(pathList[page])
                    }
                )
            }
        }
        IconButton(onClick = { onBack?.invoke() }, modifier = Modifier.padding(start = 12.dp, top = 24.dp, end = 0.dp, bottom = 0.dp)) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFFFFFFFF)
            )
        }
    }
}

@Composable
private fun DetailContent(
    imgUrl: String?,
    modifier: Modifier = Modifier,
    requestImage: @Composable () -> Unit
) {
    Surface {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUrl)
                .transformations(
                    BlurTransformation(
                        LocalContext.current,
                        radius = 25f,
                        sampling = 5f
                    )
                )
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
        )

        requestImage()
    }
}

@Composable
private fun Image(imgUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}
