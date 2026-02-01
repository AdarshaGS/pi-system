package com.budget.controller;

import com.budget.data.Tag;
import com.budget.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budget/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "APIs for managing transaction tags")
public class TagController {

    private final TagService tagService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user tags", description = "Retrieve all tags for a user")
    public List<Tag> getUserTags(@PathVariable("userId") Long userId) {
        return tagService.getUserTags(userId);
    }

    @GetMapping("/detail/{tagId}")
    @Operation(summary = "Get tag by ID", description = "Retrieve a specific tag by ID")
    public Tag getTagById(@PathVariable("tagId") Long tagId) {
        return tagService.getTagById(tagId);
    }

    @PostMapping
    @Operation(summary = "Create tag", description = "Create a new tag")
    public Tag createTag(@RequestBody Tag tag) {
        return tagService.createTag(tag);
    }

    @PutMapping("/{tagId}")
    @Operation(summary = "Update tag", description = "Update an existing tag")
    public Tag updateTag(@PathVariable("tagId") Long tagId, @RequestBody Tag tag) {
        return tagService.updateTag(tagId, tag);
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete tag", description = "Delete a tag")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable("tagId") Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok(Map.of("message", "Tag deleted successfully"));
    }
}
