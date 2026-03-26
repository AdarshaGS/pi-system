package com.budget.service;

import com.budget.data.Tag;
import com.budget.repo.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing tags
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Get all tags for a user
     */
    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    /**
     * Create a new tag
     */
    @Transactional
    public Tag createTag(Tag tag) {
        // Check if tag with same name already exists for this user
        if (tagRepository.existsByUserIdAndName(tag.getUserId(), tag.getName())) {
            throw new RuntimeException("Tag with name '" + tag.getName() + "' already exists");
        }
        return tagRepository.save(tag);
    }

    /**
     * Update a tag
     */
    @Transactional
    public Tag updateTag(Long tagId, Tag updatedTag) {
        Tag existing = tagRepository.findById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
        
        // Check if new name conflicts with another tag
        if (!existing.getName().equals(updatedTag.getName())) {
            if (tagRepository.existsByUserIdAndName(existing.getUserId(), updatedTag.getName())) {
                throw new RuntimeException("Tag with name '" + updatedTag.getName() + "' already exists");
            }
        }
        
        existing.setName(updatedTag.getName());
        existing.setColor(updatedTag.getColor());
        return tagRepository.save(existing);
    }

    /**
     * Delete a tag
     */
    @Transactional
    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }

    /**
     * Get tag by ID
     */
    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
    }
}
