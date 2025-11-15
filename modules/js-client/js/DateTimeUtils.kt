import kotlin.js.Date

fun Int.pad2() = if (this < 10) "0$this" else toString()

data class ParsedDateTime(val date: String, val hour: String, val minute: String)

fun parseMillisToDateTime(ms: Long): ParsedDateTime {
    val d = Date(ms.toDouble())
    val year = d.getFullYear()
    val month = (d.getMonth() + 1).pad2()
    val day = d.getDate().pad2()
    val hour = d.getHours().pad2()
    val minute = d.getMinutes().pad2()
    return ParsedDateTime("$year-$month-$day", hour, minute)
}

data class ParsedDateTimeResult(val success: Boolean, val isoDateWithTz: String? = null, val error: String? = null)

fun formatDateWithTimezone(date: Date): String {
    val year = date.getFullYear()
    val month = (date.getMonth() + 1).pad2()
    val day = date.getDate().pad2()
    val hour = date.getHours().pad2()
    val minute = date.getMinutes().pad2()
    val second = date.getSeconds().pad2()

    // Get timezone offset in minutes and convert to +HH:MM or -HH:MM format
    val tzOffsetMinutes = -date.getTimezoneOffset()
    val tzSign = if (tzOffsetMinutes >= 0) "+" else "-"
    val tzHours = (kotlin.math.abs(tzOffsetMinutes) / 60).pad2()
    val tzMinutes = (kotlin.math.abs(tzOffsetMinutes) % 60).pad2()

    return "$year-$month-${day}T$hour:$minute:$second$tzSign$tzHours:$tzMinutes"
}

fun parseUserDateTimeInput(input: String): ParsedDateTimeResult {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
        return ParsedDateTimeResult(success = false, error = "Input is empty")
    }

    return try {
        val lowerInput = trimmed.lowercase()
        val date = when (lowerInput) {
            "now" -> Date()
            "yesterday" -> Date(Date.now() - 24 * 60 * 60 * 1000)
            "tomorrow" -> Date(Date.now() + 24 * 60 * 60 * 1000)
            else -> parseCustomDateFormat(trimmed)
        }

        // Check if the date is valid (Date constructor returns Invalid Date if parsing fails)
        if (date.getTime().isNaN()) {
            ParsedDateTimeResult(success = false, error = "Invalid date format")
        } else {
            val isoString = formatDateWithTimezone(date)
            ParsedDateTimeResult(success = true, isoDateWithTz = isoString)
        }
    } catch (e: Exception) {
        ParsedDateTimeResult(success = false, error = "Failed to parse: ${e.message}")
    }
}

private fun parseCustomDateFormat(input: String): Date {
    // Check for format: YYYY-MM-DD or YYYY-MM-DD HH:MM
    val dateTimePattern = """^(\d{4})-(\d{2})-(\d{2})(?:\s+(\d{1,2}):(\d{2}))?$""".toRegex()
    val match = dateTimePattern.find(input)

    return if (match != null) {
        val (year, month, day, hourStr, minuteStr) = match.destructured
        val now = Date()

        // Use current time if time not specified, otherwise use provided time
        val hour = hourStr.toIntOrNull() ?: now.getHours()
        val minute = minuteStr.toIntOrNull() ?: now.getMinutes()
        val second = if (hourStr.isEmpty()) now.getSeconds() else 0

        // Create date using local timezone
        Date(year.toInt(), month.toInt() - 1, day.toInt(), hour, minute, second, 0)
    } else {
        // Fall back to standard Date parsing
        Date(input)
    }
}

