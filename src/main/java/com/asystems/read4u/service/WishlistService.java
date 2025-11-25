package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WishlistItem addToWishlist(Long memberId, Long bookId, String notes, Integer priority) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (wishlistRepository.existsByMemberAndBook(member, book)) {
            throw new RuntimeException("Book is already in your wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .member(member)
                .book(book)
                .notes(notes)
                .priority(priority != null ? priority : 0)
                .notifyWhenAvailable(true)
                .build();

        return wishlistRepository.save(item);
    }

    @Transactional
    public void removeFromWishlist(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        WishlistItem item = wishlistRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new RuntimeException("Book not in wishlist"));

        wishlistRepository.delete(item);
    }

    public List<WishlistItem> getWishlist(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return wishlistRepository.findByMemberOrderByPriorityDescAddedAtDesc(member);
    }

    @Transactional
    public WishlistItem updatePriority(Long memberId, Long bookId, Integer newPriority) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        WishlistItem item = wishlistRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new RuntimeException("Book not in wishlist"));

        item.setPriority(newPriority);
        return wishlistRepository.save(item);
    }

    @Transactional
    public WishlistItem updateNotes(Long memberId, Long bookId, String notes) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        WishlistItem item = wishlistRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new RuntimeException("Book not in wishlist"));

        item.setNotes(notes);
        return wishlistRepository.save(item);
    }

    @Transactional
    public WishlistItem toggleNotification(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        WishlistItem item = wishlistRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new RuntimeException("Book not in wishlist"));

        item.setNotifyWhenAvailable(!item.getNotifyWhenAvailable());
        return wishlistRepository.save(item);
    }

    public boolean isInWishlist(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return wishlistRepository.existsByMemberAndBook(member, book);
    }

    public List<WishlistItem> getMembersToNotify(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return wishlistRepository.findByBookAndNotifyWhenAvailableTrue(book);
    }
}
