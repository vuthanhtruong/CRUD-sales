package com.example.demo.controller;

import com.example.demo.dto.ProductCommentDTO;
import com.example.demo.model.CommentStatus;
import com.example.demo.service.ProductCommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductCommentController {
    private final ProductCommentService commentService;

    public ProductCommentController(ProductCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductCommentDTO>> publicThread(@PathVariable String productId) {
        return ResponseEntity.ok(commentService.publicThread(productId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProductCommentDTO>> mine() {
        return ResponseEntity.ok(commentService.mine());
    }

    @PostMapping
    public ResponseEntity<ProductCommentDTO> create(@RequestBody @Valid ProductCommentDTO dto) {
        return ResponseEntity.ok(commentService.create(dto));
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<ProductCommentDTO> helpful(@PathVariable String id) {
        return ResponseEntity.ok(commentService.markHelpful(id));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ProductCommentDTO>> adminFindAll(@RequestParam(required = false) CommentStatus status) {
        return ResponseEntity.ok(commentService.adminFindAll(status));
    }

    @PutMapping("/admin/{id}/moderate")
    public ResponseEntity<ProductCommentDTO> moderate(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(commentService.moderate(id, CommentStatus.valueOf(body.get("status"))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
