package io.holonaut.shared

/**
 * Parses a human-readable duration string like "2w 1d 3h" into milliseconds.
 * Supported units: w (weeks), d (days), h (hours), m (minutes), s (seconds)
 * Returns null if the input is invalid.
 */
fun parseDurationText(text: String?): Long? {
    if (text.isNullOrBlank()) return null
    
    val regex = Regex("""(\d+)\s*([wdhms])""")
    val matches = regex.findAll(text.trim().lowercase())
    
    if (!matches.any()) return null
    
    var totalMillis = 0L
    
    for (match in matches) {
        val value = match.groupValues[1].toLongOrNull() ?: return null
        val unit = match.groupValues[2]
        
        val millis = when (unit) {
            "w" -> value * 7 * 24 * 60 * 60 * 1000
            "d" -> value * 24 * 60 * 60 * 1000
            "h" -> value * 60 * 60 * 1000
            "m" -> value * 60 * 1000
            "s" -> value * 1000
            else -> return null
        }
        
        totalMillis += millis
    }
    
    return if (totalMillis > 0) totalMillis else null
}

/**
 * Formats milliseconds into a human-readable duration string.
 * Uses the largest units possible (weeks, days, hours, minutes).
 */
fun formatDurationMillis(millis: Long?): String {
    if (millis == null || millis <= 0) return ""
    
    var remaining = millis
    val parts = mutableListOf<String>()
    
    val weeks = remaining / (7 * 24 * 60 * 60 * 1000)
    if (weeks > 0) {
        parts.add("${weeks}w")
        remaining %= (7 * 24 * 60 * 60 * 1000)
    }
    
    val days = remaining / (24 * 60 * 60 * 1000)
    if (days > 0) {
        parts.add("${days}d")
        remaining %= (24 * 60 * 60 * 1000)
    }
    
    val hours = remaining / (60 * 60 * 1000)
    if (hours > 0) {
        parts.add("${hours}h")
        remaining %= (60 * 60 * 1000)
    }
    
    val minutes = remaining / (60 * 1000)
    if (minutes > 0) {
        parts.add("${minutes}m")
    }
    
    return parts.joinToString(" ")
}

