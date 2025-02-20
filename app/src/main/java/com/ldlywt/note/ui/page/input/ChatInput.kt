package com.ldlywt.note.ui.page.input

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import com.ldlywt.note.R
import com.ldlywt.note.bean.Attachment
import com.ldlywt.note.bean.Note
import com.ldlywt.note.bean.Tag
import com.ldlywt.note.component.PIconButton
import com.ldlywt.note.utils.lunchMain
import com.ldlywt.note.ui.page.home.clickable
import com.ldlywt.note.utils.handlePickFiles
import com.ldlywt.note.utils.str
import com.ldlywt.note.utils.toast
import com.ldlywt.note.ui.page.LocalMemosViewModel
import com.ldlywt.note.ui.page.LocalTags
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ChatInput(
    isShow: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    dismiss: () -> Unit
) {
    var bottomSheetState by rememberSaveable { mutableStateOf(false) }
    bottomSheetState = isShow
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var text: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val uploadAttachments: SnapshotStateList<Attachment> = remember { mutableStateListOf() }
    val context = LocalContext.current
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var photoImageUri by remember { mutableStateOf<Uri?>(null) }
    val tagList = LocalTags.current.filterNot { it.isCityTag }
    val memosViewModel = LocalMemosViewModel.current
    val locationInfoList by memosViewModel.getAllLocationInfo().collectAsState(initial = emptyList())

    fun uploadImage(uri: Uri) = coroutineScope.launch {
        lunchMain {
            handlePickFiles(setOf(uri)) {
                uploadAttachments.addAll(it)
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { uploadImage(it) }
    }

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoImageUri?.let { uploadImage(it) }
        }

    }

    fun submit() = coroutineScope.launch {
        softwareKeyboardController?.hide()
        focusRequester.freeFocus()
        val content = text.text
        memosViewModel.insertOrUpdate(Note(content = content, attachments = uploadAttachments))
        dismiss()
    }


    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState) {
            focusRequester.requestFocus()
            softwareKeyboardController?.show()
            text = TextFieldValue("")
            uploadAttachments.clear()
        } else {
            softwareKeyboardController?.hide()
            // 弹框消失时执行你想要的操作
        }
    }

    @Composable
    fun TagButton(tagList: List<Tag>, ) {
        if (tagList.isEmpty()) {
            PIconButton(
                imageVector = Icons.Filled.Tag,
                contentDescription = stringResource(R.string.tag),
            ) {
                text = text.copy(
                    text.text.replaceRange(text.selection.min, text.selection.max, "#"), TextRange(text.selection.min + 1)
                )
            }
        } else {
            Box {
                DropdownMenu(
                    expanded = tagMenuExpanded, onDismissRequest = { tagMenuExpanded = false }, properties = PopupProperties(focusable = false)
                ) {
                    tagList.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.tag) },
                            onClick = {
                                val tagText = "${tag} "
                                text = text.copy(
                                    text.text.replaceRange(
                                        text.selection.min, text.selection.max, tagText
                                    ), TextRange(text.selection.min + tagText.length)
                                )
                                tagMenuExpanded = false
                            },
                        )
                    }
                }
                PIconButton(
                    imageVector = Icons.Outlined.Tag,
                    contentDescription = stringResource(R.string.tag),
                ) {
                    tagMenuExpanded = !tagMenuExpanded
                }
            }
        }
    }

    @Composable
    fun LocationButton(locationList: List<String>, ) {
        if (locationList.isEmpty()) {
            PIconButton(
                imageVector = Icons.Filled.AddLocation,
                contentDescription = stringResource(R.string.location),
            ) {
                text = text.copy(
                    text.text.replaceRange(text.selection.min, text.selection.max, "@"), TextRange(text.selection.min + 1)
                )
            }
        } else {
            Box {
                DropdownMenu(
                    expanded = locationMenuExpanded, onDismissRequest = { locationMenuExpanded = false }, properties = PopupProperties(focusable = false)
                ) {
                    locationList.forEach { txt ->
                        DropdownMenuItem(
                            text = { Text(txt) },
                            onClick = {
                                val tagText = " @${txt} "
                                text = text.copy(
                                    text.text.replaceRange(
                                        text.selection.min, text.selection.max, tagText
                                    ), TextRange(text.selection.min + tagText.length)
                                )
                                locationMenuExpanded = false
                            },
                        )
                    }
                }
                PIconButton(
                    imageVector = Icons.Filled.AddLocation,
                    contentDescription = stringResource(R.string.location),
                ) {
                    locationMenuExpanded = !locationMenuExpanded
                }
            }
        }
    }


    if (isShow) {
        Box(
            contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(showRipple = false) {
                    dismiss()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), color = SaltTheme.colors.background)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    minLines = 5,
                    textStyle = SaltTheme.textStyles.paragraph,
                    onValueChange = { it: TextFieldValue ->
                        text = it
                    },
                    modifier =
                    modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clickable {  },
                    keyboardOptions = keyboardOptions,
                    label = { Text(R.string.any_thoughts.str) },
                )

                if (uploadAttachments.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .height(80.dp)
                            .padding(start = 15.dp, end = 15.dp, bottom = 15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uploadAttachments.toList(), { it.path }) { resource ->
                            InputImage(attachment = resource) { pat ->
                                uploadAttachments.remove(uploadAttachments.firstOrNull { it.path == pat })
                            }
                        }
                    }
                }

                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TagButton(tagList)
                    LocationButton(locationInfoList)
                    PIconButton(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = stringResource(R.string.add_image),
                    ) {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    PIconButton(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = stringResource(R.string.take_photo),
                    ) {
                        try {
                            val imagesFolder = File(context.cacheDir, "capture_picture")
                            if (!imagesFolder.exists()) {
                                imagesFolder.mkdirs()
                            }
                            val file = File.createTempFile("capture_picture_", ".jpg", imagesFolder)
                            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                            photoImageUri = uri
                            takePhoto.launch(uri)
                        } catch (e: ActivityNotFoundException) {
                            toast(e.localizedMessage ?: "Unable to take picture.")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    PIconButton(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = stringResource(R.string.send),
                    ) {
                        submit()
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
