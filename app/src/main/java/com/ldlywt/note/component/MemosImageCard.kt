package com.ldlywt.note.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ldlywt.note.api.Memo
import com.ldlywt.note.api.MemoAttachment
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.utils.SharedPreferencesUtils

@Composable
fun MemosImageCard(memo: Memo, navHostController: NavHostController? = null) {
    val baseUrl by SharedPreferencesUtils.memosServerUrl.collectAsState("")
    val authToken by SharedPreferencesUtils.memosAuthToken.collectAsState("")
    // 过滤出图片类型的附件
    val imageAttachments = memo.attachments.filter { 
        it.type.startsWith("image/") 
    }
    
    if (imageAttachments.isEmpty()) return
    
    if (imageAttachments.size == 1) {
        // 单张图片的布局
        val attachment = imageAttachments[0]
        val imageUrl = buildImageUrl(baseUrl ?: "", attachment, thumbnail = true)
        val context = LocalContext.current
        
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .addHeader("Authorization", "Bearer $authToken")
                .build(),
            contentDescription = attachment.filename,
            modifier = Modifier
                .width(160.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    navHostController?.navigate(
                        Screen.MemosPictureDisplay(
                            imageUrls = getFullSizeImageUrls(baseUrl ?: "", memo),
                            initialIndex = 0
                        )
                    )
                },
            contentScale = ContentScale.Crop
        )
    } else {
        // 多张图片的横向滚动布局
        val context = LocalContext.current
        LazyRow(
            modifier = Modifier
                .height(90.dp)
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                count = imageAttachments.size,
                key = { index -> imageAttachments[index].name }
            ) { index ->
                val attachment = imageAttachments[index]
                val imageUrl = buildImageUrl(baseUrl ?: "", attachment, thumbnail = true)
                
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .addHeader("Authorization", "Bearer $authToken")
                        .build(),
                    contentDescription = attachment.filename,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .zIndex(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            navHostController?.navigate(
                                Screen.MemosPictureDisplay(
                                    imageUrls = getFullSizeImageUrls(baseUrl ?: "", memo),
                                    initialIndex = index
                                )
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * 根据附件信息构建图片URL
 * @param baseUrl 基础URL
 * @param attachment 附件信息
 * @param thumbnail 是否使用缩略图
 * @return 构建的图片URL
 */
private fun buildImageUrl(baseUrl: String, attachment: MemoAttachment, thumbnail: Boolean = false): String {
    val url = "$baseUrl/file/${attachment.name}/${attachment.filename}"
    return if (thumbnail) "$url?thumbnail=true" else url
}

/**
 * 获取全尺寸图片URL列表
 */
fun getFullSizeImageUrls(baseUrl: String, memo: Memo): List<String> {
    return memo.attachments
        .filter { it.type.startsWith("image/") }
        .map { buildImageUrl(baseUrl, it, thumbnail = false) }
}

/**
 * 获取缩略图URL列表  
 */
fun getThumbnailImageUrls(baseUrl: String, memo: Memo): List<String> {
    return memo.attachments
        .filter { it.type.startsWith("image/") }
        .map { buildImageUrl(baseUrl, it, thumbnail = true) }
} 