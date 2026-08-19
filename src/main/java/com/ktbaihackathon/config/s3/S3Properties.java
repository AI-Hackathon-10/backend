package com.ktbaihackathon.config.s3;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

    private String region;
    private String bucket;
    private String reportPrefix = "reports";
    private long presignedUrlExpirationSeconds = 600;
    private long maxReportSizeBytes = 10 * 1024 * 1024L;

    public Duration getPresignedUrlExpiration() {
        return Duration.ofSeconds(presignedUrlExpirationSeconds);
    }
}
