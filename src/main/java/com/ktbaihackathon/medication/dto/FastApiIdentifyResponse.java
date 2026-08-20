package com.ktbaihackathon.medication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FastApiIdentifyResponse(
        @JsonProperty("vision_failed") boolean visionFailed,
        @JsonProperty("match_result") MatchResult matchResult,
        Detail detail
) {
    // 1. match_result 계층
    public record MatchResult(
            String status,
            List<Candidate> candidates,
            @JsonProperty("query_level_used") String queryLevelUsed
    ) {}

    // 2. candidates 내부 아이템 계층
    public record Candidate(
            @JsonProperty("item_seq") String itemSeq,
            @JsonProperty("item_name") String itemName, // 🚨 우리가 필요한 알약 이름!
            @JsonProperty("entp_name") String entpName,
            String chart,
            @JsonProperty("item_image") String itemImage
    ) {}

    // 3. detail 계ction (이건 카멜케이스로 오네!)
    public record Detail(
            String itemSeq,
            String itemName, // 🚨 detail에서 꺼낼 수도 있음
            String entpName,
            String itemImage,
            String efcyQesitm,
            String useMethodQesitm,
            String atpnWarnQesitm,
            String atpnQesitm,
            String intrcQesitm,
            String seQesitm,
            String depositMethodQesitm
    ) {}
}