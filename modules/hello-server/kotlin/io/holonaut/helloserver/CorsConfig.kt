package io.holonaut.helloserver

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            // Allow all origins for all endpoints
            .allowedOriginPatterns("*")
            // Allow all standard HTTP methods
            .allowedMethods("*")
            // Allow all headers
            .allowedHeaders("*")
            // Do not include credentials with wildcard origins
            .allowCredentials(false)
            // Cache preflight response for 1 hour
            .maxAge(3600)
    }
}
