package com.asystems.read4u.controller;

import com.asystems.read4u.entity.Book;
import com.asystems.read4u.service.BookService;
import com.asystems.read4u.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.createBook(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooks(q, pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Book>> getBooksByCategory(@PathVariable String category, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByCategory(category, pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<Book>> getAvailableBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailableBooks(pageable));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(bookService.getAllCategories());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<Book>> getNewArrivals(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(bookService.getNewArrivals(days));
    }

    @GetMapping("/popular/{category}")
    public ResponseEntity<List<Book>> getPopularByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getPopularBooksByCategory(category, limit));
    }

    @GetMapping("/recommendations/{memberId}")
    public ResponseEntity<List<Book>> getRecommendations(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForMember(memberId, limit));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Book>> getSimilarBooks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarBooks(id, limit));
    }

    @PatchMapping("/{id}/inventory")
    public ResponseEntity<Void> updateInventory(@PathVariable Long id, @RequestParam int adjustment) {
        bookService.updateInventory(id, adjustment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateBook(@PathVariable Long id) {
        bookService.deactivateBook(id);
        return ResponseEntity.ok().build();
    }
}
