package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    private static final int RESERVATION_EXPIRY_DAYS = 3;

    @Transactional
    public Reservation reserveBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Check if member already has a reservation for this book
        if (reservationRepository.findByBookAndMemberAndStatus(book, member,
                Reservation.ReservationStatus.PENDING).isPresent()) {
            throw new RuntimeException("You already have a reservation for this book");
        }

        // If book is available, suggest borrowing instead
        if (book.getAvailableCopies() > 0) {
            throw new RuntimeException("Book is currently available. You can borrow it directly.");
        }

        // Get current queue position
        List<Reservation> existingReservations = reservationRepository.findPendingReservationsForBook(book);
        int queuePosition = existingReservations.size() + 1;

        Reservation reservation = Reservation.builder()
                .book(book)
                .member(member)
                .reservationDate(LocalDateTime.now())
                .status(Reservation.ReservationStatus.PENDING)
                .queuePosition(queuePosition)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new RuntimeException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING &&
            reservation.getStatus() != Reservation.ReservationStatus.AVAILABLE) {
            throw new RuntimeException("Reservation cannot be cancelled");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Update queue positions for remaining reservations
        updateQueuePositions(reservation.getBook());
    }

    private void updateQueuePositions(Book book) {
        List<Reservation> pendingReservations = reservationRepository.findPendingReservationsForBook(book);
        int position = 1;
        for (Reservation res : pendingReservations) {
            res.setQueuePosition(position++);
            reservationRepository.save(res);
        }
    }

    public List<Reservation> getMemberReservations(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return reservationRepository.findByMemberAndStatus(member, Reservation.ReservationStatus.PENDING);
    }

    public List<Reservation> getAvailableReservations(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return reservationRepository.findByMemberAndStatus(member, Reservation.ReservationStatus.AVAILABLE);
    }

    public int getQueuePosition(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return reservationRepository.findByBookAndMemberAndStatus(book, member, Reservation.ReservationStatus.PENDING)
                .map(Reservation::getQueuePosition)
                .orElse(-1);
    }

    public int getTotalReservationsForBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return reservationRepository.countPendingReservationsForBook(book);
    }

    @Transactional
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            // Notify next in queue
            Book book = reservation.getBook();
            List<Reservation> pendingReservations = reservationRepository.findPendingReservationsForBook(book);
            if (!pendingReservations.isEmpty()) {
                Reservation nextReservation = pendingReservations.get(0);
                nextReservation.setStatus(Reservation.ReservationStatus.AVAILABLE);
                nextReservation.setNotifiedAt(LocalDateTime.now());
                nextReservation.setExpiryDate(LocalDateTime.now().plusDays(RESERVATION_EXPIRY_DAYS));
                reservationRepository.save(nextReservation);
            }

            updateQueuePositions(book);
        }
    }
}
