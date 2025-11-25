package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final FineService fineService;

    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final int RENEWAL_DAYS = 7;

    @Transactional
    public BorrowRecord borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Check if member has outstanding fines
        if (member.getOutstandingFines() > 0) {
            throw new RuntimeException("Please pay outstanding fines before borrowing");
        }

        // Check if member has reached max borrow limit
        int currentBorrows = borrowRecordRepository.countCurrentBorrowsByMember(member);
        if (currentBorrows >= member.getMaxBooksAllowed()) {
            throw new RuntimeException("Maximum borrow limit reached");
        }

        // Check book availability
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available. Please reserve the book.");
        }

        // Check for active reservations
        List<Reservation> pendingReservations = reservationRepository.findPendingReservationsForBook(book);
        if (!pendingReservations.isEmpty()) {
            Reservation firstInQueue = pendingReservations.get(0);
            if (!firstInQueue.getMember().getId().equals(memberId)) {
                throw new RuntimeException("Book is reserved by another member");
            }
            // Fulfill the reservation
            firstInQueue.setStatus(Reservation.ReservationStatus.FULFILLED);
            reservationRepository.save(firstInQueue);
        }

        // Create borrow record
        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS))
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .build();

        // Update book available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Update member borrowed count
        member.setCurrentBorrowedCount(member.getCurrentBorrowedCount() + 1);
        memberRepository.save(member);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (record.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
            throw new RuntimeException("Book is not currently borrowed");
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);

        // Calculate fine if overdue
        if (record.isOverdue()) {
            Double fineAmount = fineService.calculateOverdueFine(record);
            record.setFineAmount(fineAmount);
            record.setFinePaid(false);

            // Create fine record
            fineService.createFine(record.getMember(), record, fineAmount, Fine.FineType.OVERDUE,
                    "Overdue by " + record.getDaysOverdue() + " days");
        }

        // Update book available copies
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // Update member borrowed count
        Member member = record.getMember();
        member.setCurrentBorrowedCount(Math.max(0, member.getCurrentBorrowedCount() - 1));
        memberRepository.save(member);

        // Check for pending reservations and notify
        List<Reservation> pendingReservations = reservationRepository.findPendingReservationsForBook(book);
        if (!pendingReservations.isEmpty()) {
            Reservation nextReservation = pendingReservations.get(0);
            nextReservation.setStatus(Reservation.ReservationStatus.AVAILABLE);
            nextReservation.setNotifiedAt(java.time.LocalDateTime.now());
            nextReservation.setExpiryDate(java.time.LocalDateTime.now().plusDays(3));
            reservationRepository.save(nextReservation);
        }

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord renewBook(Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (record.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
            throw new RuntimeException("Book is not currently borrowed");
        }

        if (record.getRenewalCount() >= record.getMaxRenewals()) {
            throw new RuntimeException("Maximum renewals reached");
        }

        // Check if there are reservations for this book
        int reservationCount = reservationRepository.countPendingReservationsForBook(record.getBook());
        if (reservationCount > 0) {
            throw new RuntimeException("Cannot renew - book has pending reservations");
        }

        // Check for outstanding fines
        if (record.isOverdue()) {
            throw new RuntimeException("Cannot renew - book is overdue. Please return and pay fines.");
        }

        record.setDueDate(record.getDueDate().plusDays(RENEWAL_DAYS));
        record.setRenewalCount(record.getRenewalCount() + 1);

        return borrowRecordRepository.save(record);
    }

    public List<BorrowRecord> getCurrentBorrows(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return borrowRecordRepository.findByMemberAndStatus(member, BorrowRecord.BorrowStatus.BORROWED);
    }

    public Page<BorrowRecord> getBorrowHistory(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return borrowRecordRepository.findByMember(member, pageable);
    }

    public List<BorrowRecord> getOverdueBooks() {
        return borrowRecordRepository.findOverdueBooks(LocalDate.now());
    }

    public List<BorrowRecord> getOverdueByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return borrowRecordRepository.findOverdueByMember(member, LocalDate.now());
    }

    @Transactional
    public void markAsLost(Long borrowRecordId, Double replacementCost) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        record.setStatus(BorrowRecord.BorrowStatus.LOST);
        borrowRecordRepository.save(record);

        // Create fine for lost book
        fineService.createFine(record.getMember(), record, replacementCost, Fine.FineType.LOST_BOOK,
                "Lost book: " + record.getBook().getTitle());

        // Update book inventory
        Book book = record.getBook();
        book.setTotalCopies(book.getTotalCopies() - 1);
        bookRepository.save(book);
    }
}
