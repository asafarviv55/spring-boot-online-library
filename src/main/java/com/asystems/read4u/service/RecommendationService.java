package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReadingHistoryRepository readingHistoryRepository;

    public List<Book> getRecommendationsForMember(Long memberId, int limit) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Get member's favorite categories based on borrowing history
        List<String> favoriteCategories = getFavoriteCategories(member);

        if (favoriteCategories.isEmpty()) {
            // New member - return popular books
            return getPopularBooks(limit);
        }

        Set<Long> borrowedBookIds = getBorrowedBookIds(member);
        List<Book> recommendations = new ArrayList<>();

        // Get recommendations from each favorite category
        for (String category : favoriteCategories) {
            List<Book> categoryBooks = bookRepository.findPopularBooksByCategory(category, PageRequest.of(0, limit));
            for (Book book : categoryBooks) {
                if (!borrowedBookIds.contains(book.getId()) && book.getIsActive()) {
                    recommendations.add(book);
                    if (recommendations.size() >= limit) {
                        return recommendations;
                    }
                }
            }
        }

        // If not enough recommendations, fill with popular books
        if (recommendations.size() < limit) {
            List<Book> popular = getPopularBooks(limit - recommendations.size());
            for (Book book : popular) {
                if (!borrowedBookIds.contains(book.getId()) && !recommendations.contains(book)) {
                    recommendations.add(book);
                }
            }
        }

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Book> getSimilarBooks(Long bookId, int limit) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Get books in the same category by the same author
        List<Book> similarBooks = bookRepository.findPopularBooksByCategory(book.getCategory(),
                PageRequest.of(0, limit * 2));

        return similarBooks.stream()
                .filter(b -> !b.getId().equals(bookId))
                .filter(Book::getIsActive)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Book> getBasedOnRating(Long memberId, int minRating, int limit) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Get highly rated books from member's reading history
        List<Object[]> categoryStats = readingHistoryRepository.getReadingStatsByCategory(member);

        if (categoryStats.isEmpty()) {
            return getPopularBooks(limit);
        }

        // Get the top category
        String topCategory = (String) categoryStats.get(0)[0];

        // Get books from that category with high average ratings
        return bookRepository.findPopularBooksByCategory(topCategory, PageRequest.of(0, limit));
    }

    private List<String> getFavoriteCategories(Member member) {
        List<Object[]> categoryStats = borrowRecordRepository.findFavoriteCategoriesByMember(member);
        return categoryStats.stream()
                .limit(3)
                .map(obj -> (String) obj[0])
                .collect(Collectors.toList());
    }

    private Set<Long> getBorrowedBookIds(Member member) {
        return borrowRecordRepository.findByMemberAndStatus(member, BorrowRecord.BorrowStatus.BORROWED)
                .stream()
                .map(br -> br.getBook().getId())
                .collect(Collectors.toSet());
    }

    private List<Book> getPopularBooks(int limit) {
        // Get books with most borrows
        return bookRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent();
    }

    public List<Book> getNewArrivals(int days, int limit) {
        return bookRepository.findNewArrivals(java.time.LocalDateTime.now().minusDays(days))
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Book> getTrendingBooks(int limit) {
        // Get books that have been borrowed most frequently in the last 30 days
        return bookRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent();
    }
}
