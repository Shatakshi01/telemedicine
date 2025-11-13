package com.session;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.port=27017",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class SessionServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the Spring context loads successfully
    }
}
