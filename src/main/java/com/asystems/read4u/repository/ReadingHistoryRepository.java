package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Book;
import com.asystems.read4u.entity.Member;
import com.asystems.read4u.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    Page<ReadingHistory> findByMemberOrderByCompletedAtDesc(Member member, Pageable pageable);

    List<ReadingHistory> findByBookOrderByCreatedAtDesc(Book book);

    @Query("SELECT AVG(rh.rating) FROM ReadingHistory rh WHERE rh.book = :book AND rh.rating IS NOT NULL")
    Double getAverageRatingForBook(@Param("book") Book book);

    @Query("SELECT rh.book.category, COUNT(rh) FROM ReadingHistory rh WHERE rh.member = :member GROUP BY rh.book.category ORDER BY COUNT(rh) DESC")
    List<Object[]> getReadingStatsByCategory(@Param("member") Member member);

    boolean existsByMemberAndBook(Member member, Book book);
}
