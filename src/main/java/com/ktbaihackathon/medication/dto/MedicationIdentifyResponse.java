public record MedicationIdentifyResponse(
        boolean ok,
        String itemSeq,
        String itemName,
        String imageUrl,
        Identification identification,
        Recommendation recommendation,
        Features features,
        Official official,
        String document
) {

    public record Identification(
            String confidence,
            double score
    ) {
    }

    public record Recommendation(
            String status,
            double score,
            String confidence,
            String reason,
            String caution
    ) {
    }

    public record Features(
            String frontImprint,
            String backImprint,
            String shape,
            String color,
            boolean scoreLine
    ) {
    }

    public record Official(
            String itemSeq,
            String itemName,
            String efficacy,
            String useMethod,
            String warning,
            String caution,
            String interaction,
            String sideEffect,
            String storage,
            String imageUrl
    ) {
    }
}