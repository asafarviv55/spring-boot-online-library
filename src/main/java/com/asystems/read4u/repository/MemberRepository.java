package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMembershipNumber(String membershipNumber);

    @Query("SELECT m FROM Member m WHERE m.isActive = true AND m.outstandingFines > 0")
    List<Member> findMembersWithOutstandingFines();

    @Query("SELECT m FROM Member m WHERE m.isActive = true AND m.membershipExpiryDate <= :date")
    List<Member> findMembersWithExpiringMembership(@Param("date") java.time.LocalDate date);

    Boolean existsByEmail(String email);
}
