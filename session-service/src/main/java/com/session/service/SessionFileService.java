package com.session.service;

import com.session.dto.SessionFileDto;
import com.session.entity.SessionFile;
import com.session.repository.SessionFileRepository;
import com.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionFileService {

    private final SessionFileRepository sessionFileRepository;
    private final SessionRepository sessionRepository;

    @Value("${app.upload.dir:${user.home}/session-uploads}")
    private String uploadDir;

    public SessionFileDto uploadFile(String sessionId, MultipartFile file, SessionFile.FileCategory category,
            String uploadedBy, Long uploadedById, String description) throws IOException {
        log.info("Uploading file for session ID: {}, file: {}", sessionId, file.getOriginalFilename());

        // Validate session exists
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("Session not found with ID: " + sessionId);
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save file metadata to database
        SessionFile sessionFile = SessionFile.builder()
                .sessionId(sessionId)
                .fileName(uniqueFilename)
                .originalFileName(originalFilename)
                .fileType(fileExtension.substring(1)) // Remove the dot
                .fileSize(file.getSize())
                .filePath(filePath.toString())
                .contentType(file.getContentType())
                .category(category)
                .uploadedBy(uploadedBy)
                .uploadedById(uploadedById)
                .description(description)
                .build();

        SessionFile savedFile = sessionFileRepository.save(sessionFile);

        // Update session file counts
        updateSessionFileCounts(sessionId);

        log.info("Successfully uploaded file with ID: {}", savedFile.getId());
        return convertToDto(savedFile);
    }

    public List<SessionFileDto> getSessionFiles(String sessionId) {
        log.info("Retrieving files for session ID: {}", sessionId);
        List<SessionFile> files = sessionFileRepository.findBySessionId(sessionId);
        return files.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SessionFileDto> getSessionFilesByCategory(String sessionId, SessionFile.FileCategory category) {
        log.info("Retrieving files for session ID: {} and category: {}", sessionId, category);
        List<SessionFile> files = sessionFileRepository.findBySessionIdAndCategory(sessionId, category);
        return files.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SessionFileDto> getSessionFilesByUploader(String sessionId, String uploadedBy) {
        log.info("Retrieving files for session ID: {} uploaded by: {}", sessionId, uploadedBy);
        List<SessionFile> files = sessionFileRepository.findBySessionIdAndUploadedBy(sessionId, uploadedBy);
        return files.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public byte[] downloadFile(String fileId) throws IOException {
        log.info("Downloading file with ID: {}", fileId);

        SessionFile sessionFile = sessionFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        Path filePath = Paths.get(sessionFile.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Physical file not found: " + sessionFile.getFilePath());
        }

        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String fileId) throws IOException {
        log.info("Deleting file with ID: {}", fileId);

        SessionFile sessionFile = sessionFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        // Delete physical file
        Path filePath = Paths.get(sessionFile.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete metadata from database
        sessionFileRepository.delete(sessionFile);

        // Update session file counts
        updateSessionFileCounts(sessionFile.getSessionId());

        log.info("Successfully deleted file with ID: {}", fileId);
    }

    private void updateSessionFileCounts(String sessionId) {
        List<SessionFile> allFiles = sessionFileRepository.findBySessionId(sessionId);
        int totalCount = allFiles.size();

        boolean hasPatientFiles = allFiles.stream().anyMatch(f -> "PATIENT".equals(f.getUploadedBy()));
        boolean hasDoctorFiles = allFiles.stream().anyMatch(f -> "DOCTOR".equals(f.getUploadedBy()));

        // Update session with file counts
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setFileCount(totalCount);
            session.setHasPatientFiles(hasPatientFiles);
            session.setHasDoctorFiles(hasDoctorFiles);
            sessionRepository.save(session);
        });
    }

    private SessionFileDto convertToDto(SessionFile sessionFile) {
        return SessionFileDto.builder()
                .id(sessionFile.getId())
                .sessionId(sessionFile.getSessionId())
                .fileName(sessionFile.getFileName())
                .originalFileName(sessionFile.getOriginalFileName())
                .fileType(sessionFile.getFileType())
                .fileSize(sessionFile.getFileSize())
                .contentType(sessionFile.getContentType())
                .category(sessionFile.getCategory())
                .uploadedBy(sessionFile.getUploadedBy())
                .uploadedById(sessionFile.getUploadedById())
                .description(sessionFile.getDescription())
                .uploadedAt(sessionFile.getUploadedAt())
                .build();
    }
}
