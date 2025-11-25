package com.asystems.read4u.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String address;

    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MembershipType membershipType = MembershipType.STANDARD;

    @Builder.Default
    private LocalDate membershipStartDate = LocalDate.now();

    private LocalDate membershipExpiryDate;

    @Builder.Default
    private Integer maxBooksAllowed = 5;

    @Builder.Default
    private Integer currentBorrowedCount = 0;

    @Builder.Default
    private Double outstandingFines = 0.0;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WishlistItem> wishlistItems = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Fine> fines = new ArrayList<>();

    public enum MembershipType {
        STANDARD, PREMIUM, STUDENT, SENIOR
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
