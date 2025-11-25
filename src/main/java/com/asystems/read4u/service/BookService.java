package com.asystems.read4u.service;

import com.asystems.read4u.entity.Book;
import com.asystems.read4u.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findByIsActiveTrue(pageable);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
    }

    @Transactional
    public Book createBook(Book book) {
        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new RuntimeException("Book with ISBN already exists");
        }
        book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book bookDetails) {
        Book book = getBookById(id);
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setDescription(bookDetails.getDescription());
        book.setCategory(bookDetails.getCategory());
        book.setPublisher(bookDetails.getPublisher());
        book.setPageCount(bookDetails.getPageCount());
        book.setCoverImageUrl(bookDetails.getCoverImageUrl());
        book.setShelfLocation(bookDetails.getShelfLocation());
        return bookRepository.save(book);
    }

    @Transactional
    public void updateInventory(Long bookId, int adjustment) {
        Book book = getBookById(bookId);
        int newTotal = book.getTotalCopies() + adjustment;
        int newAvailable = book.getAvailableCopies() + adjustment;

        if (newTotal < 0 || newAvailable < 0) {
            throw new RuntimeException("Cannot reduce copies below zero");
        }

        book.setTotalCopies(newTotal);
        book.setAvailableCopies(newAvailable);
        bookRepository.save(book);
    }

    public Page<Book> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable);
    }

    public Page<Book> getBooksByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable);
    }

    public Page<Book> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable);
    }

    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }

    public List<Book> getNewArrivals(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return bookRepository.findNewArrivals(since);
    }

    public List<Book> getPopularBooksByCategory(String category, int limit) {
        return bookRepository.findPopularBooksByCategory(category, PageRequest.of(0, limit));
    }

    @Transactional
    public void deactivateBook(Long id) {
        Book book = getBookById(id);
        book.setIsActive(false);
        bookRepository.save(book);
    }
}
