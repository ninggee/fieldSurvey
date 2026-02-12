package com.example.fieldsurvey.export

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.example.fieldsurvey.data.SurveyRecord
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

object ExcelExporter {
    fun export(
        resolver: ContentResolver,
        records: List<SurveyRecord>,
        fileName: String
    ): Boolean {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("records")

        val header = sheet.createRow(0)
        listOf("line", "mileage", "dk", "depth(m)", "time", "photo").forEachIndexed { index, title ->
            header.createCell(index).setCellValue(title)
        }

        records.forEachIndexed { index, record ->
            val rowIndex = index + 1
            val row = sheet.createRow(rowIndex)
            row.createCell(0).setCellValue(record.lineType)
            row.createCell(1).setCellValue(record.mileageRaw)
            row.createCell(2).setCellValue(record.mileageDk)
            row.createCell(3).setCellValue(record.depthM)
            row.createCell(4).setCellValue(record.createdAt.toDouble())

            val bmp = BitmapFactory.decodeFile(record.photoPath) ?: return@forEachIndexed
            val bos = ByteArrayOutputStream()
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bos)
            val picIdx = workbook.addPicture(bos.toByteArray(), XSSFWorkbook.PICTURE_TYPE_JPEG)
            val drawing = sheet.createDrawingPatriarch()
            val anchor = workbook.creationHelper.createClientAnchor().apply {
                setCol1(5)
                setRow1(rowIndex)
                setCol2(6)
                setRow2(rowIndex + 1)
            }
            drawing.createPicture(anchor, picIdx)
            row.heightInPoints = 80f
            sheet.setColumnWidth(5, 20 * 256)
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
