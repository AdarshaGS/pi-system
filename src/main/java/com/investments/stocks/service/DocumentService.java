package com.investments.stocks.service;

import com.investments.stocks.data.Document;
import com.investments.stocks.dto.DocumentDTO;
import com.investments.stocks.exception.ResourceNotFoundException;
import com.investments.stocks.repo.DocumentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    
    @Value("${document.upload.dir:./uploads/documents}")
    private String uploadDir;
    
    @Value("${document.max.size:10485760}") // 10MB default
    private Long maxFileSize;
    
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }
    
    @Transactional
    public DocumentDTO uploadDocument(
            Long userId,
            MultipartFile file,
            Document.DocumentType documentType,
            Document.DocumentCategory category,
            String description,
            String tags,
            Long relatedEntityId,
            String relatedEntityType,
            LocalDateTime expiryDate) throws IOException {
        
        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Create user-specific directory
        Path userDir = Paths.get(uploadDir, userId.toString());
        Files.createDirectories(userDir);
        
        // Save file
        Path filePath = userDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Calculate checksum
        String checksum = calculateChecksum(file.getBytes());
        
        // Create document entity
        Document document = new Document();
        document.setUserId(userId);
        document.setFileName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setDocumentType(documentType);
        document.setCategory(category);
        document.setDescription(description);
        document.setTags(tags);
        document.setRelatedEntityId(relatedEntityId);
        document.setRelatedEntityType(relatedEntityType);
        document.setExpiryDate(expiryDate);
        document.setChecksum(checksum);
        
        document = documentRepository.save(document);
        return convertToDto(document);
    }
    
    public DocumentDTO getDocument(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return convertToDto(document);
    }
    
    public List<DocumentDTO> getUserDocuments(Long userId) {
        return documentRepository.findByUserId(userId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getActiveDocuments(Long userId) {
        return documentRepository.findByUserIdAndIsActive(userId, true).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getDocumentsByType(Long userId, Document.DocumentType type) {
        return documentRepository.findByUserIdAndDocumentType(userId, type).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getDocumentsByCategory(Long userId, Document.DocumentCategory category) {
        return documentRepository.findByUserIdAndCategory(userId, category).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getDocumentsByRelatedEntity(Long userId, Long entityId, String entityType) {
        return documentRepository.findByRelatedEntity(userId, entityId, entityType).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getExpiringDocuments(Long userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        return documentRepository.findExpiringDocuments(userId, now, futureDate).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> searchDocuments(Long userId, String searchTerm) {
        return documentRepository.searchByFileName(userId, searchTerm).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentDTO> getDocumentsByTag(Long userId, String tag) {
        return documentRepository.findByTag(userId, tag).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DocumentDTO updateDocument(Long id, DocumentDTO dto) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        if (dto.getDescription() != null) document.setDescription(dto.getDescription());
        if (dto.getTags() != null) document.setTags(dto.getTags());
        if (dto.getCategory() != null) document.setCategory(dto.getCategory());
        if (dto.getDocumentType() != null) document.setDocumentType(dto.getDocumentType());
        if (dto.getExpiryDate() != null) document.setExpiryDate(dto.getExpiryDate());
        if (dto.getRelatedEntityId() != null) document.setRelatedEntityId(dto.getRelatedEntityId());
        if (dto.getRelatedEntityType() != null) document.setRelatedEntityType(dto.getRelatedEntityType());
        
        document = documentRepository.save(document);
        return convertToDto(document);
    }
    
    @Transactional
    public void verifyDocument(Long id, String verifiedBy) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        document.setIsVerified(true);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerifiedBy(verifiedBy);
        documentRepository.save(document);
    }
    
    @Transactional
    public void archiveDocument(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        document.setIsActive(false);
        documentRepository.save(document);
    }
    
    @Transactional
    public void deleteDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        // Delete physical file
        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);
        
        // Delete database record
        documentRepository.delete(document);
    }
    
    public byte[] downloadDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        Path filePath = Paths.get(document.getFilePath());
        return Files.readAllBytes(filePath);
    }
    
    private DocumentDTO convertToDto(Document document) {
        DocumentDTO dto = new DocumentDTO();
        BeanUtils.copyProperties(document, dto);
        
        // Format file size
        dto.setFileSizeFormatted(formatFileSize(document.getFileSize()));
        
        // Calculate days until expiry
        if (document.getExpiryDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), document.getExpiryDate());
            dto.setDaysUntilExpiry(days);
            dto.setIsExpired(days < 0);
        } else {
            dto.setIsExpired(false);
        }
        
        // Generate download URL
        dto.setDownloadUrl("/api/documents/" + document.getId() + "/download");
        
        return dto;
    }
    
    private String formatFileSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
