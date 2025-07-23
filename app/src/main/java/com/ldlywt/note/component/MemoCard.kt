package com.ldlywt.note.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ldlywt.note.api.Memo
import com.ldlywt.note.utils.formatMemoTime
import com.moriafly.salt.ui.SaltTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MemoCard(
    memo: Memo,
    navHostController: NavHostController? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SaltTheme.colors.subBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 头部信息：创建者和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text(
//                    text = memo.creator.removePrefix("users/"),
//                    style = SaltTheme.textStyles.sub.copy(
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium
//                    ),
//                    color = SaltTheme.colors.subText
//                )
//                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = memo.createTime.formatMemoTime(),
                    style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp),
                    color = SaltTheme.colors.subText
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 显示是否置顶
                if (memo.pinned) {
                    Text(
                        text = "📌",
                        fontSize = 14.sp
                    )
                }
                
                // 显示可见性
                Text(
                    text = if (memo.visibility == "PRIVATE") "🔒" else "🌐",
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Markdown内容
            MarkdownText(
                markdown = memo.content,
                style = SaltTheme.textStyles.main.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                linkColor = SaltTheme.colors.highlight,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 图片附件显示
            if (memo.attachments.any { it.type.startsWith("image/") }) {
                Spacer(modifier = Modifier.height(12.dp))
                MemosImageCard(memo = memo, navHostController = navHostController)
            }
            
            // 标签显示
            if (memo.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    memo.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp),
                            color = SaltTheme.colors.highlight,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
            
            // 底部信息：附件和其他属性
            if (memo.attachments.isNotEmpty() || memo.property?.hasLink == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (memo.attachments.isNotEmpty()) {
                        // 根据附件类型显示不同图标
                        val attachmentIcons = memo.attachments.map { attachment ->
                            when {
                                attachment.type.startsWith("image/") -> "🖼️"
                                attachment.type.startsWith("video/") -> "🎬"
                                attachment.type.startsWith("audio/") -> "🎵"
                                attachment.type.contains("pdf") -> "📄"
                                else -> "📎"
                            }
                        }.distinct()
                        
                        attachmentIcons.forEach { icon ->
                            Text(
                                text = icon,
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${memo.attachments.size}",
                            style = SaltTheme.textStyles.sub.copy(fontSize = 10.sp),
                            color = SaltTheme.colors.subText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    memo.property?.let { property ->
                        if (property.hasLink) {
                            Text(
                                text = "🔗",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (property.hasCode) {
                            Text(
                                text = "💻",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (property.hasTaskList) {
                            Text(
                                text = "☑️",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(sizeString: String): String {
    return try {
        val sizeBytes = sizeString.toLongOrNull() ?: return sizeString
        when {
            sizeBytes < 1024 -> "${sizeBytes}B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024}KB"
            sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)}MB"
            else -> "${sizeBytes / (1024 * 1024 * 1024)}GB"
        }
    } catch (e: Exception) {
        sizeString
    }
} 