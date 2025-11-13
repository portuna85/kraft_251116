package com.kraft.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 애플리케이션 시작 시 로그 디렉토리가 존재하지 않으면 생성합니다.
 */
@Component
public class LogDirectoryInitializer {

    @Value("${LOG_DIR:logs}")
    private String logDir;

    @PostConstruct
    public void ensureLogDirectoryExists() {
        try {
            File dir = new File(logDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created && !dir.exists()) {
                    System.err.println("Warning: failed to create log directory: " + dir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // 초기화 실패 시 애플리케이션 시작을 막기 보다는 로그만 남김
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }
}
