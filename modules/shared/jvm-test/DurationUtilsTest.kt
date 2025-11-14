package io.holonaut.shared

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

class DurationUtilsSpec : StringSpec({
    "parseDurationText should parse weeks" {
        parseDurationText("1w") shouldBe 7 * 24 * 60 * 60 * 1000L
        parseDurationText("2w") shouldBe 2 * 7 * 24 * 60 * 60 * 1000L
    }

    "parseDurationText should parse days" {
        parseDurationText("1d") shouldBe 24 * 60 * 60 * 1000L
        parseDurationText("3d") shouldBe 3 * 24 * 60 * 60 * 1000L
    }

    "parseDurationText should parse hours" {
        parseDurationText("1h") shouldBe 60 * 60 * 1000L
        parseDurationText("5h") shouldBe 5 * 60 * 60 * 1000L
    }

    "parseDurationText should parse minutes" {
        parseDurationText("1m") shouldBe 60 * 1000L
        parseDurationText("30m") shouldBe 30 * 60 * 1000L
    }

    "parseDurationText should parse seconds" {
        parseDurationText("1s") shouldBe 1000L
        parseDurationText("45s") shouldBe 45 * 1000L
    }

    "parseDurationText should parse combined units" {
        val week = 7 * 24 * 60 * 60 * 1000L
        val day = 24 * 60 * 60 * 1000L
        val hour = 60 * 60 * 1000L

        parseDurationText("2w 1d 3h") shouldBe (2 * week + 1 * day + 3 * hour)
    }

    "parseDurationText should handle no spaces" {
        val week = 7 * 24 * 60 * 60 * 1000L
        val day = 24 * 60 * 60 * 1000L
        val hour = 60 * 60 * 1000L

        parseDurationText("2w1d3h") shouldBe (2 * week + 1 * day + 3 * hour)
    }

    "parseDurationText should return null for invalid input" {
        parseDurationText("").shouldBeNull()
        parseDurationText("   ").shouldBeNull()
        parseDurationText("invalid").shouldBeNull()
        parseDurationText("2x").shouldBeNull()
        parseDurationText(null).shouldBeNull()
    }

    "formatDurationMillis should format weeks and days" {
        val millis = 2 * 7 * 24 * 60 * 60 * 1000L + 3 * 24 * 60 * 60 * 1000L
        formatDurationMillis(millis) shouldBe "2w 3d"
    }

    "formatDurationMillis should format hours and minutes" {
        val millis = 5 * 60 * 60 * 1000L + 30 * 60 * 1000L
        formatDurationMillis(millis) shouldBe "5h 30m"
    }

    "formatDurationMillis should format 61 seconds as 1m 1s" {
        val millis = 61 * 1000L
        formatDurationMillis(millis) shouldBe "1m 1s"
    }

    "formatDurationMillis should return empty for null or zero" {
        formatDurationMillis(null) shouldBe ""
        formatDurationMillis(0) shouldBe ""
        formatDurationMillis(-100) shouldBe ""
    }
})
