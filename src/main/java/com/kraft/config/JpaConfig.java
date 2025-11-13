package com.kraft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 *
 * @EnableJpaAuditing: JPA Auditing 기능 활성화
 * - BaseEntity의 @CreatedDate, @LastModifiedDate 자동 설정
 * - Entity 생성/수정 시각 자동 관리
 *
 * 설계 결정:
 * 1. Application 클래스가 아닌 별도 Config 클래스에서 관리
 *    - 관심사 분리 (Application은 진입점 역할만)
 *    - 테스트 격리 용이 (슬라이스 테스트에서 선택적 로드)
 *    - 설정 집중화 (JPA 관련 설정을 한 곳에서 관리)
 *
 * 2. 향후 확장 가능성:
 *    - AuditorAware 빈 추가로 생성자/수정자 자동 기록 가능
 *    - @CreatedBy, @LastModifiedBy 지원
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    // TODO: 향후 필요시 AuditorAware 빈 추가하여
    // BaseEntity에 @CreatedBy, @LastModifiedBy 필드 자동 설정 가능

    /*
    예시:
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
    */
}
