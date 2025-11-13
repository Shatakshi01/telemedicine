package com.session.controller;

import com.session.dto.SessionFileDto;
import com.session.entity.SessionFile;
import com.session.service.SessionFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Files", description = "APIs for managing files in telemedicine sessions")
public class SessionFileController {

    private final SessionFileService sessionFileService;

    @PostMapping("/upload")
    @Operation(summary = "Upload file to session", description = "Upload a file (image, PDF, etc.) to a telemedicine session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or session data"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionFileDto> uploadFile(
            @Parameter(description = "Session ID") @PathVariable String sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "DOCUMENT") SessionFile.FileCategory category,
            @RequestParam("uploadedBy") String uploadedBy,
            @RequestParam("uploadedById") Long uploadedById,
            @RequestParam(value = "description", required = false) String description) {

        try {
            log.info("REST API: Uploading file to session ID: {}, file: {}", sessionId, file.getOriginalFilename());
            SessionFileDto response = sessionFileService.uploadFile(sessionId, file, category, uploadedBy, uploadedById,
                    description);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get session files", description = "Retrieve all files for a specific session")
    @ApiResponse(responseCode = "200", description = "Files retrieved successfully")
    public ResponseEntity<List<SessionFileDto>> getSessionFiles(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {

        log.info("REST API: Fetching files for session ID: {}", sessionId);
        List<SessionFileDto> response = sessionFileService.getSessionFiles(sessionId);
        return ResponseEntity.ok(response);
    }
}
