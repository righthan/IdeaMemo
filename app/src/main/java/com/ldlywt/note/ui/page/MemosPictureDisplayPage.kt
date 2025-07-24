package com.ldlywt.note.ui.page

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ldlywt.note.ui.page.router.debouncedPopBackStack
import com.ldlywt.note.utils.BlurTransformation
import com.ldlywt.note.utils.SharedPreferencesUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemosPictureDisplayPage(
    imageUrls: List<String>,
    initialIndex: Int,
    navController: NavHostController
) {
    val userSession by SharedPreferencesUtils.memosUserSession.collectAsState("")
    val pagerState = rememberPagerState(
        pageCount = { imageUrls.size },
        initialPage = initialIndex
    )

    Box {
        HorizontalPager(state = pagerState) { page ->
            Surface(modifier = Modifier.fillMaxSize()) {
                MemosDetailContent(
                    imgUrl = imageUrls[page],
                    userSession = userSession ?: "",
                    requestImage = {
                        MemosImage(imageUrls[page],userSession ?: "")
                    }
                )
            }
        }

        IconButton(
            onClick = { navController.debouncedPopBackStack() },
            modifier = Modifier.padding(start = 12.dp, top = 24.dp, end = 0.dp, bottom = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFFFFFFFF)
            )
        }
    }
}

@Composable
private fun MemosDetailContent(
    imgUrl: String?,
    userSession: String,
    modifier: Modifier = Modifier,
    requestImage: @Composable () -> Unit
) {
    Surface {
        // 模糊背景
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUrl)
                .addHeader("Cookie",  "user_session=$userSession")
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

        // 清晰前景
        requestImage()
    }
}

@Composable
private fun MemosImage(imgUrl: String, userSession: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .addHeader("Cookie",  "user_session=$userSession")
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
} 