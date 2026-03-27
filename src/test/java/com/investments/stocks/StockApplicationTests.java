package com.investments.stocks;

import com.pisystem.PiSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PiSystemApplication.class)
@ActiveProfiles("test")
class StockApplicationTests {

	@Test
	void contextLoads() {
	}

}
