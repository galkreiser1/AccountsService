package com.example.accountserviceproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:contextloadstestdb;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccountserviceprojectApplicationTests {

	@Test
	void contextLoads() {
	}

}
