package com.pipes.service;

import com.pipes.dto.StaticPreviewDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StaticPreviewService {

    private static final int MAX_TOTAL_BYTES = 600_000;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("html", "css", "js", "png", "jpg", "jpeg", "gif", "svg", "webp");

    private final Path previewsRoot;

    public StaticPreviewService(@Value("${pipes.preview.root:./pipes-previews}") String previewsRoot) throws IOException {
        this.previewsRoot = Path.of(previewsRoot).toAbsolutePath().normalize();
        Files.createDirectories(this.previewsRoot);
    }

    public StaticPreviewDto.PreviewResponse createPreview(StaticPreviewDto.PreviewRequest request) throws IOException {
        String id = UUID.randomUUID().toString();
        Path previewDir = previewsRoot.resolve(id).normalize();
        Files.createDirectories(previewDir);

        String html = safeText(request.html());
        String css = safeText(request.css());
        String js = safeText(request.js());

        if (html.isBlank()) {
            html = defaultHtml();
        }

        int totalBytes = html.getBytes(StandardCharsets.UTF_8).length
                + css.getBytes(StandardCharsets.UTF_8).length
                + js.getBytes(StandardCharsets.UTF_8).length;

        List<StaticPreviewDto.FileRequest> extraFiles =
                request.files() == null ? List.of() : request.files();

        for (StaticPreviewDto.FileRequest file : extraFiles) {
            totalBytes += safeText(file.content()).getBytes(StandardCharsets.UTF_8).length;
        }

        if (totalBytes > MAX_TOTAL_BYTES) {
            throw new IllegalArgumentException("Static preview is too large. Keep it under 600 KB.");
        }

        Files.writeString(previewDir.resolve("index.html"), html, StandardCharsets.UTF_8);
        Files.writeString(previewDir.resolve("style.css"), css, StandardCharsets.UTF_8);
        Files.writeString(previewDir.resolve("script.js"), js, StandardCharsets.UTF_8);

        for (StaticPreviewDto.FileRequest file : extraFiles) {
            writeExtraFile(previewDir, file);
        }

        return new StaticPreviewDto.PreviewResponse(
                id,
                "/previews/" + id + "/index.html",
                Instant.now()
        );
    }

    private void writeExtraFile(Path previewDir, StaticPreviewDto.FileRequest file) throws IOException {
        String filename = sanitizeFilename(file.filename());
        if (filename.isBlank()) return;

        String ext = extension(filename);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed: " + filename);
        }

        Path target = previewDir.resolve(filename).normalize();

        if (!target.startsWith(previewDir)) {
            throw new IllegalArgumentException("Invalid file path: " + filename);
        }

        Files.createDirectories(target.getParent());
        Files.writeString(target, safeText(file.content()), StandardCharsets.UTF_8);
    }

    private String sanitizeFilename(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "/").replaceAll("^/+", "").trim();
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String defaultHtml() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Pipes Preview</title>
                  <link rel="stylesheet" href="style.css" />
                </head>
                <body>
                  <h1>Pipes Static Preview</h1>
                  <p>Add HTML, CSS, and JavaScript to preview a small static site.</p>
                  <script src="script.js"></script>
                </body>
                </html>
                """;
    }
}