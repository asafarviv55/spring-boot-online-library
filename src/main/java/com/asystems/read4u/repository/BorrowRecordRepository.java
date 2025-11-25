package com.asystems.read4u.repository;

import com.asystems.read4u.entity.BorrowRecord;
import com.asystems.read4u.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByMemberAndStatus(Member member, BorrowRecord.BorrowStatus status);

    Page<BorrowRecord> findByMember(Member member, Pageable pageable);

    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.dueDate < :today")
    List<BorrowRecord> findOverdueBooks(@Param("today") LocalDate today);

    @Query("SELECT br FROM BorrowRecord br WHERE br.member = :member AND br.status = 'BORROWED' AND br.dueDate < :today")
    List<BorrowRecord> findOverdueByMember(@Param("member") Member member, @Param("today") LocalDate today);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.member = :member AND br.status = 'BORROWED'")
    int countCurrentBorrowsByMember(@Param("member") Member member);

    @Query("SELECT br FROM BorrowRecord br WHERE br.book.id = :bookId AND br.member.id = :memberId AND br.status = 'BORROWED'")
    java.util.Optional<BorrowRecord> findActiveBorrowByBookAndMember(@Param("bookId") Long bookId, @Param("memberId") Long memberId);

    @Query("SELECT DISTINCT br.book.category, COUNT(br) FROM BorrowRecord br WHERE br.member = :member GROUP BY br.book.category ORDER BY COUNT(br) DESC")
    List<Object[]> findFavoriteCategoriesByMember(@Param("member") Member member);
}
