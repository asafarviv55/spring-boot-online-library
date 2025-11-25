package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Fine;
import com.asystems.read4u.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByMemberAndIsPaidFalse(Member member);

    List<Fine> findByMember(Member member);

    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.member = :member AND f.isPaid = false")
    Double getTotalUnpaidFines(@Param("member") Member member);

    @Query("SELECT f FROM Fine f WHERE f.isPaid = false ORDER BY f.createdAt DESC")
    List<Fine> findAllUnpaidFines();
}
