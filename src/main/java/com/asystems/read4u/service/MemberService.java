package com.asystems.read4u.service;

import com.asystems.read4u.entity.Member;
import com.asystems.read4u.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Page<Member> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found with email: " + email));
    }

    public Member getMemberByMembershipNumber(String membershipNumber) {
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new RuntimeException("Member not found with membership number: " + membershipNumber));
    }

    @Transactional
    public Member registerMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Generate membership number
        member.setMembershipNumber(generateMembershipNumber());

        // Set membership expiry (1 year from now by default)
        member.setMembershipExpiryDate(LocalDate.now().plusYears(1));

        // Set max books based on membership type
        setMaxBooksForMembershipType(member);

        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);

        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setPhone(memberDetails.getPhone());
        member.setAddress(memberDetails.getAddress());
        member.setDateOfBirth(memberDetails.getDateOfBirth());

        return memberRepository.save(member);
    }

    @Transactional
    public Member upgradeMembership(Long id, Member.MembershipType newType) {
        Member member = getMemberById(id);
        member.setMembershipType(newType);
        setMaxBooksForMembershipType(member);
        return memberRepository.save(member);
    }

    @Transactional
    public Member renewMembership(Long id, int years) {
        Member member = getMemberById(id);

        LocalDate newExpiry;
        if (member.getMembershipExpiryDate().isBefore(LocalDate.now())) {
            newExpiry = LocalDate.now().plusYears(years);
        } else {
            newExpiry = member.getMembershipExpiryDate().plusYears(years);
        }

        member.setMembershipExpiryDate(newExpiry);
        return memberRepository.save(member);
    }

    @Transactional
    public void deactivateMember(Long id) {
        Member member = getMemberById(id);

        // Check for outstanding borrows
        if (member.getCurrentBorrowedCount() > 0) {
            throw new RuntimeException("Cannot deactivate member with borrowed books");
        }

        // Check for outstanding fines
        if (member.getOutstandingFines() > 0) {
            throw new RuntimeException("Cannot deactivate member with outstanding fines");
        }

        member.setIsActive(false);
        memberRepository.save(member);
    }

    public List<Member> getMembersWithOutstandingFines() {
        return memberRepository.findMembersWithOutstandingFines();
    }

    public List<Member> getMembersWithExpiringMembership(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return memberRepository.findMembersWithExpiringMembership(expiryDate);
    }

    private String generateMembershipNumber() {
        return "MEM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void setMaxBooksForMembershipType(Member member) {
        switch (member.getMembershipType()) {
            case PREMIUM:
                member.setMaxBooksAllowed(10);
                break;
            case STUDENT:
                member.setMaxBooksAllowed(7);
                break;
            case SENIOR:
                member.setMaxBooksAllowed(8);
                break;
            default:
                member.setMaxBooksAllowed(5);
        }
    }
}
