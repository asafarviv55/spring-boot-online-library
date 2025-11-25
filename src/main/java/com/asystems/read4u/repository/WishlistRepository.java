package com.asystems.read4u.repository;

import com.asystems.read4u.entity.Book;
import com.asystems.read4u.entity.Member;
import com.asystems.read4u.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByMemberOrderByPriorityDescAddedAtDesc(Member member);

    Optional<WishlistItem> findByMemberAndBook(Member member, Book book);

    List<WishlistItem> findByBookAndNotifyWhenAvailableTrue(Book book);

    boolean existsByMemberAndBook(Member member, Book book);
}
