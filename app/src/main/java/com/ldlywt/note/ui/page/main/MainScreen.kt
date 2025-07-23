package com.ldlywt.note.ui.page.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.ldlywt.note.ui.page.home.CalenderPage
import com.ldlywt.note.ui.page.memos.MemosPage
import com.ldlywt.note.ui.page.settings.ExplorePage
import com.ldlywt.note.ui.page.settings.SettingsPage
import com.ldlywt.note.utils.isWideScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var currentDestination by rememberSaveable { mutableStateOf(NavigationBarPath.AllNote.route) }
    val pagerState = rememberPagerState(initialPage = 0) { NavigationBarPath.entries.size }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current // 屏幕方向改变会触发 recompose
    val context = LocalContext.current
    var hideNavBar by rememberSaveable { mutableStateOf(false) }
    // 在屏幕方向变化时更新 isWideScreen
    val isWideScreen = remember(configuration.orientation) { isWideScreen(context) }

    val navigationBar: @Composable () -> Unit = {
        AdaptiveNavigationBar(
            destinations = NavigationBarPath.entries,
            currentDestination = currentDestination,
            onNavigateToDestination = {
                currentDestination = NavigationBarPath.entries[it].route
                scope.launch { pagerState.scrollToPage(it) }
            },
            isWideScreen = isWideScreen,
        )
    }

    val pagerContent: @Composable (Modifier) -> Unit = { modifier ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = modifier
        ) { page ->
            when (page) {
                0 -> MemosPage(navController = navController)
                1 -> CalenderPage(navController = navController)
                2 -> ExplorePage(navController = navController)
                3 -> SettingsPage(navController = navController)
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
        ) {
            if (!hideNavBar) {
                navigationBar()
            }
            pagerContent(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            pagerContent(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            if (!hideNavBar) {
                navigationBar()
            }
        }
    }
}
