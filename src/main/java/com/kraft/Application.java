package com.kraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kraft Application 진입점
 *
 * 참고:
 * - JPA Auditing은 JpaConfig에서 활성화됨
 * - OAuth2 설정은 SecurityConfig에서 관리됨 (oauth 프로파일)
 * - main 메서드: Java 25부터 public 수식어가 암묵적으로 적용됨 (JEP 445)
 */
@SpringBootApplication
public class Application {

    /**
     * 애플리케이션 진입점
     *
     * Java 25 참고:
     * - 'public' 수식어는 암묵적으로 적용됨 (생략 가능)
     * - 하지만 명시성과 팀 협업을 위해 유지할 수도 있음
     */
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
