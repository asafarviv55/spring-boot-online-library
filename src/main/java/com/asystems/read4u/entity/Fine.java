package com.asystems.read4u.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id")
    private BorrowRecord borrowRecord;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineType fineType;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private Boolean isPaid = false;

    private LocalDateTime paidAt;

    private String paymentMethod;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum FineType {
        OVERDUE, LOST_BOOK, DAMAGED_BOOK, OTHER
    }
}
