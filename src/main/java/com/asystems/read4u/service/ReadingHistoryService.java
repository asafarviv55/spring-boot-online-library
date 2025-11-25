package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReadingHistory addToHistory(Long memberId, Long bookId, Integer rating, String review, Boolean wouldRecommend) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (readingHistoryRepository.existsByMemberAndBook(member, book)) {
            throw new RuntimeException("Book already in reading history");
        }

        ReadingHistory history = ReadingHistory.builder()
                .member(member)
                .book(book)
                .completedAt(LocalDateTime.now())
                .rating(rating)
                .review(review)
                .wouldRecommend(wouldRecommend != null ? wouldRecommend : false)
                .build();

        return readingHistoryRepository.save(history);
    }

    @Transactional
    public ReadingHistory updateReview(Long historyId, Long memberId, Integer rating, String review, Boolean wouldRecommend) {
        ReadingHistory history = readingHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Reading history not found"));

        if (!history.getMember().getId().equals(memberId)) {
            throw new RuntimeException("You can only update your own reviews");
        }

        if (rating != null) {
            history.setRating(rating);
        }
        if (review != null) {
            history.setReview(review);
        }
        if (wouldRecommend != null) {
            history.setWouldRecommend(wouldRecommend);
        }

        return readingHistoryRepository.save(history);
    }

    public Page<ReadingHistory> getMemberHistory(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return readingHistoryRepository.findByMemberOrderByCompletedAtDesc(member, pageable);
    }

    public List<ReadingHistory> getBookReviews(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return readingHistoryRepository.findByBookOrderByCreatedAtDesc(book);
    }

    public Double getAverageRating(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Double avgRating = readingHistoryRepository.getAverageRatingForBook(book);
        return avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null;
    }

    public Map<String, Object> getMemberReadingStats(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Object[]> categoryStats = readingHistoryRepository.getReadingStatsByCategory(member);
        Page<ReadingHistory> history = readingHistoryRepository.findByMemberOrderByCompletedAtDesc(member, Pageable.unpaged());

        int totalBooks = (int) history.getTotalElements();
        int totalRated = 0;
        int totalRecommended = 0;
        double totalRatingSum = 0;

        for (ReadingHistory h : history) {
            if (h.getRating() != null) {
                totalRated++;
                totalRatingSum += h.getRating();
            }
            if (h.getWouldRecommend()) {
                totalRecommended++;
            }
        }

        Map<String, Long> categoryBreakdown = new HashMap<>();
        for (Object[] stat : categoryStats) {
            categoryBreakdown.put((String) stat[0], (Long) stat[1]);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooksRead", totalBooks);
        stats.put("totalBooksRated", totalRated);
        stats.put("averageRating", totalRated > 0 ? Math.round(totalRatingSum / totalRated * 10.0) / 10.0 : null);
        stats.put("recommendedCount", totalRecommended);
        stats.put("categoryBreakdown", categoryBreakdown);

        return stats;
    }

    public boolean hasReadBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return readingHistoryRepository.existsByMemberAndBook(member, book);
    }
}
