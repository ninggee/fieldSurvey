package com.example.fieldsurvey.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "survey_records",
    indices = [
        androidx.room.Index(value = ["mileageKm", "mileageDecimal"], unique = true)
    ]
)
data class SurveyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mileageKm: Int,  // 千位及以上部分
    val mileageDecimal: Double,  // 小数部分（0-999.5的倍数）
    val mileageDk: String,  // 完整 DK 格式字符串
    // 是否类字段（可为 null）
    val hasChipping: Boolean?,  // 掉块擦伤
    val hasWear: Boolean?,  // 磨损
    val hasOther: Boolean?,  // 其他
    val hasConcreteNewSegment: Boolean?,  // 砼枕换新段落
    val hasSeamlessStart: Boolean?,  // 无缝线路起始里程
    val hasDerailmentStart: Boolean?,  // 脱线事故起始里程
    val hasSubgradeCompaction: Boolean?,  // 道床严重板结段落
    val hasBallastedBeam: Boolean?,  // 有砟梁/明桥面木枕段落
    // 数值类字段（可为 null）
    val leftRailChippingDepth: Double?,  // 左轨头掉块深度（米）
    val rightRailChippingDepth: Double?,  // 右轨头掉块深度（米）
    val leftRailScratchDepth: Double?,  // 左轨擦伤深度（米）
    val rightRailScratchDepth: Double?,  // 右轨擦伤深度（米）
    val bedThickness: Double?,  // 道床厚度（米）
    // 整数类字段（可为 null）
    val leftRailScratchCount: Int?,  // 左轨擦伤个数
    val rightRailScratchCount: Int?,  // 右轨擦伤个数
    val concreteSleeperDamageCount: Int?,  // 砼枕严重伤损个数
    val woodenSleeperDamageCount: Int?,  // 木枕严重伤损个数
    val concreteClipFailureCount: Int?,  // 砼枕扣件失效套数
    val woodenClipFailureCount: Int?,  // 木枕扣件失效套数
    val fishplateDefectCount: Int?,  // 夹板接头伤损个数
    val boltDefectCount: Int?,  // 螺栓伤损个数
    val antiClimbGoodCount: Int?,  // 防爬器完好个数
    val antiClimbSupportGoodCount: Int?,  // 防爬支撑完好个数
    val gaugeBarGoodCount: Int?,  // 轨距杆完好个数
    val steelRailSevereCount: Int?,  // 钢轨重伤个数（新增）
    // 照片路径列表（用分号分隔多张照片）
    val photoPaths: String,  // 照片路径，多张用分号分隔，可为空
    // 时间戳（隐藏字段，自动记录）
    val createdAt: Long
)

