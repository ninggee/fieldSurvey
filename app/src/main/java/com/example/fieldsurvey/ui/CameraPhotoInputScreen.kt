package com.example.fieldsurvey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 集成相机和输入表单的完整输入界面
 */
@Composable
fun CameraPhotoInputScreen(
    viewModel: SurveyViewModel
) {
    val photoMap by viewModel.photoMap.collectAsState()
    var showPositionSelector by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var selectedPosition by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 显示拍照按钮
        if (!showPositionSelector && !showCamera) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { showPositionSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("拍照 (${photoMap.size}/9)")
                }
            }
        }

        // 新的输入界面
        NewRecordScreen(viewModel = viewModel)
    }

    // 位置选择对话框
    if (showPositionSelector) {
        Dialog(onDismissRequest = { showPositionSelector = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("选择拍照位置", style = MaterialTheme.typography.titleMedium)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(SurveyViewModel.PHOTO_POSITIONS) { position, label ->
                            val hasPhoto = photoMap.containsKey(position)
                            OutlinedButton(
                                onClick = {
                                    selectedPosition = position
                                    showPositionSelector = false
                                    showCamera = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label)
                                    Text(
                                        if (hasPhoto) "已拍" else "未拍",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (hasPhoto)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showPositionSelector = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("取消")
                    }
                }
            }
        }
    }

    // 相机对话框
    if (showCamera && selectedPosition >= 0) {
        Dialog(onDismissRequest = {
            showCamera = false
            selectedPosition = -1
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "拍照: ${SurveyViewModel.PHOTO_POSITIONS.getOrNull(selectedPosition) ?: ""}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    var takePhotoCallback: (() -> Unit)? by remember { mutableStateOf(null) }

                    CameraCapture(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        onPhotoSaved = { photoPath ->
                            viewModel.addPhotoAtPosition(selectedPosition, photoPath)
                            showCamera = false
                            selectedPosition = -1
                        },
                        onTakePhoto = { callback ->
                            takePhotoCallback = callback
                        }
                    )

                    // 拍照按钮和取消按钮
                    Button(
                        onClick = { takePhotoCallback?.invoke() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("拍照")
                    }

                    Button(
                        onClick = {
                            showCamera = false
                            selectedPosition = -1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

