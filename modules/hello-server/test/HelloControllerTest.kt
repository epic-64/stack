package io.holonaut.helloserver

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class HelloControllerSpec : StringSpec({
    val controller = HelloController()
    val mapper = ObjectMapper()

    "hello endpoint returns expected greeting fields" {
        val before = System.currentTimeMillis()
        val greeting = controller.hello()
        val after = System.currentTimeMillis()
        greeting.message shouldBe "Hello, world!"
        // timestamp should fall between before and after within a generous bound
        (after - greeting.timestampMillis) shouldBeLessThan 2_000 // not too far in past
        (greeting.timestampMillis - before) shouldBeLessThan 2_000 // not too far in future relative to capture
    }

    "greeting serializes to JSON with expected fields" {
        val json = mapper.writeValueAsString(controller.hello())
        json shouldContain "\"message\":\"Hello, world!\""
        json shouldContain "timestampMillis"
    }
})
