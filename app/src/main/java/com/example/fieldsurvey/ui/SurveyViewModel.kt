package com.example.fieldsurvey.ui

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fieldsurvey.data.SurveyDatabase
import com.example.fieldsurvey.data.SurveyRecord
import com.example.fieldsurvey.data.SurveyRepository
import com.example.fieldsurvey.export.ExcelExporter
import com.example.fieldsurvey.util.toDk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SurveyViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = SurveyRepository(SurveyDatabase.getInstance(app).surveyDao())

    private val _lineType = MutableStateFlow("左线")
    val lineType: StateFlow<String> = _lineType.asStateFlow()

    private val _mileageText = MutableStateFlow("")
    val mileageText: StateFlow<String> = _mileageText.asStateFlow()

    private val _depthText = MutableStateFlow("")
    val depthText: StateFlow<String> = _depthText.asStateFlow()

    private val _photoPath = MutableStateFlow("")
    val photoPath: StateFlow<String> = _photoPath.asStateFlow()

    private val _records = MutableStateFlow<List<SurveyRecord>>(emptyList())
    val records: StateFlow<List<SurveyRecord>> = _records.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _filterDate = MutableStateFlow<LocalDate?>(null)
    val filterDate: StateFlow<LocalDate?> = _filterDate.asStateFlow()

    private val _filterStartDate = MutableStateFlow<LocalDate?>(null)
    val filterStartDate: StateFlow<LocalDate?> = _filterStartDate.asStateFlow()

    private val _filterEndDate = MutableStateFlow<LocalDate?>(null)
    val filterEndDate: StateFlow<LocalDate?> = _filterEndDate.asStateFlow()

    private val _filterLineType = MutableStateFlow("全部")
    val filterLineType: StateFlow<String> = _filterLineType.asStateFlow()

    private val _filterMileageMinText = MutableStateFlow("")
    val filterMileageMinText: StateFlow<String> = _filterMileageMinText.asStateFlow()

    private val _filterMileageMaxText = MutableStateFlow("")
    val filterMileageMaxText: StateFlow<String> = _filterMileageMaxText.asStateFlow()

    init {
        refreshList()
    }

    fun updateLineType(value: String) {
        _lineType.value = value
    }

    fun updateMileageText(value: String) {
        _mileageText.value = value
    }

    fun updateDepthText(value: String) {
        _depthText.value = value
    }

    fun updatePhotoPath(value: String) {
        _photoPath.value = value
    }

    fun updateFilterDate(value: LocalDate?) {
        _filterDate.value = value
        refreshList()
    }

    fun updateFilterStartDate(value: LocalDate?) {
        _filterStartDate.value = value
    }

    fun updateFilterEndDate(value: LocalDate?) {
        _filterEndDate.value = value
    }

    fun updateFilterLineType(value: String) {
        _filterLineType.value = value
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
        _filterLineType.value = "全部"
        _filterMileageMinText.value = ""
        _filterMileageMaxText.value = ""
        refreshList()
    }

    fun saveRecord() {
        val mileage = _mileageText.value.toDoubleOrNull()
        val depth = _depthText.value.toDoubleOrNull()
        val photo = _photoPath.value
        if (mileage == null || depth == null || photo.isBlank()) {
            _error.value = "请填写里程、深度并拍照"
            return
        }
        _error.value = ""
        val dk = toDk(mileage)
        val record = SurveyRecord(
            lineType = _lineType.value,
            mileageRaw = mileage,
            mileageDk = dk,
            depthM = depth,
            photoPath = photo,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(record)
            withContext(Dispatchers.Main) {
                _mileageText.value = ""
                _depthText.value = ""
                _photoPath.value = ""
                refreshList()
            }
        }
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
        val line = _filterLineType.value
        val minMileage = _filterMileageMinText.value.toDoubleOrNull()
        val maxMileage = _filterMileageMaxText.value.toDoubleOrNull()

        val startMillis = startDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val endMillis = endDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()?.minus(1)

        return records.filter { record ->
            if (startMillis != null && record.createdAt < startMillis) return@filter false
            if (endMillis != null && record.createdAt > endMillis) return@filter false
            if (line != "全部" && record.lineType != line) return@filter false
            if (minMileage != null && record.mileageRaw < minMileage) return@filter false
            if (maxMileage != null && record.mileageRaw > maxMileage) return@filter false
            true
        }
    }

    fun dkPreview(): String {
        val mileage = _mileageText.value.toDoubleOrNull() ?: return ""
        return toDk(mileage)
    }
}
