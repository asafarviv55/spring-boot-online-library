package com.asystems.read4u.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BorrowStatus status = BorrowStatus.BORROWED;

    @Builder.Default
    private Integer renewalCount = 0;

    @Builder.Default
    private Integer maxRenewals = 2;

    private Double fineAmount;

    private Boolean finePaid;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BorrowStatus {
        BORROWED, RETURNED, OVERDUE, LOST
    }

    public boolean isOverdue() {
        return status == BorrowStatus.BORROWED && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
}
