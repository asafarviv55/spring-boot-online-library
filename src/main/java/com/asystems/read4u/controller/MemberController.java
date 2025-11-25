package com.asystems.read4u.controller;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final FineService fineService;
    private final ReservationService reservationService;
    private final WishlistService wishlistService;
    private final ReadingHistoryService readingHistoryService;

    @GetMapping
    public ResponseEntity<Page<Member>> getAllMembers(Pageable pageable) {
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PostMapping
    public ResponseEntity<Member> registerMember(@RequestBody Member member) {
        return ResponseEntity.ok(memberService.registerMember(member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
        return ResponseEntity.ok(memberService.updateMember(id, member));
    }

    @PostMapping("/{id}/upgrade")
    public ResponseEntity<Member> upgradeMembership(
            @PathVariable Long id,
            @RequestParam Member.MembershipType type) {
        return ResponseEntity.ok(memberService.upgradeMembership(id, type));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<Member> renewMembership(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int years) {
        return ResponseEntity.ok(memberService.renewMembership(id, years));
    }

    // Fines
    @GetMapping("/{id}/fines")
    public ResponseEntity<List<Fine>> getFines(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.getAllFines(id));
    }

    @GetMapping("/{id}/fines/unpaid")
    public ResponseEntity<List<Fine>> getUnpaidFines(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.getUnpaidFines(id));
    }

    @PostMapping("/{id}/fines/pay-all")
    public ResponseEntity<Void> payAllFines(
            @PathVariable Long id,
            @RequestParam String paymentMethod) {
        fineService.payAllFines(id, paymentMethod);
        return ResponseEntity.ok().build();
    }

    // Reservations
    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<Reservation>> getReservations(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getMemberReservations(id));
    }

    @GetMapping("/{id}/reservations/available")
    public ResponseEntity<List<Reservation>> getAvailableReservations(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getAvailableReservations(id));
    }

    // Wishlist
    @GetMapping("/{id}/wishlist")
    public ResponseEntity<List<WishlistItem>> getWishlist(@PathVariable Long id) {
        return ResponseEntity.ok(wishlistService.getWishlist(id));
    }

    @PostMapping("/{id}/wishlist")
    public ResponseEntity<WishlistItem> addToWishlist(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Integer priority) {
        return ResponseEntity.ok(wishlistService.addToWishlist(id, bookId, notes, priority));
    }

    @DeleteMapping("/{id}/wishlist/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long id,
            @PathVariable Long bookId) {
        wishlistService.removeFromWishlist(id, bookId);
        return ResponseEntity.ok().build();
    }

    // Reading History
    @GetMapping("/{id}/reading-history")
    public ResponseEntity<Page<ReadingHistory>> getReadingHistory(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(readingHistoryService.getMemberHistory(id, pageable));
    }

    @PostMapping("/{id}/reading-history")
    public ResponseEntity<ReadingHistory> addToReadingHistory(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String review,
            @RequestParam(required = false) Boolean wouldRecommend) {
        return ResponseEntity.ok(readingHistoryService.addToHistory(id, bookId, rating, review, wouldRecommend));
    }

    @GetMapping("/{id}/reading-stats")
    public ResponseEntity<Map<String, Object>> getReadingStats(@PathVariable Long id) {
        return ResponseEntity.ok(readingHistoryService.getMemberReadingStats(id));
    }
}
