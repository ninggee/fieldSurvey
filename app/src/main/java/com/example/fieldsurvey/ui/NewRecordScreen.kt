package com.example.fieldsurvey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NewRecordScreen(viewModel: SurveyViewModel) {
    val mileageKm by viewModel.mileageKmText.collectAsState()
    val mileageDecimal by viewModel.mileageDecimalText.collectAsState()
    val currentMileageDk by viewModel.currentMileageDk.collectAsState()
    val photoMap by viewModel.photoMap.collectAsState()
    val error by viewModel.error.collectAsState()

    val hasChipping by viewModel.hasChipping.collectAsState()
    val hasWear by viewModel.hasWear.collectAsState()
    val hasOther by viewModel.hasOther.collectAsState()
    val hasConcreteNewSegment by viewModel.hasConcreteNewSegment.collectAsState()
    val hasSeamlessStart by viewModel.hasSeamlessStart.collectAsState()
    val hasDerailmentStart by viewModel.hasDerailmentStart.collectAsState()
    val hasSubgradeCompaction by viewModel.hasSubgradeCompaction.collectAsState()
    val hasBallastedBeam by viewModel.hasBallastedBeam.collectAsState()

    val leftRailChippingDepth by viewModel.leftRailChippingDepthText.collectAsState()
    val rightRailChippingDepth by viewModel.rightRailChippingDepthText.collectAsState()
    val leftRailScratchDepth by viewModel.leftRailScratchDepthText.collectAsState()
    val rightRailScratchDepth by viewModel.rightRailScratchDepthText.collectAsState()
    val bedThickness by viewModel.bedThicknessText.collectAsState()

    val leftRailScratchCount by viewModel.leftRailScratchCountText.collectAsState()
    val rightRailScratchCount by viewModel.rightRailScratchCountText.collectAsState()
    val concreteSleeperDamageCount by viewModel.concreteSleeperDamageCountText.collectAsState()
    val woodenSleeperDamageCount by viewModel.woodenSleeperDamageCountText.collectAsState()
    val concreteClipFailureCount by viewModel.concreteClipFailureCountText.collectAsState()
    val woodenClipFailureCount by viewModel.woodenClipFailureCountText.collectAsState()
    val fishplateDefectCount by viewModel.fishplateDefectCountText.collectAsState()
    val boltDefectCount by viewModel.boltDefectCountText.collectAsState()
    val antiClimbGoodCount by viewModel.antiClimbGoodCountText.collectAsState()
    val antiClimbSupportGoodCount by viewModel.antiClimbSupportGoodCountText.collectAsState()
    val gaugeBarGoodCount by viewModel.gaugeBarGoodCountText.collectAsState()
    val steelRailSevereCount by viewModel.steelRailSevereCountText.collectAsState()

    var showPhotoPreview by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 监听保存成功事件
    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collectLatest {
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("现场记录", style = MaterialTheme.typography.titleLarge)

        // ==================== 照片框部分 ====================
        Text("照片 (${photoMap.size}/9)", style = MaterialTheme.typography.titleMedium)

        PhotoGridWithLabels(
            photoMap = photoMap,
            photoPositions = SurveyViewModel.PHOTO_POSITIONS,
            onPhotoClick = { showPhotoPreview = it },
            onRemovePhotoClick = { position -> viewModel.removePhotoAtPosition(position) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== 钢轨重伤个数 ====================
        OutlinedTextField(
            value = steelRailSevereCount,
            onValueChange = { viewModel.updateSteelRailSevereCountText(it) },
            label = { Text("钢轨重伤个数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== 里程部分 ====================
        Text("里程信息", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = mileageKm,
                onValueChange = { viewModel.updateMileageKmText(it) },
                label = { Text("千位") },
                placeholder = { Text("如: 838") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Text("+", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = mileageDecimal,
                onValueChange = { viewModel.updateMileageDecimalText(it) },
                label = { Text("其他位") },
                placeholder = { Text("如: 12.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            "完整里程: $currentMileageDk",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = viewModel::previousMileage, modifier = Modifier.weight(1f)) {
                Text("上一个")
            }
            OutlinedButton(onClick = viewModel::nextMileage, modifier = Modifier.weight(1f)) {
                Text("下一个")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== 状态字段 ====================
        Text("状态信息", style = MaterialTheme.typography.titleMedium)

        BooleanField("掉块擦伤", hasChipping) { viewModel.updateHasChipping(it) }
        BooleanField("磨损", hasWear) { viewModel.updateHasWear(it) }
        BooleanField("其他", hasOther) { viewModel.updateHasOther(it) }
        BooleanField("砼枕换新段落", hasConcreteNewSegment) { viewModel.updateHasConcreteNewSegment(it) }
        BooleanField("无缝线路起始里程", hasSeamlessStart) { viewModel.updateHasSeamlessStart(it) }
        BooleanField("脱线事故起始里程", hasDerailmentStart) { viewModel.updateHasDerailmentStart(it) }
        BooleanField("道床严重板结段落", hasSubgradeCompaction) { viewModel.updateHasSubgradeCompaction(it) }
        BooleanField("有砟梁/明桥面木枕段落", hasBallastedBeam) { viewModel.updateHasBallastedBeam(it) }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== 轨道相关字段 ====================
        Text("轨道信息", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = leftRailChippingDepth,
                onValueChange = { viewModel.updateLeftRailChippingDepthText(it) },
                label = { Text("左轨头掉块深度(m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = rightRailChippingDepth,
                onValueChange = { viewModel.updateRightRailChippingDepthText(it) },
                label = { Text("右轨头掉块深度(m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = leftRailScratchDepth,
                onValueChange = { viewModel.updateLeftRailScratchDepthText(it) },
                label = { Text("左轨擦伤深度(m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = leftRailScratchCount,
                onValueChange = { viewModel.updateLeftRailScratchCountText(it) },
                label = { Text("左轨擦伤个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rightRailScratchDepth,
                onValueChange = { viewModel.updateRightRailScratchDepthText(it) },
                label = { Text("右轨擦伤深度(m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = rightRailScratchCount,
                onValueChange = { viewModel.updateRightRailScratchCountText(it) },
                label = { Text("右轨擦伤个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== 枕木相关字段 ====================
        Text("枕木信息", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = concreteSleeperDamageCount,
                onValueChange = { viewModel.updateConcreteSleeperDamageCountText(it) },
                label = { Text("砼枕严重伤损个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = woodenSleeperDamageCount,
                onValueChange = { viewModel.updateWoodenSleeperDamageCountText(it) },
                label = { Text("木枕严重伤损个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = concreteClipFailureCount,
                onValueChange = { viewModel.updateConcreteClipFailureCountText(it) },
                label = { Text("砼枕扣件失效套数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = woodenClipFailureCount,
                onValueChange = { viewModel.updateWoodenClipFailureCountText(it) },
                label = { Text("木枕扣件失效套数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== 其他设备信息 ====================
        Text("其他设备", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = bedThickness,
            onValueChange = { viewModel.updateBedThicknessText(it) },
            label = { Text("道床厚度(m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fishplateDefectCount,
                onValueChange = { viewModel.updateFishplateDefectCountText(it) },
                label = { Text("夹板接头伤损个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = boltDefectCount,
                onValueChange = { viewModel.updateBoltDefectCountText(it) },
                label = { Text("螺栓伤损个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = antiClimbGoodCount,
                onValueChange = { viewModel.updateAntiClimbGoodCountText(it) },
                label = { Text("防爬器完好个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = antiClimbSupportGoodCount,
                onValueChange = { viewModel.updateAntiClimbSupportGoodCountText(it) },
                label = { Text("防爬支撑完好个数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = gaugeBarGoodCount,
            onValueChange = { viewModel.updateGaugeBarGoodCountText(it) },
            label = { Text("轨距杆完好个数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(onClick = viewModel::saveRecord, modifier = Modifier.fillMaxWidth()) {
            Text("保存记录")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showPhotoPreview != null) {
        PhotoPreviewDialog(
            photoPath = showPhotoPreview!!,
            onDismiss = { showPhotoPreview = null }
        )
    }
}

@Composable
private fun PhotoGridWithLabels(
    photoMap: Map<Int, String>,
    photoPositions: List<String>,
    onPhotoClick: (String) -> Unit,
    onRemovePhotoClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0..2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..2) {
                    val position = row * 3 + col
                    PhotoSlotWithLabel(
                        position = position,
                        label = photoPositions.getOrNull(position) ?: "",
                        photoPath = photoMap[position],
                        onPhotoClick = { onPhotoClick(it) },
                        onRemoveClick = { onRemovePhotoClick(position) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSlotWithLabel(
    position: Int,
    label: String,
    photoPath: String?,
    onPhotoClick: (String) -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable {
                    if (photoPath != null) {
                        onPhotoClick(photoPath)
                    }
                }
        ) {
            if (photoPath != null) {
                Box {
                    AsyncImage(
                        model = photoPath,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "删除",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "空",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BooleanField(
    label: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)

        Checkbox(
            checked = value == true,
            onCheckedChange = { isChecked ->
                onValueChange(if (isChecked) true else null)
            }
        )
    }
}

@Composable
private fun PhotoPreviewDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = photoPath,
                contentDescription = "photo preview",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}

