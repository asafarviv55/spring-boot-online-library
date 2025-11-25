package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Book;
import com.asystems.read4u.entity.Member;
import com.asystems.read4u.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberAndStatus(Member member, Reservation.ReservationStatus status);

    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.book = :book AND r.status = 'PENDING' ORDER BY r.queuePosition ASC")
    List<Reservation> findPendingReservationsForBook(@Param("book") Book book);

    Optional<Reservation> findByBookAndMemberAndStatus(Book book, Member member, Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'AVAILABLE' AND r.expiryDate < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book = :book AND r.status = 'PENDING'")
    int countPendingReservationsForBook(@Param("book") Book book);
}
