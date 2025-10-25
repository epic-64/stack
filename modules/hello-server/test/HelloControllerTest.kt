package io.holonaut.helloserver

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [HelloController::class])
class HelloControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET hello returns valid JSON`() {
        mockMvc.get("/hello")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.message") { value("Hello, world!") }
                jsonPath("$.timestampMillis") { isNumber() }
            }
    }
}
