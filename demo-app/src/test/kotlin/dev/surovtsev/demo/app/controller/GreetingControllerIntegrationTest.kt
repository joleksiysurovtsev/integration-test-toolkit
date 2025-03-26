package dev.surovtsev.demo.app.controller

import dev.surovtsev.integration.test.toolkit.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class GreetingControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `should return default greeting when no name is provided`() {
        mockMvc.perform(get("/greeting"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Hello, World!"))
    }

    @Test
    fun `should return personalized greeting when name is provided`() {
        val name = "John"
        mockMvc.perform(get("/greeting").param("name", name))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Hello, $name!"))
    }
}
