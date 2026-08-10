package com.contactai.indian_contact_ai.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentParserService {

    private final Tika tika = new Tika();

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String extractText(MultipartFile file) throws Exception {
        tika.setMaxStringLength(-1); // no size limit
        try (var stream = file.getInputStream()) {
            return tika.parseToString(stream);
        }
    }

    // Saves the raw uploaded file to disk with a collision-proof name.
    // Returns the relative path stored in Contract.filePath.
    public String saveFile(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "contract";
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueName = UUID.randomUUID() + "_" + safeName;

        Path target = dir.resolve(uniqueName);
        Files.copy(file.getInputStream(), target);

        return uploadDir + "/" + uniqueName;
    }

    // PDF/DOCX only — rejects anything else.
    public boolean isAllowedFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return false;
        String lower = name.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".docx"))) return false;

        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
}