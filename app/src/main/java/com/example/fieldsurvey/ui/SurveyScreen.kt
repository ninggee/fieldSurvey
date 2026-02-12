package com.example.fieldsurvey.ui

import androidx.compose.foundation.Image
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
fun SurveyScreen(viewModel: SurveyViewModel) {
    val lineType by viewModel.lineType.collectAsState()
    val mileageText by viewModel.mileageText.collectAsState()
    val depthText by viewModel.depthText.collectAsState()
    val photoPath by viewModel.photoPath.collectAsState()
    val records by viewModel.records.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterDate by viewModel.filterDate.collectAsState()
    val filterStartDate by viewModel.filterStartDate.collectAsState()
    val filterEndDate by viewModel.filterEndDate.collectAsState()
    val filterLineType by viewModel.filterLineType.collectAsState()
    val filterMileageMin by viewModel.filterMileageMinText.collectAsState()
    val filterMileageMax by viewModel.filterMileageMaxText.collectAsState()
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
        Text("现场记录", style = MaterialTheme.typography.titleLarge)

        if (photoPath.isBlank()) {
            CameraCapture(
                modifier = Modifier.fillMaxWidth(),
                onPhotoSaved = { viewModel.updatePhotoPath(it) }
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(photoPath),
                contentDescription = "photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.updatePhotoPath("") }) {
                    Text("重拍")
                }
            }
        }

        LineTypeField(lineType = lineType, onChanged = viewModel::updateLineType)

        OutlinedTextField(
            value = mileageText,
            onValueChange = viewModel::updateMileageText,
            label = { Text("里程 (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        val dk = viewModel.dkPreview()
        if (dk.isNotBlank()) {
            Text("DK: $dk")
        }

        OutlinedTextField(
            value = depthText,
            onValueChange = viewModel::updateDepthText,
            label = { Text("深度 (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = viewModel::saveRecord, modifier = Modifier.fillMaxWidth()) {
            Text("保存")
        }

        Text("筛选条件", style = MaterialTheme.typography.titleMedium)
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

        LineTypeFilterField(lineType = filterLineType, onChanged = viewModel::updateFilterLineType)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filterMileageMin,
                onValueChange = viewModel::updateFilterMileageMinText,
                label = { Text("里程最小") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterMileageMax,
                onValueChange = viewModel::updateFilterMileageMaxText,
                label = { Text("里程最大") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        if (exportMessage.isNotBlank()) {
            Text(exportMessage, color = MaterialTheme.colorScheme.primary)
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
                        Text("${record.lineType}  ${record.mileageDk}  深度 ${record.depthM}m")
                        Text(formatTime(record.createdAt))
                        if (record.photoPath.isNotBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(record.photoPath),
                                contentDescription = "item photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
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

enum class DatePickerTarget {
    START,
    END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineTypeField(lineType: String, onChanged: (String) -> Unit) {
    val options = listOf("左线", "右线")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = lineType,
            onValueChange = {},
            readOnly = true,
            label = { Text("线别") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineTypeFilterField(lineType: String, onChanged: (String) -> Unit) {
    val options = listOf("全部", "左线", "右线")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = lineType,
            onValueChange = {},
            readOnly = true,
            label = { Text("线别筛选") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onChanged(option)
                        expanded = false
                    }
                )
            }
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
