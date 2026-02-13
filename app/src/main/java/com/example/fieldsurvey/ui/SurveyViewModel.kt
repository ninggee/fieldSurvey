package com.example.fieldsurvey.ui

import android.app.Application
import android.content.ContentResolver
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fieldsurvey.data.SurveyDatabase
import com.example.fieldsurvey.data.SurveyRecord
import com.example.fieldsurvey.data.SurveyRepository
import com.example.fieldsurvey.export.ExcelExporter
import com.example.fieldsurvey.util.MileageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SurveyViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = SurveyRepository(SurveyDatabase.getInstance(app).surveyDao())

    // ==================== 输入界面状态 ====================

    // ==================== 里程相关状态 ====================

    // 里程千位部分
    private val _mileageKmText = MutableStateFlow("")
    val mileageKmText: StateFlow<String> = _mileageKmText.asStateFlow()

    // 里程小数部分
    private val _mileageDecimalText = MutableStateFlow("")
    val mileageDecimalText: StateFlow<String> = _mileageDecimalText.asStateFlow()

    // 当前完整DK格式
    private val _currentMileageDk = MutableStateFlow("DK0+000")
    val currentMileageDk: StateFlow<String> = _currentMileageDk.asStateFlow()

    // 当前编辑的记录ID（如果是编辑已有记录）
    private val _currentRecordId = MutableStateFlow<Long?>(null)
    val currentRecordId: StateFlow<Long?> = _currentRecordId.asStateFlow()

    // 照片位置定义（9个固定位置）
    companion object {
        val PHOTO_POSITIONS = listOf(
            "左轨擦伤病害",
            "左轨掉块病害",
            "左轨其它病害",
            "右轨擦伤病害",
            "右轨掉块病害",
            "右轨其它病害",
            "扣件病害",
            "轨枕病害",
            "道床状态"
        )
    }

    // 照片存储：Map<位置索引, 照片路径>
    private val _photoMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val photoMap: StateFlow<Map<Int, String>> = _photoMap.asStateFlow()

    // 是否类字段（可为 null）
    private val _hasChipping = MutableStateFlow<Boolean?>(null)
    val hasChipping: StateFlow<Boolean?> = _hasChipping.asStateFlow()

    private val _hasWear = MutableStateFlow<Boolean?>(null)
    val hasWear: StateFlow<Boolean?> = _hasWear.asStateFlow()

    private val _hasOther = MutableStateFlow<Boolean?>(null)
    val hasOther: StateFlow<Boolean?> = _hasOther.asStateFlow()

    private val _hasConcreteNewSegment = MutableStateFlow<Boolean?>(null)
    val hasConcreteNewSegment: StateFlow<Boolean?> = _hasConcreteNewSegment.asStateFlow()

    private val _hasSeamlessStart = MutableStateFlow<Boolean?>(null)
    val hasSeamlessStart: StateFlow<Boolean?> = _hasSeamlessStart.asStateFlow()

    private val _hasDerailmentStart = MutableStateFlow<Boolean?>(null)
    val hasDerailmentStart: StateFlow<Boolean?> = _hasDerailmentStart.asStateFlow()

    private val _hasSubgradeCompaction = MutableStateFlow<Boolean?>(null)
    val hasSubgradeCompaction: StateFlow<Boolean?> = _hasSubgradeCompaction.asStateFlow()

    private val _hasBallastedBeam = MutableStateFlow<Boolean?>(null)
    val hasBallastedBeam: StateFlow<Boolean?> = _hasBallastedBeam.asStateFlow()

    // 数值类字段
    private val _leftRailChippingDepthText = MutableStateFlow("")
    val leftRailChippingDepthText: StateFlow<String> = _leftRailChippingDepthText.asStateFlow()

    private val _rightRailChippingDepthText = MutableStateFlow("")
    val rightRailChippingDepthText: StateFlow<String> = _rightRailChippingDepthText.asStateFlow()

    private val _leftRailScratchDepthText = MutableStateFlow("")
    val leftRailScratchDepthText: StateFlow<String> = _leftRailScratchDepthText.asStateFlow()

    private val _rightRailScratchDepthText = MutableStateFlow("")
    val rightRailScratchDepthText: StateFlow<String> = _rightRailScratchDepthText.asStateFlow()

    private val _bedThicknessText = MutableStateFlow("")
    val bedThicknessText: StateFlow<String> = _bedThicknessText.asStateFlow()

    // 整数类字段
    private val _leftRailScratchCountText = MutableStateFlow("")
    val leftRailScratchCountText: StateFlow<String> = _leftRailScratchCountText.asStateFlow()

    private val _rightRailScratchCountText = MutableStateFlow("")
    val rightRailScratchCountText: StateFlow<String> = _rightRailScratchCountText.asStateFlow()

    private val _concreteSleeperDamageCountText = MutableStateFlow("")
    val concreteSleeperDamageCountText: StateFlow<String> = _concreteSleeperDamageCountText.asStateFlow()

    private val _woodenSleeperDamageCountText = MutableStateFlow("")
    val woodenSleeperDamageCountText: StateFlow<String> = _woodenSleeperDamageCountText.asStateFlow()

    private val _concreteClipFailureCountText = MutableStateFlow("")
    val concreteClipFailureCountText: StateFlow<String> = _concreteClipFailureCountText.asStateFlow()

    private val _woodenClipFailureCountText = MutableStateFlow("")
    val woodenClipFailureCountText: StateFlow<String> = _woodenClipFailureCountText.asStateFlow()

    private val _fishplateDefectCountText = MutableStateFlow("")
    val fishplateDefectCountText: StateFlow<String> = _fishplateDefectCountText.asStateFlow()

    private val _boltDefectCountText = MutableStateFlow("")
    val boltDefectCountText: StateFlow<String> = _boltDefectCountText.asStateFlow()

    private val _antiClimbGoodCountText = MutableStateFlow("")
    val antiClimbGoodCountText: StateFlow<String> = _antiClimbGoodCountText.asStateFlow()

    private val _antiClimbSupportGoodCountText = MutableStateFlow("")
    val antiClimbSupportGoodCountText: StateFlow<String> = _antiClimbSupportGoodCountText.asStateFlow()

    private val _gaugeBarGoodCountText = MutableStateFlow("")
    val gaugeBarGoodCountText: StateFlow<String> = _gaugeBarGoodCountText.asStateFlow()

    private val _steelRailSevereCountText = MutableStateFlow("")
    val steelRailSevereCountText: StateFlow<String> = _steelRailSevereCountText.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    // 保存成功事件（用于显示 Snackbar）
    private val _saveSuccessEvent = Channel<Unit>(Channel.BUFFERED)
    val saveSuccessEvent = _saveSuccessEvent.receiveAsFlow()

    // 记录无变化事件（用于显示"没有更新"提示）
    private val _noChangeEvent = Channel<Unit>(Channel.BUFFERED)
    val noChangeEvent = _noChangeEvent.receiveAsFlow()

    // 操作状态管理
    private val _isOperating = MutableStateFlow(false)
    val isOperating: StateFlow<Boolean> = _isOperating.asStateFlow()

    private val _records = MutableStateFlow<List<SurveyRecord>>(emptyList())
    val records: StateFlow<List<SurveyRecord>> = _records.asStateFlow()

    private val _filterStartDate = MutableStateFlow<LocalDate?>(null)
    val filterStartDate: StateFlow<LocalDate?> = _filterStartDate.asStateFlow()

    private val _filterEndDate = MutableStateFlow<LocalDate?>(null)
    val filterEndDate: StateFlow<LocalDate?> = _filterEndDate.asStateFlow()

    private val _filterMileageMinText = MutableStateFlow("")
    val filterMileageMinText: StateFlow<String> = _filterMileageMinText.asStateFlow()

    private val _filterMileageMaxText = MutableStateFlow("")
    val filterMileageMaxText: StateFlow<String> = _filterMileageMaxText.asStateFlow()


    // 记录选中状态
    private val _selectedRecordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedRecordIds: StateFlow<Set<Long>> = _selectedRecordIds.asStateFlow()

    // 导出进度和日志
    private val _exportProgress = MutableStateFlow(0)
    val exportProgress: StateFlow<Int> = _exportProgress.asStateFlow()

    private val _exportLog = MutableStateFlow<List<String>>(emptyList())
    val exportLog: StateFlow<List<String>> = _exportLog.asStateFlow()

    // 日志文件列表相关
    private val _logFiles = MutableStateFlow<List<java.io.File>>(emptyList())
    val logFiles: StateFlow<List<java.io.File>> = _logFiles.asStateFlow()

    private val _selectedLogFiles = MutableStateFlow<Set<java.io.File>>(emptySet())
    val selectedLogFiles: StateFlow<Set<java.io.File>> = _selectedLogFiles.asStateFlow()

    init {
        refreshList()
        loadLogFiles()
    }

    // ==================== 里程管理方法 ====================

    fun updateMileageKmText(value: String) {
        _mileageKmText.value = value
        updateDkDisplay()
    }

    fun updateMileageDecimalText(value: String) {
        _mileageDecimalText.value = value
        updateDkDisplay()

        // 当千位和小数部分都填写后，自动查询该里程的记录
        val km = _mileageKmText.value.toIntOrNull()
        val decimal = value.toDoubleOrNull()
        if (km != null && decimal != null) {
            loadRecordByMileage(km, decimal)
        }
    }

    private fun updateDkDisplay() {
        val km = _mileageKmText.value.toIntOrNull() ?: 0
        val decimal = _mileageDecimalText.value.toDoubleOrNull() ?: 0.0
        _currentMileageDk.value = MileageManager.generateDkString(km, decimal)
    }


    fun previousMileage() {
        // 如果正在操作，则直接返回
        if (_isOperating.value) {
            _error.value = "操作进行中，请稍候"
            return
        }

        val km = _mileageKmText.value.toIntOrNull() ?: 0
        val decimal = _mileageDecimalText.value.toDoubleOrNull() ?: 0.0

        val mileageInfo = MileageManager.getPreviousMileage(km, decimal)

        // 自动填充里程字段
        _mileageKmText.value = mileageInfo.km.toString()
        _mileageDecimalText.value = formatDecimal(mileageInfo.decimal)
        _currentMileageDk.value = mileageInfo.dkString

        // 尝试加载该里程的已有记录
        loadRecordByMileage(mileageInfo.km, mileageInfo.decimal)
    }

    fun nextMileage() {
        // 如果正在操作，则直接返回
        if (_isOperating.value) {
            _error.value = "操作进行中，请稍候"
            return
        }

        val km = _mileageKmText.value.toIntOrNull() ?: 0
        val decimal = _mileageDecimalText.value.toDoubleOrNull() ?: 0.0

        val mileageInfo = MileageManager.getNextMileage(km, decimal)

        // 自动填充里程字段
        _mileageKmText.value = mileageInfo.km.toString()
        _mileageDecimalText.value = formatDecimal(mileageInfo.decimal)
        _currentMileageDk.value = mileageInfo.dkString

        // 尝试加载该里程的已有记录
        loadRecordByMileage(mileageInfo.km, mileageInfo.decimal)
    }

    private fun loadRecordByMileage(km: Int, decimal: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingRecord = repository.findByMileage(km, decimal)
            withContext(Dispatchers.Main) {
                if (existingRecord != null) {
                    // 加载已有记录
                    loadRecordToForm(existingRecord)
                } else {
                    // 清空所有其他输入字段
                    clearAllFieldsExceptMileage()
                }
            }
        }
    }

    private fun loadRecordToForm(record: SurveyRecord) {
        _currentRecordId.value = record.id

        // 加载布尔值字段
        _hasChipping.value = record.hasChipping
        _hasWear.value = record.hasWear
        _hasOther.value = record.hasOther
        _hasConcreteNewSegment.value = record.hasConcreteNewSegment
        _hasSeamlessStart.value = record.hasSeamlessStart
        _hasDerailmentStart.value = record.hasDerailmentStart
        _hasSubgradeCompaction.value = record.hasSubgradeCompaction
        _hasBallastedBeam.value = record.hasBallastedBeam

        // 加载数值字段
        _leftRailChippingDepthText.value = record.leftRailChippingDepth?.toString() ?: ""
        _rightRailChippingDepthText.value = record.rightRailChippingDepth?.toString() ?: ""
        _leftRailScratchDepthText.value = record.leftRailScratchDepth?.toString() ?: ""
        _rightRailScratchDepthText.value = record.rightRailScratchDepth?.toString() ?: ""
        _bedThicknessText.value = record.bedThickness?.toString() ?: ""

        // 加载整数字段
        _leftRailScratchCountText.value = record.leftRailScratchCount?.toString() ?: ""
        _rightRailScratchCountText.value = record.rightRailScratchCount?.toString() ?: ""
        _concreteSleeperDamageCountText.value = record.concreteSleeperDamageCount?.toString() ?: ""
        _woodenSleeperDamageCountText.value = record.woodenSleeperDamageCount?.toString() ?: ""
        _concreteClipFailureCountText.value = record.concreteClipFailureCount?.toString() ?: ""
        _woodenClipFailureCountText.value = record.woodenClipFailureCount?.toString() ?: ""
        _fishplateDefectCountText.value = record.fishplateDefectCount?.toString() ?: ""
        _boltDefectCountText.value = record.boltDefectCount?.toString() ?: ""
        _antiClimbGoodCountText.value = record.antiClimbGoodCount?.toString() ?: ""
        _antiClimbSupportGoodCountText.value = record.antiClimbSupportGoodCount?.toString() ?: ""
        _gaugeBarGoodCountText.value = record.gaugeBarGoodCount?.toString() ?: ""
        _steelRailSevereCountText.value = record.steelRailSevereCount?.toString() ?: ""

        // 加载照片（解析分号分隔的字符串）
        val photoPathsList = record.photoPaths.split(";")
        val photoMapData = mutableMapOf<Int, String>()
        photoPathsList.forEachIndexed { index, path ->
            if (path.isNotBlank()) {
                photoMapData[index] = path
            }
        }
        _photoMap.value = photoMapData
    }

    private fun formatDecimal(decimal: Double): String {
        return if (decimal % 1 == 0.0) {
            decimal.toInt().toString()
        } else {
            decimal.toString()
        }
    }

    private fun clearAllFieldsExceptMileage() {
        // 清空记录ID
        _currentRecordId.value = null

        // 清空照片
        _photoMap.value = emptyMap()

        // 清空布尔值字段
        _hasChipping.value = null
        _hasWear.value = null
        _hasOther.value = null
        _hasConcreteNewSegment.value = null
        _hasSeamlessStart.value = null
        _hasDerailmentStart.value = null
        _hasSubgradeCompaction.value = null
        _hasBallastedBeam.value = null

        // 清空数值字段
        _leftRailChippingDepthText.value = ""
        _rightRailChippingDepthText.value = ""
        _leftRailScratchDepthText.value = ""
        _rightRailScratchDepthText.value = ""
        _bedThicknessText.value = ""

        // 清空整数字段
        _leftRailScratchCountText.value = ""
        _rightRailScratchCountText.value = ""
        _concreteSleeperDamageCountText.value = ""
        _woodenSleeperDamageCountText.value = ""
        _concreteClipFailureCountText.value = ""
        _woodenClipFailureCountText.value = ""
        _fishplateDefectCountText.value = ""
        _boltDefectCountText.value = ""
        _antiClimbGoodCountText.value = ""
        _antiClimbSupportGoodCountText.value = ""
        _gaugeBarGoodCountText.value = ""
        _steelRailSevereCountText.value = ""
    }


    // ==================== 照片管理方法 ====================

    fun addPhotoAtPosition(position: Int, path: String) {
        val currentMap = _photoMap.value.toMutableMap()
        currentMap[position] = path
        _photoMap.value = currentMap
    }

    fun removePhotoAtPosition(position: Int) {
        val currentMap = _photoMap.value.toMutableMap()
        currentMap.remove(position)
        _photoMap.value = currentMap
    }

    fun getPhotoAtPosition(position: Int): String? = _photoMap.value[position]

    fun getPhotoCount(): Int = _photoMap.value.size

    // ==================== 字段更新方法 ====================

    fun updateHasChipping(value: Boolean?) {
        _hasChipping.value = value
    }

    fun updateHasWear(value: Boolean?) {
        _hasWear.value = value
    }

    fun updateHasOther(value: Boolean?) {
        _hasOther.value = value
    }

    fun updateHasConcreteNewSegment(value: Boolean?) {
        _hasConcreteNewSegment.value = value
    }

    fun updateHasSeamlessStart(value: Boolean?) {
        _hasSeamlessStart.value = value
    }

    fun updateHasDerailmentStart(value: Boolean?) {
        _hasDerailmentStart.value = value
    }

    fun updateHasSubgradeCompaction(value: Boolean?) {
        _hasSubgradeCompaction.value = value
    }

    fun updateHasBallastedBeam(value: Boolean?) {
        _hasBallastedBeam.value = value
    }

    fun updateLeftRailChippingDepthText(value: String) {
        _leftRailChippingDepthText.value = value
    }

    fun updateRightRailChippingDepthText(value: String) {
        _rightRailChippingDepthText.value = value
    }

    fun updateLeftRailScratchDepthText(value: String) {
        _leftRailScratchDepthText.value = value
    }

    fun updateRightRailScratchDepthText(value: String) {
        _rightRailScratchDepthText.value = value
    }

    fun updateBedThicknessText(value: String) {
        _bedThicknessText.value = value
    }

    fun updateLeftRailScratchCountText(value: String) {
        _leftRailScratchCountText.value = value
    }

    fun updateRightRailScratchCountText(value: String) {
        _rightRailScratchCountText.value = value
    }

    fun updateConcreteSleeperDamageCountText(value: String) {
        _concreteSleeperDamageCountText.value = value
    }

    fun updateWoodenSleeperDamageCountText(value: String) {
        _woodenSleeperDamageCountText.value = value
    }

    fun updateConcreteClipFailureCountText(value: String) {
        _concreteClipFailureCountText.value = value
    }

    fun updateWoodenClipFailureCountText(value: String) {
        _woodenClipFailureCountText.value = value
    }

    fun updateFishplateDefectCountText(value: String) {
        _fishplateDefectCountText.value = value
    }

    fun updateBoltDefectCountText(value: String) {
        _boltDefectCountText.value = value
    }

    fun updateAntiClimbGoodCountText(value: String) {
        _antiClimbGoodCountText.value = value
    }

    fun updateAntiClimbSupportGoodCountText(value: String) {
        _antiClimbSupportGoodCountText.value = value
    }

    fun updateGaugeBarGoodCountText(value: String) {
        _gaugeBarGoodCountText.value = value
    }

    fun updateSteelRailSevereCountText(value: String) {
        _steelRailSevereCountText.value = value
    }

    // ==================== 照片管理方法 ====================

    fun saveRecord() {
        // 如果正在操作，则直接返回
        if (_isOperating.value) {
            _error.value = "操作进行中，请稍候"
            return
        }

        val km = _mileageKmText.value.toIntOrNull()
        if (km == null) {
            _error.value = "请输入里程千位"
            return
        }

        val decimal = _mileageDecimalText.value.toDoubleOrNull() ?: 0.0

        _error.value = ""

        // 将照片 Map 转换为按位置排序的字符串（空位置用空字符串表示）
        val photoPathsStr = (0..8).map { position ->
            _photoMap.value[position] ?: ""
        }.joinToString(";")

        val currentId = _currentRecordId.value

        val record = SurveyRecord(
            id = currentId ?: 0,  // 如果是更新，使用现有ID；否则为0（自动生成）
            mileageKm = km,
            mileageDecimal = decimal,
            mileageDk = _currentMileageDk.value,
            hasChipping = _hasChipping.value,
            hasWear = _hasWear.value,
            hasOther = _hasOther.value,
            hasConcreteNewSegment = _hasConcreteNewSegment.value,
            hasSeamlessStart = _hasSeamlessStart.value,
            hasDerailmentStart = _hasDerailmentStart.value,
            hasSubgradeCompaction = _hasSubgradeCompaction.value,
            hasBallastedBeam = _hasBallastedBeam.value,
            leftRailChippingDepth = _leftRailChippingDepthText.value.toDoubleOrNull(),
            rightRailChippingDepth = _rightRailChippingDepthText.value.toDoubleOrNull(),
            leftRailScratchDepth = _leftRailScratchDepthText.value.toDoubleOrNull(),
            rightRailScratchDepth = _rightRailScratchDepthText.value.toDoubleOrNull(),
            bedThickness = _bedThicknessText.value.toDoubleOrNull(),
            leftRailScratchCount = _leftRailScratchCountText.value.toIntOrNull(),
            rightRailScratchCount = _rightRailScratchCountText.value.toIntOrNull(),
            concreteSleeperDamageCount = _concreteSleeperDamageCountText.value.toIntOrNull(),
            woodenSleeperDamageCount = _woodenSleeperDamageCountText.value.toIntOrNull(),
            concreteClipFailureCount = _concreteClipFailureCountText.value.toIntOrNull(),
            woodenClipFailureCount = _woodenClipFailureCountText.value.toIntOrNull(),
            fishplateDefectCount = _fishplateDefectCountText.value.toIntOrNull(),
            boltDefectCount = _boltDefectCountText.value.toIntOrNull(),
            antiClimbGoodCount = _antiClimbGoodCountText.value.toIntOrNull(),
            antiClimbSupportGoodCount = _antiClimbSupportGoodCountText.value.toIntOrNull(),
            gaugeBarGoodCount = _gaugeBarGoodCountText.value.toIntOrNull(),
            steelRailSevereCount = _steelRailSevereCountText.value.toIntOrNull(),
            photoPaths = photoPathsStr,
            createdAt = System.currentTimeMillis()
        )

        // ✅ 设置操作状态为进行中
        _isOperating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = repository.findByMileage(km, decimal)

                when {
                    existing != null -> {
                        // 同里程已存在，比较内容
                        if (isRecordContentEqual(existing, record)) {
                            // ✅ 内容相同，不更新数据库，发送"无变化"事件
                            _noChangeEvent.send(Unit)
                        } else {
                            // ✅ 内容不同，更新记录，发送"成功"事件
                            repository.update(record.copy(id = existing.id))
                            withContext(Dispatchers.Main) {
                                refreshList()
                            }
                            _saveSuccessEvent.send(Unit)
                        }
                    }
                    else -> {
                        // ✅ 不存在，插入新记录，发送"成功"事件
                        repository.insert(record)
                        withContext(Dispatchers.Main) {
                            refreshList()
                        }
                        _saveSuccessEvent.send(Unit)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "保存失败: ${e.message}"
                }
            } finally {
                // ✅ 操作完成后，无论成功还是失败，都要设置状态为完成
                withContext(Dispatchers.Main) {
                    _isOperating.value = false
                }
            }
        }
    }

    /**
     * 比较两条记录的内容是否相同（除了 id 和 createdAt）
     */
    private fun isRecordContentEqual(record1: SurveyRecord, record2: SurveyRecord): Boolean {
        return record1.mileageKm == record2.mileageKm &&
                record1.mileageDecimal == record2.mileageDecimal &&
                record1.mileageDk == record2.mileageDk &&
                record1.hasChipping == record2.hasChipping &&
                record1.hasWear == record2.hasWear &&
                record1.hasOther == record2.hasOther &&
                record1.hasConcreteNewSegment == record2.hasConcreteNewSegment &&
                record1.hasSeamlessStart == record2.hasSeamlessStart &&
                record1.hasDerailmentStart == record2.hasDerailmentStart &&
                record1.hasSubgradeCompaction == record2.hasSubgradeCompaction &&
                record1.hasBallastedBeam == record2.hasBallastedBeam &&
                record1.leftRailChippingDepth == record2.leftRailChippingDepth &&
                record1.rightRailChippingDepth == record2.rightRailChippingDepth &&
                record1.leftRailScratchDepth == record2.leftRailScratchDepth &&
                record1.rightRailScratchDepth == record2.rightRailScratchDepth &&
                record1.bedThickness == record2.bedThickness &&
                record1.leftRailScratchCount == record2.leftRailScratchCount &&
                record1.rightRailScratchCount == record2.rightRailScratchCount &&
                record1.concreteSleeperDamageCount == record2.concreteSleeperDamageCount &&
                record1.woodenSleeperDamageCount == record2.woodenSleeperDamageCount &&
                record1.concreteClipFailureCount == record2.concreteClipFailureCount &&
                record1.woodenClipFailureCount == record2.woodenClipFailureCount &&
                record1.fishplateDefectCount == record2.fishplateDefectCount &&
                record1.boltDefectCount == record2.boltDefectCount &&
                record1.antiClimbGoodCount == record2.antiClimbGoodCount &&
                record1.antiClimbSupportGoodCount == record2.antiClimbSupportGoodCount &&
                record1.gaugeBarGoodCount == record2.gaugeBarGoodCount &&
                record1.steelRailSevereCount == record2.steelRailSevereCount &&
                record1.photoPaths == record2.photoPaths
    }


    // ==================== 列表筛选方法 ====================

    fun updateFilterStartDate(value: LocalDate?) {
        _filterStartDate.value = value
    }

    fun updateFilterEndDate(value: LocalDate?) {
        _filterEndDate.value = value
    }

    fun updateFilterMileageMinText(value: String) {
        _filterMileageMinText.value = value
    }

    fun updateFilterMileageMaxText(value: String) {
        _filterMileageMaxText.value = value
    }


    // 记录选择方法
    fun toggleRecordSelection(recordId: Long) {
        val current = _selectedRecordIds.value.toMutableSet()
        if (current.contains(recordId)) {
            current.remove(recordId)
        } else {
            current.add(recordId)
        }
        _selectedRecordIds.value = current
    }

    fun selectAllRecords() {
        _selectedRecordIds.value = _records.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedRecordIds.value = emptySet()
    }

    fun deleteSelectedRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _selectedRecordIds.value.forEach { id ->
                    repository.deleteById(id)
                }
                withContext(Dispatchers.Main) {
                    _selectedRecordIds.value = emptySet()
                    refreshList()
                    // ✅ 用 send() 代替 trySend()
                    _saveSuccessEvent.send(Unit)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "删除失败: ${e.message}"
                }
            }
        }
    }

    fun applyFilters() {
        refreshList()
    }

    fun clearFilters() {
        _filterStartDate.value = null
        _filterEndDate.value = null
        _filterMileageMinText.value = ""
        _filterMileageMaxText.value = ""
        refreshList()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportCurrentList(resolver: ContentResolver, fileName: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logs = mutableListOf<String>()
                logs.add("========== 导出开始 ==========")
                logs.add("时间: ${System.currentTimeMillis()}")
                logs.add("文件名: $fileName")

                // 需求 4：按里程排序
                val list = _records.value
                    .sortedBy { it.mileageKm + it.mileageDecimal }

                logs.add("待导出记录数: ${list.size}")
                _exportLog.value = logs
                _exportProgress.value = 10

                // 调用导出器，传递进度回调
                val ok = ExcelExporter.export(resolver, list, fileName) { progress ->
                    _exportProgress.value = progress
                    val updatedLogs = logs.toMutableList()
                    updatedLogs.add("导出进度: $progress%")
                    _exportLog.value = updatedLogs
                }

                withContext(Dispatchers.Main) {
                    if (ok) {
                        logs.add("导出成功")
                        _exportProgress.value = 100
                    } else {
                        logs.add("导出失败")
                        _exportProgress.value = -1
                    }
                    logs.add("========== 导出结束 ==========")
                    _exportLog.value = logs

                    // 保存日志到文件
                    saveLogsToFile(logs)

                    onDone(ok)
                }
            } catch (e: Exception) {
                val logs = _exportLog.value.toMutableList()
                logs.add("导出异常: ${e.message}")
                logs.add(e.stackTraceToString())
                logs.add("========== 导出结束(异常) ==========")
                withContext(Dispatchers.Main) {
                    _exportLog.value = logs
                    _exportProgress.value = -1
                    saveLogsToFile(logs)
                    onDone(false)
                }
            }
        }
    }

    private fun saveLogsToFile(logs: List<String>) {
        try {
            val context = getApplication<Application>()
            val logsDir = java.io.File(context.filesDir, "export_logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }

            val logFile = java.io.File(logsDir, "export_log_${System.currentTimeMillis()}.txt")
            logFile.writeText(logs.joinToString("\n"))
        } catch (e: Exception) {
            // 日志保存失败，不影响主流程
            e.printStackTrace()
        }
    }

    private fun refreshList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.listAll()
            val filtered = applyFilterRules(list)
            withContext(Dispatchers.Main) {
                _records.value = filtered
            }
        }
    }

    private fun applyFilterRules(records: List<SurveyRecord>): List<SurveyRecord> {
        val zone = ZoneId.systemDefault()
        val startDate = _filterStartDate.value
        val endDate = _filterEndDate.value

        // 解析里程范围
        val minMileage = parseMileage(_filterMileageMinText.value)
        val maxMileage = parseMileage(_filterMileageMaxText.value)

        val startMillis = startDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val endMillis = endDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()?.minus(1)

        return records.filter { record ->
            if (startMillis != null && record.createdAt < startMillis) return@filter false
            if (endMillis != null && record.createdAt > endMillis) return@filter false

            val recordMileage = record.mileageKm + record.mileageDecimal

            if (minMileage != null && recordMileage < minMileage) return@filter false
            if (maxMileage != null && recordMileage > maxMileage) return@filter false
            true
        }
    }

    // 解析DK格式里程（如 "DK838+012.5"） 返回总里程数（如 838.0125）
    private fun parseMileage(dkString: String): Double? {
        if (dkString.isBlank()) return null

        return try {
            val normalized = dkString.trim().uppercase()
            // 移除 "DK" 前缀
            val withoutDK = if (normalized.startsWith("DK")) {
                normalized.substring(2)
            } else {
                normalized
            }

            // 处理 "+" 符号
            val parts = if (withoutDK.contains("+")) {
                withoutDK.split("+")
            } else {
                listOf(withoutDK)
            }

            val kmPart = parts[0].toIntOrNull() ?: return null
            val decimalPart = if (parts.size > 1) {
                parts[1].toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }

            (kmPart + decimalPart / 1000.0)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== 日志文件管理 ====================

    fun loadLogFiles() {
        try {
            val context = getApplication<Application>()
            val logsDir = java.io.File(context.filesDir, "export_logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }

            val files = logsDir.listFiles()?.filter { it.isFile && it.extension == "txt" }?.sortedByDescending { it.lastModified() } ?: emptyList()
            _logFiles.value = files
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleLogFileSelection(file: java.io.File) {
        val current = _selectedLogFiles.value.toMutableSet()
        if (current.contains(file)) {
            current.remove(file)
        } else {
            current.add(file)
        }
        _selectedLogFiles.value = current
    }

    fun selectAllLogFiles() {
        _selectedLogFiles.value = _logFiles.value.toSet()
    }

    fun clearLogFileSelection() {
        _selectedLogFiles.value = emptySet()
    }

    fun deleteSelectedLogFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _selectedLogFiles.value.forEach { file ->
                    file.delete()
                }
                withContext(Dispatchers.Main) {
                    _selectedLogFiles.value = emptySet()
                    loadLogFiles()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportLogsToFile(resolver: android.content.ContentResolver, outputFileName: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logsToExport = if (_selectedLogFiles.value.isNotEmpty()) {
                    _selectedLogFiles.value.toList()
                } else {
                    _logFiles.value
                }

                if (logsToExport.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onDone(false)
                    }
                    return@launch
                }

                // 合并所有选中的日志内容
                val combinedLogs = mutableListOf<String>()
                combinedLogs.add("========== 日志导出 ==========")
                combinedLogs.add("导出时间: ${System.currentTimeMillis()}")
                combinedLogs.add("导出日志数: ${logsToExport.size}")
                combinedLogs.add("")

                logsToExport.forEach { file ->
                    combinedLogs.add("【${file.name}】")
                    combinedLogs.addAll(file.readLines())
                    combinedLogs.add("")
                }

                combinedLogs.add("========== 日志导出结束 ==========")

                // 保存到下载目录
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, outputFileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/plain")
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        output.write(combinedLogs.joinToString("\n").toByteArray())
                    }
                    withContext(Dispatchers.Main) {
                        onDone(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onDone(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onDone(false)
                }
            }
        }
    }
}
