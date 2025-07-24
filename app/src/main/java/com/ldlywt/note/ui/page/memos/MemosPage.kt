package com.ldlywt.note.ui.page.memos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.component.MemoCard
import com.ldlywt.note.component.RYScaffold
import com.ldlywt.note.ui.page.MemosViewModel
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.utils.SharedPreferencesUtils
import com.ldlywt.note.utils.str
import com.moriafly.salt.ui.SaltTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemosPage(
    navController: NavHostController,
    memosViewModel: MemosViewModel = hiltViewModel()
) {
    val memos by memosViewModel.memos.collectAsState()
    val isLoading by memosViewModel.isLoading.collectAsState()
    val isRefreshing by memosViewModel.isRefreshing.collectAsState()
    val isLoadingMore by memosViewModel.isLoadingMore.collectAsState()
    val error by memosViewModel.error.collectAsState()
    val nextPageToken by memosViewModel.nextPageToken.collectAsState()
    
    // 监听登录状态变化，当配置完成后自动加载数据
    val memosLoginSuccess by SharedPreferencesUtils.memosLoginSuccess.collectAsState(false)
    
    LaunchedEffect(memosLoginSuccess) {
        if (memosLoginSuccess) {
            memosViewModel.checkConfigAndLoadMemos()
        }
    }
    
    val listState = rememberLazyListState()
    
    // 监听滚动到底部，加载更多
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            
            lastVisibleItemIndex > (totalItemsNumber - 3) && 
            nextPageToken.isNotEmpty() && 
            !isLoadingMore
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            memosViewModel.loadMoreMemos()
        }
    }
    
    RYScaffold(
        title = "Memos",
        navController = null,
        actions = {
            IconButton(
                onClick = {
                    navController.navigate(route = Screen.Search) {
                        launchSingleTop = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search_hint),
                    tint = SaltTheme.colors.text
                )
            }
        }
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { memosViewModel.refreshMemos() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = memos,
                    key = { it.name }
                ) { memo ->
                    MemoCard(memo = memo, navHostController = navController)
                }
                
                // 加载更多指示器
                if (isLoadingMore && memos.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            
            // 错误提示
            error?.let { errorMessage ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败",
                            style = SaltTheme.textStyles.main,
                            color = SaltTheme.colors.text
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = SaltTheme.textStyles.sub,
                            color = SaltTheme.colors.subText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(
                            onClick = {
                                memosViewModel.refreshMemos()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "重试"
                            )
                        }
                    }
                }
            }
            
            // 首次加载指示器
            if (isLoading && memos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // 空状态
            if (!isLoading && memos.isEmpty() && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝",
                            style = SaltTheme.textStyles.sub
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无笔记",
                            style = SaltTheme.textStyles.main,
                            color = SaltTheme.colors.text
                        )
                    }
                }
            }
        }
    }
} 