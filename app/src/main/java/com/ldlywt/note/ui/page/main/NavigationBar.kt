package com.ldlywt.note.ui.page.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.moriafly.salt.ui.SaltTheme
import java.util.Locale

@Deprecated("Use AdaptiveNavigationBar instead")
@Composable
fun BottomNavigationBar(modifier: Modifier, navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isNavBarVisible = remember(currentBackStackEntry) {
        val currentDestination = currentBackStackEntry?.destination
        NavigationBarPath.entries.any { it.route == currentDestination?.route }
    }

    AnimatedVisibility(
        visible = isNavBarVisible,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar(navController = navController)
    }
}

@Deprecated("Use AdaptiveNavigationBar instead")
@Composable
fun NavigationBar(
    navController: NavController
) {
    // TODO: Can we use `navigationBarsPadding()` instead?
    NavigationBar(
        Modifier.height(
            65.dp + WindowInsets
                .navigationBars
                .asPaddingValues()
                .calculateBottomPadding()
        ),
        // TODO: Use a `NavigationRail` instead.
        windowInsets = if (LocalConfiguration.current.orientation
            == Configuration.ORIENTATION_LANDSCAPE
        ) {
            WindowInsets.displayCutout
        } else {
            WindowInsets(0.dp)
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        NavigationBarPath.entries.forEach { destination ->
            NavigationBarItem(
                modifier = Modifier.navigationBarsPadding(),
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon
            )
        }
    }
}

@Deprecated("Use AdaptiveNavigationBar instead")
@Composable
fun NavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .height(
                65.dp + WindowInsets
                    .navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = destination.route == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(index) },
                icon = destination.icon,
            )
        }
    }
}

/**
 * 可以将[currentDestination]替换成索引判断是否选中
 */
@Composable
fun AdaptiveNavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        // 使用 NavigationRail 适配宽屏
        NavigationRail(modifier, containerColor = SaltTheme.colors.subBackground) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
//                    label = { Text(destination.route) }, // 可选：添加标签
                )
            }
        }
    } else {
        // 使用 NavigationBar 适配普通屏幕
        NavigationBar(modifier, containerColor = SaltTheme.colors.subBackground) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
//                    label = { Text(destination.label()) },
                )
            }
        }
    }
}

enum class NavigationBarPath(
    val route: String,
    val icon: @Composable () -> Unit,
    val label: @Composable () -> String,
) {
    AllNote(
        route = "home".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "memos"
            )
        },
        label = { "memos" }
    ),
    Calendar(
        route = "Calendar".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calendar"
            )
        },
        label = { "Calendar" }
    ),
    Explore(
        route = "Explore".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "explore"
            )
        },
        label = { "explore" }
    ),
    Settings(
        route = "Settings".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "settings"
            )
        },
        label = { "Settings"}
    )
}

private fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }