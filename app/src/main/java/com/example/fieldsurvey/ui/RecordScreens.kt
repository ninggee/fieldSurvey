package com.example.fieldsurvey.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(viewModel: SurveyViewModel) {
    val records by viewModel.records.collectAsState()
    val filterStartDate by viewModel.filterStartDate.collectAsState()
    val filterEndDate by viewModel.filterEndDate.collectAsState()
    val filterMileageMin by viewModel.filterMileageMinText.collectAsState()
    val filterMileageMax by viewModel.filterMileageMaxText.collectAsState()
    val selectedRecordIds by viewModel.selectedRecordIds.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf(DatePickerTarget.START) }
    var exportMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("筛选条件", style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                datePickerTarget = DatePickerTarget.START
                showDatePicker = true
            }) {
                Text(filterStartDate?.toString() ?: "开始日期")
            }
            Button(onClick = {
                datePickerTarget = DatePickerTarget.END
                showDatePicker = true
            }) {
                Text(filterEndDate?.toString() ?: "结束日期")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filterMileageMin,
                onValueChange = viewModel::updateFilterMileageMinText,
                label = { Text("起始里程(如DK838+000)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterMileageMax,
                onValueChange = viewModel::updateFilterMileageMaxText,
                label = { Text("终止里程(如DK839+012.5)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::applyFilters) {
                Text("应用筛选")
            }
            Button(onClick = viewModel::clearFilters) {
                Text("清除筛选")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val fileName = "survey_${System.currentTimeMillis()}.xlsx"
                viewModel.exportCurrentList(context.contentResolver, fileName) { ok ->
                    exportMessage = if (ok) "已导出到下载目录" else "导出失败"
                }
            }) {
                Text("导出筛选")
            }
        }

        // 显示导出进度条
        if (exportProgress > 0 && exportProgress < 100) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("导出进度: $exportProgress%")
                LinearProgressIndicator(
                    progress = exportProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (exportMessage.isNotBlank()) {
            Text(exportMessage, color = MaterialTheme.colorScheme.primary)
        }

        // 多选操作栏
        if (records.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::selectAllRecords,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("全选(${selectedRecordIds.size}/${records.size})")
                }
                Button(
                    onClick = viewModel::clearSelection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清空选择")
                }
                if (selectedRecordIds.isNotEmpty()) {
                    Button(
                        onClick = viewModel::deleteSelectedRecords,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除(${selectedRecordIds.size})")
                    }
                }
            }
        }

        Text("记录列表", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${record.mileageDk}", style = MaterialTheme.typography.titleSmall)
                                Text(formatTime(record.createdAt), style = MaterialTheme.typography.bodySmall)
                            }
                            Checkbox(
                                checked = selectedRecordIds.contains(record.id),
                                onCheckedChange = { viewModel.toggleRecordSelection(record.id) }
                            )
                        }

                        // 显示照片
                        val photoPaths = record.photoPaths.split(";").filter { it.isNotBlank() }
                        if (photoPaths.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                photoPaths.take(3).forEach { photoPath ->
                                    Image(
                                        painter = rememberAsyncImagePainter(photoPath),
                                        contentDescription = "item photo",
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerStateFor(
            if (datePickerTarget == DatePickerTarget.START) filterStartDate else filterEndDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        if (datePickerTarget == DatePickerTarget.START) {
                            viewModel.updateFilterStartDate(date)
                        } else {
                            viewModel.updateFilterEndDate(date)
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



private fun formatTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberDatePickerStateFor(date: LocalDate?) =
    androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

@Composable
fun LogExportScreen(viewModel: SurveyViewModel) {
    val logFiles by viewModel.logFiles.collectAsState()
    val selectedLogFiles by viewModel.selectedLogFiles.collectAsState()
    val context = LocalContext.current

    var exportMessage by rememberSaveable { mutableStateOf("") }
    var outputFileName by rememberSaveable { mutableStateOf("logs_${System.currentTimeMillis()}.txt") }
    var isExporting by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("导出日志", style = MaterialTheme.typography.titleLarge)

        // 输出文件名输入框
        OutlinedTextField(
            value = outputFileName,
            onValueChange = { outputFileName = it },
            label = { Text("输出文件名") },
            modifier = Modifier.fillMaxWidth()
        )

        // 日志文件列表操作栏
        if (logFiles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = viewModel::selectAllLogFiles,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("全选(${selectedLogFiles.size}/${logFiles.size})")
                }
                Button(
                    onClick = viewModel::clearLogFileSelection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消选择")
                }
            }
        }

        // 日志文件列表
        Text("可导出的日志文件", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (logFiles.isEmpty()) {
                item {
                    Text("暂无日志文件", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(logFiles) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleLogFileSelection(file)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    file.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "修改时间: ${formatTime(file.lastModified())}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "大小: ${(file.length() / 1024).toInt()} KB",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Checkbox(
                                checked = selectedLogFiles.contains(file),
                                onCheckedChange = {
                                    viewModel.toggleLogFileSelection(file)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 导出和删除按钮
        if (logFiles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isExporting = true
                        viewModel.exportLogsToFile(
                            context.contentResolver,
                            outputFileName
                        ) { success ->
                            isExporting = false
                            exportMessage = if (success) "日志已导出到下载目录" else "日志导出失败"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isExporting && selectedLogFiles.isNotEmpty()
                ) {
                    Text(if (isExporting) "导出中..." else "导出选中日志")
                }

                if (selectedLogFiles.isNotEmpty()) {
                    Button(
                        onClick = viewModel::deleteSelectedLogFiles,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除(${selectedLogFiles.size})")
                    }
                }
            }
        }

        // 导出消息提示
        if (exportMessage.isNotBlank()) {
            Text(
                exportMessage,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // 刷新日志列表
    LaunchedEffect(Unit) {
        viewModel.loadLogFiles()
    }
}

