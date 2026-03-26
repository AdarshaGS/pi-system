package com.investments.stocks.controller;

import com.investments.stocks.data.Document;
import com.investments.stocks.dto.DocumentDTO;
import com.investments.stocks.service.DocumentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    
    private final DocumentService documentService;
    
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("documentType") Document.DocumentType documentType,
            @RequestParam("category") Document.DocumentCategory category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "relatedEntityId", required = false) Long relatedEntityId,
            @RequestParam(value = "relatedEntityType", required = false) String relatedEntityType,
            @RequestParam(value = "expiryDate", required = false) String expiryDateStr) {
        
        try {
            LocalDateTime expiryDate = expiryDateStr != null ? LocalDateTime.parse(expiryDateStr) : null;
            
            DocumentDTO uploaded = documentService.uploadDocument(
                userId, file, documentType, category, description, tags,
                relatedEntityId, relatedEntityType, expiryDate
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        DocumentDTO document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocument(id);
            byte[] data = documentService.downloadDocument(id);
            
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download document: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentDTO>> getUserDocuments(@PathVariable Long userId) {
        List<DocumentDTO> documents = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<DocumentDTO>> getActiveDocuments(@PathVariable Long userId) {
        List<DocumentDTO> documents = documentService.getActiveDocuments(userId);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByType(
            @PathVariable Long userId,
            @PathVariable Document.DocumentType type) {
        List<DocumentDTO> documents = documentService.getDocumentsByType(userId, type);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByCategory(
            @PathVariable Long userId,
            @PathVariable Document.DocumentCategory category) {
        List<DocumentDTO> documents = documentService.getDocumentsByCategory(userId, category);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/entity/{entityType}/{entityId}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByRelatedEntity(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType) {
        List<DocumentDTO> documents = documentService.getDocumentsByRelatedEntity(userId, entityId, entityType);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/expiring")
    public ResponseEntity<List<DocumentDTO>> getExpiringDocuments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        List<DocumentDTO> documents = documentService.getExpiringDocuments(userId, days);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(
            @PathVariable Long userId,
            @RequestParam String query) {
        List<DocumentDTO> documents = documentService.searchDocuments(userId, query);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/user/{userId}/tag/{tag}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByTag(
            @PathVariable Long userId,
            @PathVariable String tag) {
        List<DocumentDTO> documents = documentService.getDocumentsByTag(userId, tag);
        return ResponseEntity.ok(documents);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentDTO dto) {
        DocumentDTO updated = documentService.updateDocument(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verifyDocument(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String verifiedBy = request.get("verifiedBy");
        documentService.verifyDocument(id, verifiedBy);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveDocument(@PathVariable Long id) {
        documentService.archiveDocument(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
        }
    }
}
