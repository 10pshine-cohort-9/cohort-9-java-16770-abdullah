package com.abdullahadil.contactmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that the whole application context wires up. Runs on the dev
 * profile (H2) so it doesn't need Docker - see TestcontainersConfiguration
 * for running against a real SQL Server container instead.
 */
@SpringBootTest
@ActiveProfiles("dev")
class ContactManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
