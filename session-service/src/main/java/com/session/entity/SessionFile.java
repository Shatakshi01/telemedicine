package com.session.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "session_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionFile {

    @Id
    private String id;

    @NotNull(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "File name is required")
    private String fileName;

    private String originalFileName;

    @NotNull(message = "File type is required")
    private String fileType;

    private Long fileSize;

    private String filePath;

    private String contentType;

    private FileCategory category;

    private String uploadedBy; // USER or DOCTOR

    private Long uploadedById; // Patient ID or Doctor ID

    private String description;

    @CreatedDate
    private LocalDateTime uploadedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum FileCategory {
        MEDICAL_RECORD,
        PRESCRIPTION,
        LAB_REPORT,
        IMAGE,
        DOCUMENT,
        OTHER
    }
}
