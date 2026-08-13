package com.devlog;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 전체 컨텍스트 로드 테스트는 MySQL·Redis가 필요하므로 통합 테스트 환경에서만 실행
@SpringBootTest
@Disabled("Infrastructure(MySQL, Redis) required — run with Testcontainers in CI")
class DevlogApplicationTests {

	@Test
	void contextLoads() {
	}

}
