package com.kraft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis 기반 HTTP 세션 설정
 *
 * 기능:
 * - Redis를 세션 저장소로 사용
 * - 세션 타임아웃: 30분 (1800초)
 * - 자동 세션 만료 및 정리
 *
 * 장점:
 * - 빠른 성능 (메모리 기반)
 * - 수평 확장 가능 (여러 서버 간 세션 공유)
 * - 자동 TTL 관리
 * - 클러스터링 지원
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30분
public class RedisSessionConfig {

    // Redis 연결 설정은 application.yml에서 관리
    // spring.data.redis.host, port, password 등
}

