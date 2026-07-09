package com.pipes.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class StaticPreviewDto {

    public record FileRequest(
            @Size(max = 120) String filename,
            @Size(max = 200_000) String content
    ) {}

    public record PreviewRequest(
            @Size(max = 200_000) String html,
            @Size(max = 200_000) String css,
            @Size(max = 200_000) String js,
            List<FileRequest> files
    ) {}

    public record PreviewResponse(
            String id,
            String previewUrl,
            Instant createdAt
    ) {}
}