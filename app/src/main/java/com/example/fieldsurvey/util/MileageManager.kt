package com.example.fieldsurvey.util

import kotlin.math.floor

data class MileageInfo(
    val km: Int,  // 千位及以上部分
    val decimal: Double,  // 小数部分（0-987.5）
    val dkString: String  // DK 格式完整字符串
)

object MileageManager {
    // 12.5 米为一个单位
    private const val UNIT = 12.5

    /**
     * 从千位部分和小数部分生成完整的 DK 字符串
     */
    fun generateDkString(km: Int, decimal: Double): String {
        val decimalStr = if (decimal % 1 == 0.0) {
            decimal.toInt().toString().padStart(3, '0')
        } else {
            String.format("%.1f", decimal).padStart(5, '0')
        }
        return "DK$km+$decimalStr"
    }

    /**
     * 初始化里程（输入千位部分，返回初始化为 +000 的里程信息）
     */
    fun initMileage(km: Int): MileageInfo {
        return MileageInfo(
            km = km,
            decimal = 0.0,
            dkString = generateDkString(km, 0.0)
        )
    }

    /**
     * 获取下一条记录的里程
     * 按 12.5 米的倍数递增
     */
    fun getNextMileage(km: Int, decimal: Double): MileageInfo {
        var nextDecimal = decimal + UNIT
        var nextKm = km

        // 当小数部分超过 1000 时，千位部分 +1，小数部分重置为 0
        if (nextDecimal >= 1000.0) {
            nextDecimal -= 1000.0
            nextKm += 1
        }

        return MileageInfo(
            km = nextKm,
            decimal = nextDecimal,
            dkString = generateDkString(nextKm, nextDecimal)
        )
    }

    /**
     * 获取上一条记录的里程
     * 按 12.5 米的倍数递减
     */
    fun getPreviousMileage(km: Int, decimal: Double): MileageInfo {
        var prevDecimal = decimal - UNIT
        var prevKm = km

        // 当小数部分小于 0 时，千位部分 -1，小数部分设为 987.5
        if (prevDecimal < 0.0) {
            prevDecimal += 1000.0
            prevKm -= 1
        }

        // 防止负数
        if (prevKm < 0) {
            prevKm = 0
            prevDecimal = 0.0
        }

        return MileageInfo(
            km = prevKm,
            decimal = prevDecimal,
            dkString = generateDkString(prevKm, prevDecimal)
        )
    }

    /**
     * 解析 DK 字符串，提取千位和小数部分
     * 例如：DK838+012.5 -> MileageInfo(838, 12.5, "DK838+012.5")
     */
    fun parseDkString(dkString: String): MileageInfo? {
        val regex = """DK(\d+)\+(\d+(?:\.\d+)?)""".toRegex()
        val match = regex.find(dkString) ?: return null

        val km = match.groupValues[1].toIntOrNull() ?: return null
        val decimal = match.groupValues[2].toDoubleOrNull() ?: return null

        return MileageInfo(km, decimal, dkString)
    }

    /**
     * 获取所有可能的小数部分值（按 12.5 的倍数）
     */
    fun getAllDecimalValues(): List<Double> {
        val values = mutableListOf<Double>()
        var value = 0.0
        while (value < 1000.0) {
            values.add(value)
            value += UNIT
        }
        return values
    }
}

