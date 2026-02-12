package com.example.fieldsurvey.ui

import android.app.Application
import android.content.ContentResolver
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

    // 保存成功事件（用于显示 Toast）
    private val _saveSuccessEvent = Channel<Unit>()
    val saveSuccessEvent = _saveSuccessEvent.receiveAsFlow()

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

    init {
        refreshList()
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

    fun nextMileage() {
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

    fun previousMileage() {
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentId != null && currentId > 0) {
                    // 编辑已有记录，检查内容是否改变
                    val existingRecord = repository.findByMileage(km, decimal)
                    if (existingRecord != null && isRecordContentEqual(existingRecord, record)) {
                        // 内容相同，只显示 Toast，不保存
                        _saveSuccessEvent.trySend(Unit)
                    } else {
                        // 内容不同，更新记录
                        repository.update(record)
                        withContext(Dispatchers.Main) {
                            _saveSuccessEvent.trySend(Unit)
                            refreshList()
                        }
                    }
                } else {
                    // 新记录，先检查是否已存在同里程的记录
                    val existingRecord = repository.findByMileage(km, decimal)
                    if (existingRecord != null) {
                        // 同里程已存在，比较内容
                        if (isRecordContentEqual(existingRecord, record)) {
                            // 内容相同，只显示 Toast，不保存
                            _saveSuccessEvent.trySend(Unit)
                        } else {
                            // 内容不同，更新记录
                            repository.update(record.copy(id = existingRecord.id))
                            withContext(Dispatchers.Main) {
                                _saveSuccessEvent.trySend(Unit)
                                refreshList()
                            }
                        }
                    } else {
                        // 不存在，插入新记录
                        repository.insert(record)
                        withContext(Dispatchers.Main) {
                            _saveSuccessEvent.trySend(Unit)
                            refreshList()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "保存失败: ${e.message}"
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

    fun exportCurrentList(resolver: ContentResolver, fileName: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _records.value
            val ok = ExcelExporter.export(resolver, list, fileName)
            withContext(Dispatchers.Main) {
                onDone(ok)
            }
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
        val minMileage = _filterMileageMinText.value.toIntOrNull()
        val maxMileage = _filterMileageMaxText.value.toIntOrNull()

        val startMillis = startDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val endMillis = endDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()?.minus(1)

        return records.filter { record ->
            if (startMillis != null && record.createdAt < startMillis) return@filter false
            if (endMillis != null && record.createdAt > endMillis) return@filter false
            if (minMileage != null && record.mileageKm < minMileage) return@filter false
            if (maxMileage != null && record.mileageKm > maxMileage) return@filter false
            true
        }
    }
}
