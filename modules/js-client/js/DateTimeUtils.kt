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

