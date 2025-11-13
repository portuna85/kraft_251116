-- V3: Remove SPRING_SESSION tables (migrating to Redis session storage)
-- JDBC 세션 저장소에서 Redis 세션 저장소로 마이그레이션
-- 기존 세션 데이터는 손실되지만, 사용자는 재로그인만 하면 됩니다.

-- Spring Session 관련 테이블 삭제
DROP TABLE IF EXISTS SPRING_SESSION_ATTRIBUTES;
DROP TABLE IF EXISTS SPRING_SESSION;

