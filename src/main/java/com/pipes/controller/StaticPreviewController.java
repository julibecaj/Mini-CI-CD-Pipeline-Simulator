package com.pipes.controller;

import com.pipes.dto.StaticPreviewDto;
import com.pipes.service.StaticPreviewService;
import com.pipes.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/static-preview")
public class StaticPreviewController {

    private final StaticPreviewService staticPreviewService;

    public StaticPreviewController(StaticPreviewService staticPreviewService) {
        this.staticPreviewService = staticPreviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaticPreviewDto.PreviewResponse>> create(
            @Valid @RequestBody StaticPreviewDto.PreviewRequest request
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Static preview created.", staticPreviewService.createPreview(request)));
    }
}