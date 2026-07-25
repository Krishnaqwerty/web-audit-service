package com.webaudit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class WebAuditApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context initializes cleanly with dev profile
    }
}
