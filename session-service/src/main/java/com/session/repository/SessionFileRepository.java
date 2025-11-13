package com.session.repository;

import com.session.entity.SessionFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionFileRepository extends MongoRepository<SessionFile, String> {

    List<SessionFile> findBySessionId(String sessionId);

    List<SessionFile> findBySessionIdAndCategory(String sessionId, SessionFile.FileCategory category);

    List<SessionFile> findBySessionIdAndUploadedBy(String sessionId, String uploadedBy);

    List<SessionFile> findByUploadedById(Long uploadedById);
}
