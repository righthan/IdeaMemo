package com.ldlywt.note.ui.page.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.component.MemoCard
import com.ldlywt.note.ui.page.router.debouncedPopBackStack
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.delay

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    navController: NavHostController,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    val searchQuery by searchViewModel.query.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val memosList by searchViewModel.memosFlow.collectAsState(initial = emptyList())
    val isLoading by searchViewModel.isLoading.collectAsState()
    val error by searchViewModel.error.collectAsState()

    Box(Modifier.fillMaxSize()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = searchViewModel::onQuery,
                    onSearch = {
                        searchBarExpanded = true
                        searchViewModel.onSearch(it)
                        keyboardController?.hide()
                    },
                    expanded = searchBarExpanded,
                    onExpandedChange = { },
                    placeholder = {
                        Text(stringResource(id = R.string.search_hint))
                    },
                    leadingIcon = {
                        IconButton(onClick = { navController.debouncedPopBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                searchViewModel.clearSearchQuery()
                            }) {
                                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "")
                            }
                        }
                    },
                )
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .focusRequester(focusRequester),
            expanded = searchBarExpanded,
            onExpandedChange = { if (!it) navController.debouncedPopBackStack() },
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // 加载指示器
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    // 错误状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "搜索失败",
                                style = SaltTheme.textStyles.main,
                                color = SaltTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error ?: "",
                                style = SaltTheme.textStyles.sub,
                                color = SaltTheme.colors.subText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // 搜索结果
                    LazyColumn(
                        Modifier.fillMaxSize()
                    ) {
                        items(
                            items = memosList,
                            key = { it.name }
                        ) { memo ->
                            MemoCard(memo = memo, navHostController = navController)
                        }
                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}