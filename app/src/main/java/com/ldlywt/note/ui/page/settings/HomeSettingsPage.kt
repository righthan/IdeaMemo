@file:JvmName("SettingsPageKt")

package com.ldlywt.note.ui.page.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LineStyle
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.material.color.DynamicColors
import com.ldlywt.note.R
import com.ldlywt.note.component.ItemPopup
import com.ldlywt.note.ui.page.LocalMemosState
import com.ldlywt.note.ui.page.LocalTags
import com.ldlywt.note.ui.page.MemosViewModel
import com.ldlywt.note.ui.page.data.DataManagerViewModel
import com.ldlywt.note.ui.page.main.MainActivity
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.utils.Constant
import com.ldlywt.note.utils.SettingsPreferences
import com.ldlywt.note.utils.TipsDialog
import com.ldlywt.note.utils.lunchIo
import com.ldlywt.note.utils.str
import com.ldlywt.note.utils.toYYMMDD
import com.ldlywt.note.utils.toast
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import kotlinx.coroutines.launch
import com.ldlywt.note.utils.SharedPreferencesUtils
import com.ldlywt.note.ui.page.auth.UserInfoComponent


@Composable
fun SettingsPage(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Column {
            Text(
                text = R.string.settings.str,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                style = SaltTheme.textStyles.main.copy(fontSize = 24.sp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SettingsPreferenceScreen(navController)
    }
}

data class SettingsBean(val title: Int, val imageVector: ImageVector, val onClick: () -> Unit)

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(UnstableSaltApi::class)
@Composable
fun SettingsPreferenceScreen(navController: NavHostController) {
    val dataViewModel = hiltViewModel<DataManagerViewModel>()

    val context = LocalContext.current
    val themeModePopupMenuState = rememberPopupState()
    val maxLinePopupMenuState = rememberPopupState()
    val settingsViewModel = hiltViewModel<SettingsViewModel>()
    val biometricAuthState by settingsViewModel.biometricAuthState.collectAsState()
    var showWarnDialog by rememberSaveable { mutableStateOf(false) }
    val dynamicColor by SettingsPreferences.dynamicColor.collectAsState(false)
    val themeMode by SettingsPreferences.themeMode.collectAsState(SettingsPreferences.ThemeMode.SYSTEM)
    val maxLine by SettingsPreferences.cardMaxLine.collectAsState(SettingsPreferences.CardMaxLineMode.MAX_LINE)
    val scope = rememberCoroutineScope()

    val settingList = listOf(
        SettingsBean(R.string.gallery, Icons.Outlined.Photo) { navController.navigate(Screen.Gallery) },
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        // 固定两列
        content = {
            item(content = {
                HeatContent()
            })

            // 用户信息组件
            item {
                val memosLoginSuccess by SharedPreferencesUtils.memosLoginSuccess.collectAsState(false)
                if (memosLoginSuccess) {
                    UserInfoComponent(
                        onLogout = {
                            // 重新启动Activity以回到登录页面
                            (context as? MainActivity)?.recreate()
                        }
                    )
                }
            }

            item(content = {
                SettingsHeadLayout()
            })

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.user_interface))
                    if (DynamicColors.isDynamicColorAvailable()) {
                        ItemSwitcher(
                            state = dynamicColor,
                            onChange = { checked ->
                                scope.launch {
                                    SettingsPreferences.changeDynamicColor(checked)
                                }
                            },
                            text = stringResource(R.string.dynamic_color_switcher_text),
                            sub = stringResource(R.string.dynamic_color_switcher_sub),
                            iconPainter = painterResource(id = R.drawable.color),
                            iconPaddingValues = PaddingValues(all = 1.7.dp),
                            iconColor = SaltTheme.colors.text,
                        )
                    }
                    ItemPopup(
                        state = themeModePopupMenuState,
                        iconPainter = painterResource(id = R.drawable.app_theme),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.theme_mode_switcher_text),
                        selectedItem = stringResource(id = themeMode.resId),
                        popupWidth = 140
                    ) {

                        val options =
                            SettingsPreferences.ThemeMode.entries.map { stringResource(id = it.resId) }
                        var selectedIndex by remember {
                            mutableIntStateOf(
                                SettingsPreferences.ThemeMode.entries.indexOf(themeMode)
                            )
                        }

                        options.forEachIndexed { index, label ->
                            PopupMenuItem(
                                onClick = {
                                    selectedIndex = index
                                    scope.launch {
                                        SettingsPreferences.changeThemeMode(SettingsPreferences.ThemeMode.entries[index])
                                    }
                                    themeModePopupMenuState.dismiss()
                                },
                                selected = selectedIndex == 0,
                                text = label,
                                iconColor = SaltTheme.colors.text
                            )
                        }
                    }

                    ItemPopup(
                        state = maxLinePopupMenuState,
                        iconPainter = rememberVectorPainter(Icons.Outlined.LineStyle),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.card_note_maxline),
                        selectedItem = maxLine.line.toString(),
                        popupWidth = 140
                    ) {
                        val options = SettingsPreferences.CardMaxLineMode.entries.map { it.line.toString() }
                        var selectedIndex by remember {
                            mutableIntStateOf(
                                SettingsPreferences.CardMaxLineMode.entries.indexOf(maxLine)
                            )
                        }

                        options.forEachIndexed { index, label ->
                            PopupMenuItem(
                                onClick = {
                                    selectedIndex = index
                                    scope.launch {
                                        SettingsPreferences.changeMaxLine(SettingsPreferences.CardMaxLineMode.entries[index])
                                    }
                                    maxLinePopupMenuState.dismiss()
                                },
                                selected = selectedIndex == 0,
                                text = label,
                                iconColor = SaltTheme.colors.text
                            )
                        }
                    }
                }
            }

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.safe))
                    ItemSwitcher(
                        state = biometricAuthState,
                        iconPainter = rememberVectorPainter(Icons.Outlined.Fingerprint),
                        iconColor = SaltTheme.colors.text,
                        onChange = {
                            settingsViewModel.showBiometricPrompt(context as MainActivity)
                        },
                        text = R.string.biometric.str
                    )

                    settingList.forEachIndexed { index, it ->
                        Item(
                            onClick = {
                                it.onClick()
                            },
                            text = it.title.str,
                            iconPainter = rememberVectorPainter(it.imageVector),
                        )
                    }
                }
            }

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.other))
                    Item(
                        onClick = {
                            showWarnDialog = true
                        },
                        text = stringResource(id = R.string.warm_reminder),
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.TipsAndUpdates),
                        iconColor = SaltTheme.colors.text,
                        iconPaddingValues = PaddingValues(all = 1.5.dp)
                    )
                    Item(
                        onClick = {
                            Constant.startGithubReleaseUrl(context)
                        },
                        text = R.string.new_version.str,
                        iconPainter = rememberVectorPainter(Icons.Outlined.Download),
                    )

                    Item(
                        onClick = {
                            navController.navigate(Screen.MoreInfo) { launchSingleTop = true }
                        },
                        text = stringResource(id = R.string.other),
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.Info),
                        iconColor = SaltTheme.colors.text,
                        iconPaddingValues = PaddingValues(all = 1.5.dp)
                    )
                }
            }
        })

    if (showWarnDialog) {
        TipsDialog(block = { showWarnDialog = false })
    }
}

@Composable
fun SettingsHeadLayout() {
    val noteState = LocalMemosState.current
    val memos by noteState::notes
    val tagList = LocalTags.current
    
    // Memos 相关状态
    val memosViewModel: MemosViewModel = hiltViewModel()
    val memosLoginSuccess by SharedPreferencesUtils.memosLoginSuccess.collectAsState(false)
    val memosTotalCount by memosViewModel.totalMemoCount.collectAsState()
    val memosTagStats by memosViewModel.userStats.collectAsState()
    val memosUniqueDays by memosViewModel.uniqueDaysCount.collectAsState()

    Row {
        val modifier = Modifier.weight(1f)

        // 总笔记数
        val totalNotesCount = if (memosLoginSuccess) {
            memosTotalCount.toString()
        } else {
            memos.size.toString()
        }

        // 字符数（只有本地数据可以计算）
        val charactersCount = if (memosLoginSuccess) {
            "N/A" // Memos API 不提供字符数统计
        } else {
            memos.fastSumBy { it.note.noteTitle?.length ?: (0 + it.note.content.length) }.toString()
        }

        // 天数统计
        val daysCount = if (memosLoginSuccess) {
            memosUniqueDays.toString()
        } else {
            memos.map { it.note.createTime.toYYMMDD() }.toSet().size.toString()
        }

        // 标签数量
        val tagsCount = if (memosLoginSuccess) {
            memosTagStats.size.toString()
        } else {
            tagList.size.toString()
        }

        boxText(modifier, totalNotesCount, R.string.all_note.str)
        boxText(modifier, charactersCount, R.string.characters.str)
        boxText(modifier, daysCount, R.string.dyas.str)
        boxText(modifier, tagsCount, R.string.tag.str)
    }
}

@Composable
private fun boxText(modifier: Modifier, title: String, desc: String) {

    Column(
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = SaltTheme.textStyles.main,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = desc,
            style = SaltTheme.textStyles.sub,
        )
    }
}
