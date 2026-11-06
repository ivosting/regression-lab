package dev.regressionlab.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"lab.simulated-latency.enabled=false", "LOG_FILE=build/test-logs/test.json"})
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void ordersEndpointReturnsOrdersAndRequestId() throws Exception {
        mvc.perform(get("/orders").header("X-Request-Id", "test-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "test-123"))
                .andExpect(jsonPath("$.length()").value(20))
                .andExpect(jsonPath("$[0].customerName").isNotEmpty());
    }

    @Test
    void ordersCanBeFilteredByStatus() throws Exception {
        mvc.perform(get("/orders").param("status", "paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].status").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("PAID"))));
    }
}
