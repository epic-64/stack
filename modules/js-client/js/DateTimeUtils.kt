import kotlin.js.Date

data class ParsedDateTimeResult(val success: Boolean, val isoDateWithTz: String? = null, val error: String? = null)

fun parseUserDateTimeInput(input: String): ParsedDateTimeResult {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
        return ParsedDateTimeResult(success = false, error = "Input is empty")
    }

    return try {
        val date = Date(trimmed)
        // Check if the date is valid (Date constructor returns Invalid Date if parsing fails)
        if (date.getTime().isNaN()) {
            ParsedDateTimeResult(success = false, error = "Invalid date format")
        } else {
            val isoString = date.toISOString()
            ParsedDateTimeResult(success = true, isoDateWithTz = isoString)
        }
    } catch (e: Exception) {
        ParsedDateTimeResult(success = false, error = "Failed to parse: ${e.message}")
    }
}

