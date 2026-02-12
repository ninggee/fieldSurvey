package com.example.fieldsurvey.export

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.fieldsurvey.data.SurveyRecord
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExcelExporter {
    // 照片位置名称（与 SurveyViewModel 对应）
    private val PHOTO_POSITIONS = listOf(
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun export(
        resolver: ContentResolver,
        records: List<SurveyRecord>,
        fileName: String
    ): Boolean {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("records")

        // 设置表头
        val header = sheet.createRow(0)
        val headers = listOf(
            "里程DK", "钢轨重伤个数", "掉块擦伤", "磨损", "其他",
            "左轨头掉块深度", "右轨头掉块深度",
            "左轨擦伤深度", "左轨擦伤个数",
            "右轨擦伤深度", "右轨擦伤个数",
            "砼枕严重伤损个数", "木枕严重伤损个数",
            "砼枕换新段落", "砼枕扣件失效套数", "木枕扣件失效套数",
            "道床厚度", "夹板接头伤损个数", "螺栓伤损个数",
            "防爬器完好个数", "防爬支撑完好个数", "轨距杆完好个数",
            "无缝线路起始里程", "脱线事故起始里程", "道床严重板结段落",
            "有砟梁/明桥面木枕段落", "时间"
        ) + PHOTO_POSITIONS

        headers.forEachIndexed { index, title ->
            header.createCell(index).setCellValue(title)
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

        records.forEachIndexed { index, record ->
            val rowIndex = index + 1
            val row = sheet.createRow(rowIndex)

            var cellIndex = 0

            // 基本信息
            row.createCell(cellIndex++).setCellValue(record.mileageDk)

            // 钢轨重伤个数 - 0 时显示空白
            row.createCell(cellIndex++).setCellValue(if (record.steelRailSevereCount != null && record.steelRailSevereCount != 0) record.steelRailSevereCount.toString() else "")

            // 布尔值 - 显示为对勾或空
            row.createCell(cellIndex++).setCellValue(if (record.hasChipping == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.hasWear == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.hasOther == true) "✓" else "")

            // 深度值 - 0 时显示空白
            row.createCell(cellIndex++).setCellValue(if (record.leftRailChippingDepth != null && record.leftRailChippingDepth != 0.0) record.leftRailChippingDepth.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.rightRailChippingDepth != null && record.rightRailChippingDepth != 0.0) record.rightRailChippingDepth.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.leftRailScratchDepth != null && record.leftRailScratchDepth != 0.0) record.leftRailScratchDepth.toString() else "")

            // 计数值 - 0 时显示空白
            row.createCell(cellIndex++).setCellValue(if (record.leftRailScratchCount != null && record.leftRailScratchCount != 0) record.leftRailScratchCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.rightRailScratchDepth != null && record.rightRailScratchDepth != 0.0) record.rightRailScratchDepth.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.rightRailScratchCount != null && record.rightRailScratchCount != 0) record.rightRailScratchCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.concreteSleeperDamageCount != null && record.concreteSleeperDamageCount != 0) record.concreteSleeperDamageCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.woodenSleeperDamageCount != null && record.woodenSleeperDamageCount != 0) record.woodenSleeperDamageCount.toString() else "")

            // 更多布尔值
            row.createCell(cellIndex++).setCellValue(if (record.hasConcreteNewSegment == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.concreteClipFailureCount != null && record.concreteClipFailureCount != 0) record.concreteClipFailureCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.woodenClipFailureCount != null && record.woodenClipFailureCount != 0) record.woodenClipFailureCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.bedThickness != null && record.bedThickness != 0.0) record.bedThickness.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.fishplateDefectCount != null && record.fishplateDefectCount != 0) record.fishplateDefectCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.boltDefectCount != null && record.boltDefectCount != 0) record.boltDefectCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.antiClimbGoodCount != null && record.antiClimbGoodCount != 0) record.antiClimbGoodCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.antiClimbSupportGoodCount != null && record.antiClimbSupportGoodCount != 0) record.antiClimbSupportGoodCount.toString() else "")
            row.createCell(cellIndex++).setCellValue(if (record.gaugeBarGoodCount != null && record.gaugeBarGoodCount != 0) record.gaugeBarGoodCount.toString() else "")


            // 更多布尔值
            row.createCell(cellIndex++).setCellValue(if (record.hasSeamlessStart == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.hasDerailmentStart == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.hasSubgradeCompaction == true) "✓" else "")
            row.createCell(cellIndex++).setCellValue(if (record.hasBallastedBeam == true) "✓" else "")

            // 时间
            val timeStr = formatter.format(Instant.ofEpochMilli(record.createdAt))
            row.createCell(cellIndex++).setCellValue(timeStr)

            // 处理照片 - 按位置分别导出到 9 列
            val photoPathsList = record.photoPaths.split(";")
            for (position in 0 until 9) {
                val photoPath = if (position < photoPathsList.size && photoPathsList[position].isNotBlank()) {
                    photoPathsList[position]
                } else {
                    null
                }

                if (photoPath != null && photoPath.isNotBlank()) {
                    try {
                        val bmp = BitmapFactory.decodeFile(photoPath)
                        if (bmp != null) {
                            val bos = ByteArrayOutputStream()
                            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bos)
                            val picIdx = workbook.addPicture(bos.toByteArray(), XSSFWorkbook.PICTURE_TYPE_JPEG)
                            val drawing = sheet.createDrawingPatriarch()
                            val photoCol = cellIndex
                            val anchor = workbook.creationHelper.createClientAnchor().apply {
                                setCol1(photoCol)
                                setRow1(rowIndex)
                                setCol2(photoCol + 1)
                                setRow2(rowIndex + 1)
                            }
                            drawing.createPicture(anchor, picIdx)
                            sheet.setColumnWidth(photoCol, 20 * 256)
                            row.heightInPoints = 80f
                        }
                    } catch (e: Exception) {
                        // 照片处理失败，留空
                    }
                }
                cellIndex++
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { output ->
            workbook.write(output)
        } ?: return false
        workbook.close()
        return true
    }
}
