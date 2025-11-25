package com.asystems.read4u.service;

import com.asystems.read4u.entity.*;
import com.asystems.read4u.repository.FineRepository;
import com.asystems.read4u.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;

    private static final double DAILY_FINE_RATE = 0.50;
    private static final double MAX_FINE_PER_BOOK = 25.00;

    public Double calculateOverdueFine(BorrowRecord record) {
        if (!record.isOverdue()) {
            return 0.0;
        }

        long daysOverdue = record.getDaysOverdue();
        double fine = daysOverdue * DAILY_FINE_RATE;

        return Math.min(fine, MAX_FINE_PER_BOOK);
    }

    @Transactional
    public Fine createFine(Member member, BorrowRecord borrowRecord, Double amount,
                           Fine.FineType fineType, String description) {
        Fine fine = Fine.builder()
                .member(member)
                .borrowRecord(borrowRecord)
                .amount(amount)
                .fineType(fineType)
                .description(description)
                .build();

        // Update member's outstanding fines
        member.setOutstandingFines(member.getOutstandingFines() + amount);
        memberRepository.save(member);

        return fineRepository.save(fine);
    }

    @Transactional
    public Fine payFine(Long fineId, String paymentMethod) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found"));

        if (fine.getIsPaid()) {
            throw new RuntimeException("Fine is already paid");
        }

        fine.setIsPaid(true);
        fine.setPaidAt(LocalDateTime.now());
        fine.setPaymentMethod(paymentMethod);

        // Update member's outstanding fines
        Member member = fine.getMember();
        member.setOutstandingFines(Math.max(0, member.getOutstandingFines() - fine.getAmount()));
        memberRepository.save(member);

        return fineRepository.save(fine);
    }

    @Transactional
    public void payAllFines(Long memberId, String paymentMethod) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Fine> unpaidFines = fineRepository.findByMemberAndIsPaidFalse(member);

        for (Fine fine : unpaidFines) {
            fine.setIsPaid(true);
            fine.setPaidAt(LocalDateTime.now());
            fine.setPaymentMethod(paymentMethod);
            fineRepository.save(fine);
        }

        member.setOutstandingFines(0.0);
        memberRepository.save(member);
    }

    public List<Fine> getUnpaidFines(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return fineRepository.findByMemberAndIsPaidFalse(member);
    }

    public List<Fine> getAllFines(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return fineRepository.findByMember(member);
    }

    public Double getTotalUnpaidFines(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Double total = fineRepository.getTotalUnpaidFines(member);
        return total != null ? total : 0.0;
    }

    public List<Fine> getAllUnpaidFines() {
        return fineRepository.findAllUnpaidFines();
    }

    @Transactional
    public Fine waiveFine(Long fineId, String reason) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found"));

        if (fine.getIsPaid()) {
            throw new RuntimeException("Fine is already paid");
        }

        // Update member's outstanding fines
        Member member = fine.getMember();
        member.setOutstandingFines(Math.max(0, member.getOutstandingFines() - fine.getAmount()));
        memberRepository.save(member);

        // Mark as paid (waived)
        fine.setIsPaid(true);
        fine.setPaidAt(LocalDateTime.now());
        fine.setPaymentMethod("WAIVED: " + reason);

        return fineRepository.save(fine);
    }

    public double getDailyFineRate() {
        return DAILY_FINE_RATE;
    }

    public double getMaxFinePerBook() {
        return MAX_FINE_PER_BOOK;
    }
}
