package com.ldlywt.note.ui.page.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.kizitonwose.calendar.core.yearMonth
import com.ldlywt.note.R
import com.ldlywt.note.bean.NoteShowBean
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.ui.page.home.clickable
import com.ldlywt.note.ui.page.LocalMemosState
import com.ldlywt.note.ui.page.router.debouncedPopBackStack
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class GalleryItem(val note: NoteShowBean, val path: String, val localDate: LocalDate)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, UnstableSaltApi::class)
@Composable
fun GalleryPage(
    navHostController: NavHostController
) {
    val noteState = LocalMemosState.current
    val memos = remember { mutableStateMapOf<String, List<GalleryItem>>() }

    LaunchedEffect(Unit) {
        val galleryList = mutableListOf<GalleryItem>()
        noteState.notes.forEachIndexed { index, noteShowBean ->
            val localDate = Instant.ofEpochMilli(noteShowBean.note.createTime).atZone(ZoneId.systemDefault()).toLocalDate()
            noteShowBean.note.attachments.forEach { attach ->
                galleryList.add(GalleryItem(noteShowBean, attach.path, localDate))
            }
        }
        val map: Map<String, List<GalleryItem>> = galleryList.groupBy { it.localDate.yearMonth.toString() }
        memos.clear()
        memos.putAll(map)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
            .padding(top = 30.dp)
    ) {
        TitleBar(
            onBack = {
                navHostController.debouncedPopBackStack()
            },
            text = stringResource(R.string.gallery)
        )
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp),
            columns = GridCells.Fixed(3),
            content = {
                memos.toSortedMap(compareByDescending { it }).forEach { (month, list) ->
                    item(span = { GridItemSpan(this.maxLineSpan) }, content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = month, style = SaltTheme.textStyles.main.copy(fontSize = 18.sp).copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    })
                    items(list.size) {
                        AsyncImage(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    navHostController.navigate(route = Screen.InputDetail(list[it].note.note.noteId))
                                },
                            contentScale = ContentScale.Crop,
                            model = list[it].path,
                            contentDescription = null,
                        )
                    }
                    item(span = { GridItemSpan(this.maxLineSpan) }, content = {
                        Spacer(modifier = Modifier.height(20.dp))
                    })
                }
            }
        )
    }
}

