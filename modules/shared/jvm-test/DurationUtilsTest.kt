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
        val expected = 2 * 7 * 24 * 60 * 60 * 1000L + 1 * 24 * 60 * 60 * 1000L + 3 * 60 * 60 * 1000L
        parseDurationText("2w 1d 3h") shouldBe expected
    }

    "parseDurationText should handle no spaces" {
        val expected = 2 * 7 * 24 * 60 * 60 * 1000L + 1 * 24 * 60 * 60 * 1000L + 3 * 60 * 60 * 1000L
        parseDurationText("2w1d3h") shouldBe expected
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

    "formatDurationMillis should return empty for null or zero" {
        formatDurationMillis(null) shouldBe ""
        formatDurationMillis(0) shouldBe ""
        formatDurationMillis(-100) shouldBe ""
    }
})
