package io.holonaut.shared

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

class DurationUtilsSpec : StringSpec({
    val SECOND = 1000L
    val MINUTE = 60 * SECOND
    val HOUR = 60 * MINUTE
    val DAY = 24 * HOUR
    val WEEK = 7 * DAY

    "parseDurationText should parse weeks" {
        parseDurationText("1w") shouldBe WEEK
        parseDurationText("2w") shouldBe 2 * WEEK
    }

    "parseDurationText should parse days" {
        parseDurationText("1d") shouldBe DAY
        parseDurationText("3d") shouldBe 3 * DAY
    }

    "parseDurationText should parse hours" {
        parseDurationText("1h") shouldBe HOUR
        parseDurationText("5h") shouldBe 5 * HOUR
    }

    "parseDurationText should parse minutes" {
        parseDurationText("1m") shouldBe MINUTE
        parseDurationText("30m") shouldBe 30 * MINUTE
    }

    "parseDurationText should parse seconds" {
        parseDurationText("1s") shouldBe SECOND
        parseDurationText("45s") shouldBe 45 * SECOND
        parseDurationText("61s") shouldBe 61 * SECOND
    }

    "parseDurationText should parse combined units" {
        parseDurationText("2w 1d 3h") shouldBe (2 * WEEK + 1 * DAY + 3 * HOUR)
        parseDurationText("1d 2h 30m") shouldBe (1 * DAY + 2 * HOUR + 30 * MINUTE)
        parseDurationText("1d 1s") shouldBe (1 * DAY + 1 * SECOND)
    }

    "parseDurationText should handle unit overflow" {
        parseDurationText("1w 8d") shouldBe (2 * WEEK + 1 * DAY)
        parseDurationText("25h") shouldBe (1 * DAY + 1 * HOUR)
    }

    "parseDurationText should handle no spaces" {
        parseDurationText("2w1d3h") shouldBe (2 * WEEK + 1 * DAY + 3 * HOUR)
    }

    "parseDurationText should return null for invalid input" {
        parseDurationText("").shouldBeNull()
        parseDurationText("   ").shouldBeNull()
        parseDurationText("invalid").shouldBeNull()
        parseDurationText("2x").shouldBeNull()
        parseDurationText(null).shouldBeNull()
    }

    "formatDurationMillis should format weeks and days" {
        formatDurationMillis(17 * DAY) shouldBe "2w 3d"
    }

    "formatDurationMillis should format hours and minutes" {
        formatDurationMillis(330 * MINUTE) shouldBe "5h 30m"
    }

    "formatDurationMillis should format 61 seconds as 1m 1s" {
        formatDurationMillis(61 * SECOND) shouldBe "1m 1s"
    }

    "formatDurationMillis should return empty for null or zero" {
        formatDurationMillis(null) shouldBe ""
        formatDurationMillis(0) shouldBe ""
        formatDurationMillis(-100) shouldBe ""
    }
})
