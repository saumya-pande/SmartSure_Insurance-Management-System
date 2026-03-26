package com.dev.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:testAuthDb;MODE=MySQL;NON_KEYWORDS=USER",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "JWT_SECRET=superSecretKeyForTesting1234567890123",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "eureka.client.enabled=false"
})
class AuthenticationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
