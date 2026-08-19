package com.ktbaihackathon.report.storage;

import java.time.Instant;

public record PresignedUpload(String uploadUrl, Instant expiresAt) {
}
