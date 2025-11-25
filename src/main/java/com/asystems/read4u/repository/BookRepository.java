package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByIsActiveTrue(Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.isActive = true ORDER BY b.category")
    List<String> findAllCategories();

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.createdAt >= :since ORDER BY b.createdAt DESC")
    List<Book> findNewArrivals(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.category = :category " +
           "ORDER BY (SELECT COUNT(br) FROM BorrowRecord br WHERE br.book = b) DESC")
    List<Book> findPopularBooksByCategory(@Param("category") String category, Pageable pageable);
}
