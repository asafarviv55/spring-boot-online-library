package com.asystems.read4u.controller;

import com.asystems.read4u.entity.BorrowRecord;
import com.asystems.read4u.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    public ResponseEntity<BorrowRecord> borrowBook(
            @RequestParam Long memberId,
            @RequestParam Long bookId) {
        return ResponseEntity.ok(borrowService.borrowBook(memberId, bookId));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowRecord> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.returnBook(id));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<BorrowRecord> renewBook(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.renewBook(id));
    }

    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<List<BorrowRecord>> getCurrentBorrows(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowService.getCurrentBorrows(memberId));
    }

    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<Page<BorrowRecord>> getBorrowHistory(
            @PathVariable Long memberId,
            Pageable pageable) {
        return ResponseEntity.ok(borrowService.getBorrowHistory(memberId, pageable));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecord>> getOverdueBooks() {
        return ResponseEntity.ok(borrowService.getOverdueBooks());
    }

    @GetMapping("/member/{memberId}/overdue")
    public ResponseEntity<List<BorrowRecord>> getOverdueByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowService.getOverdueByMember(memberId));
    }

    @PostMapping("/{id}/lost")
    public ResponseEntity<Void> markAsLost(
            @PathVariable Long id,
            @RequestParam Double replacementCost) {
        borrowService.markAsLost(id, replacementCost);
        return ResponseEntity.ok().build();
    }
}
