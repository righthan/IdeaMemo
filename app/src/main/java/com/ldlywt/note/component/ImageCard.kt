package com.ldlywt.note.component

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ldlywt.note.bean.Note
import com.ldlywt.note.ui.page.router.Screen
import kotlinx.coroutines.launch

@Composable
fun ImageCard(note: Note, navHostController: NavHostController?) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (note.attachments.size == 1) {
        BoxWithConstraints {
            var isLandscape by remember { mutableStateOf(true) }
            var aspectRatio by remember { mutableStateOf(1f) }

            LaunchedEffect(note.attachments[0].path) {
                scope.launch {
                    isLandscape = isLandscape(context, note.attachments[0].path)
                    aspectRatio = getAspectRatio(context, note.attachments[0].path)
                }
            }

            AsyncImage(
                model = note.attachments[0].path,
                contentDescription = null,
                modifier = Modifier
                    .let { modifier ->
                        if (isLandscape) {
                            modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        } else {
                            modifier
                                .height(200.dp)
                                .width(200.dp * aspectRatio)
                        }
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        navHostController?.navigate(Screen.PictureDisplay(note.attachments[0].path))
                    },
                contentScale = ContentScale.Crop
            )
        }
    } else {
        LazyRow(
            modifier = Modifier
                .height(90.dp)
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(count = note.attachments.size, key = { index -> note.attachments[index].path }) { index ->
                val path: String = note.attachments[index].path
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .zIndex(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            navHostController?.navigate(Screen.PictureDisplay(path))
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

suspend fun isLandscape(context: Context, imagePath: String): Boolean {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imagePath)
        .build()

    val result = (imageLoader.execute(request) as SuccessResult).drawable
    val width = result.intrinsicWidth
    val height = result.intrinsicHeight

    return width > height
}

suspend fun getAspectRatio(context: Context, imagePath: String): Float {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imagePath)
        .build()

    val result = (imageLoader.execute(request) as SuccessResult).drawable
    val width = result.intrinsicWidth
    val height = result.intrinsicHeight

    return width.toFloat() / height.toFloat()
}