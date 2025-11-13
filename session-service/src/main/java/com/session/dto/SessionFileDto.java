package com.session.dto;

import com.session.entity.SessionFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFileDto {

    private String id;
    private String sessionId;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String contentType;
    private SessionFile.FileCategory category;
    private String uploadedBy;
    private Long uploadedById;
    private String description;
    private LocalDateTime uploadedAt;
}
