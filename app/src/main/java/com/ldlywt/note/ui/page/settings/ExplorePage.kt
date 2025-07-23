package com.ldlywt.note.ui.page.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.api.Memo
import com.ldlywt.note.api.MemosApiService
import com.ldlywt.note.component.DraggableCard
import com.ldlywt.note.component.EmptyComponent
import com.ldlywt.note.component.LoadingComponent
import com.ldlywt.note.component.MemosImageCard
import com.ldlywt.note.component.locationAndTimeText
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.ui.page.router.debouncedPopBackStack
import com.ldlywt.note.utils.PageTokenCrypto
import com.ldlywt.note.utils.SharedPreferencesUtils
import com.ldlywt.note.utils.orFalse
import com.ldlywt.note.utils.formatMemoTime
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val memosApiService: MemosApiService
) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    /**
     * 获取随机数据
     */
    suspend fun fetchRandomMemos(count: Int = 5): List<Memo> {
        return try {
            val totalCount = SharedPreferencesUtils.memosTotalMemoCount.first()?.toIntOrNull() ?: 0
            if (totalCount <= 0) return emptyList()

            val fetchedMemos = mutableListOf<Memo>()
            repeat(count) {
                val randomOffset = Random.nextInt(0, totalCount)
                val pageToken = PageTokenCrypto.createPageToken(limit = 1, offset = randomOffset)
                val response = memosApiService.getMemos(pageToken = pageToken)
                fetchedMemos.addAll(response.memos)
            }
            fetchedMemos
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 初始加载数据
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedMemos = fetchRandomMemos(5)
                _memos.value = fetchedMemos
                _isEmpty.value = fetchedMemos.isEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                _isEmpty.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 移除备忘录
     */
    fun removeMemo(memo: Memo) {
        val currentMemos = _memos.value.toMutableList()
        // 防止重复删除：只有当memo确实存在时才删除
        if (currentMemos.contains(memo)) {
            currentMemos.remove(memo)
            _memos.value = currentMemos

            // 当剩余2条或更少时，获取新数据
            if (currentMemos.size <= 2) {
                loadMoreData()
            }
        }
        
        // 检查是否为空（在contains检查之外）
        if (_memos.value.isEmpty()) {
            _isEmpty.value = true
        }
    }

    /**
     * 加载更多数据
     */
    private fun loadMoreData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newMemos = fetchRandomMemos(5)
                val currentMemos = _memos.value.toMutableList()
                currentMemos.addAll(newMemos)
                _memos.value = currentMemos
                if (currentMemos.isEmpty()) {
                    _isEmpty.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_memos.value.isEmpty()) {
                    _isEmpty.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, UnstableSaltApi::class)
@Composable
fun ExplorePage(
    navController: NavHostController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val memos by viewModel.memos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()

    // 首次加载数据
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
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
            text = stringResource(R.string.random_walk)
        )

        if (isLoading) {
            LoadingComponent(true)
        } else if (isEmpty) {
            EmptyComponent()
        } else {
            ExploreList(
                memos = memos,
                navController = navController,
                onItemSwiped = { swipedMemo ->
                    viewModel.removeMemo(swipedMemo)
                },
                onItemClick = { index ->
                    // 这里需要转换Memo到Note的ID，暂时使用Memo的name作为标识
                    // navController.navigate(route = Screen.InputDetail(memos[index].name.hashCode().toLong()))
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreList(
    memos: List<Memo>,
    navController: NavHostController,
    onItemSwiped: (Memo) -> Unit,
    onItemClick: (index: Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = screenHeight - 200.dp

    LazyColumn {
        itemsIndexed(
            items = memos,
            key = { index, memo -> memo.name } // 使用 memo.name 作为唯一key
        ) { index, memo ->
            DraggableCard(
                item = memo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .padding(
                        top = 16.dp + (index + 2).dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                onSwiped = { _, item ->
                    onItemSwiped(item as Memo)
                }
            ) {
                ExploreMemoCard(memo, navController)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreMemoCard(
    memo: Memo,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = SaltTheme.colors.subBackground)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        MarkdownText(
            markdown = memo.content,
            style = SaltTheme.textStyles.paragraph.copy(fontSize = 15.sp, lineHeight = 24.sp),
            onTagClick = { tag ->
                navController.navigate(Screen.TagDetail(tag))
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (memo.attachments.isNotEmpty()) {
            MemosImageCard(memo, navController)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        locationAndTimeText(memo.createTime.formatMemoTime(), modifier = Modifier.padding(start = 2.dp))
        // 由于Memo没有location信息，这里暂时注释掉
        // showLocationInfoContent(memo)
    }
}