package com.ldlywt.note.ui.page.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.ui.page.LocalMemosViewModel
import com.ldlywt.note.ui.page.LocalTags
import com.ldlywt.note.ui.page.NoteViewModel
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.ui.page.router.debouncedPopBackStack
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, UnstableSaltApi::class)
@Composable
fun TagListPage(navController: NavHostController) {
    val tagList = LocalTags.current
    val noteViewModel: NoteViewModel = LocalMemosViewModel.current
    val allYears = remember { mutableStateListOf<String>() }

    LaunchedEffect(key1 = Unit) {
        allYears.clear()
        allYears.addAll(noteViewModel.getAllDistinctYears())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
            .padding(top = 30.dp)
    ) {
        TitleBar(
            onBack = {
                navController.debouncedPopBackStack()
            },
            text = stringResource(R.string.tag)
        )
        LazyColumn(modifier = Modifier
            .fillMaxWidth(1f)
            .padding(start = 12.dp, end = 12.dp), content = {
            item {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(R.string.custom),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                FlowRow(
                    Modifier
                        .fillMaxWidth(1f)
                        .wrapContentHeight(align = Alignment.Top),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    repeat(tagList.size) { index ->
                        ElevatedAssistChip(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .align(alignment = Alignment.CenterVertically),
                            onClick = {
                                navController.navigate(Screen.TagDetail(tagList[index].tag))
                            },
                            label = {
                                Text(tagList[index].tag)
                            }
                        )
                    }
                }
            }

            if (allYears.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(R.string.year),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    FlowRow(
                        Modifier
                            .fillMaxWidth(1f)
                            .wrapContentHeight(align = Alignment.Top),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        repeat(allYears.size) { index ->
                            ElevatedAssistChip(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .align(alignment = Alignment.CenterVertically),
                                onClick = {
                                    navController.navigate(Screen.YearDetail(allYears[index]))
                                },
                                label = {
                                    Text(allYears[index])
                                },
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        })
    }
}