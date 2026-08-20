package com.ktbaihackathon.medication.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ImageUrlServiceTest {

    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final S3ImageUrlService s3ImageUrlService = new S3ImageUrlService(
            s3Presigner,
            "pillid-images-team10"
    );

    @Test
    void issuesFiveMinuteDownloadUrlForImageObjectKey() throws Exception {
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url())
                .thenReturn(URI.create("https://s3.example/front-download").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        String imageUrl = s3ImageUrlService.issueDownloadUrl("uploads/front.jpg");

        assertThat(imageUrl).isEqualTo("https://s3.example/front-download");

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().signatureDuration())
                .isEqualTo(Duration.ofMinutes(5));
        assertThat(requestCaptor.getValue().getObjectRequest().bucket())
                .isEqualTo("pillid-images-team10");
        assertThat(requestCaptor.getValue().getObjectRequest().key())
                .isEqualTo("uploads/front.jpg");
    }

    @Test
    void returnsNullWithoutCallingS3ForMissingObjectKey() {
        assertThat(s3ImageUrlService.issueDownloadUrl(" ")).isNull();

        verify(s3Presigner, never())
                .presignGetObject(any(GetObjectPresignRequest.class));
    }
}
