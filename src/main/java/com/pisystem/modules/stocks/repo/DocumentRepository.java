package com.investments.stocks.repo;

import com.investments.stocks.data.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByUserId(Long userId);
    
    List<Document> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<Document> findByUserIdAndDocumentType(Long userId, Document.DocumentType documentType);
    
    List<Document> findByUserIdAndCategory(Long userId, Document.DocumentCategory category);
    
    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.relatedEntityId = :entityId AND d.relatedEntityType = :entityType")
    List<Document> findByRelatedEntity(
        @Param("userId") Long userId,
        @Param("entityId") Long entityId,
        @Param("entityType") String entityType
    );
    
    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.expiryDate BETWEEN :startDate AND :endDate")
    List<Document> findExpiringDocuments(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.fileName LIKE %:fileName%")
    List<Document> searchByFileName(@Param("userId") Long userId, @Param("fileName") String fileName);
    
    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.tags LIKE %:tag%")
    List<Document> findByTag(@Param("userId") Long userId, @Param("tag") String tag);
}
