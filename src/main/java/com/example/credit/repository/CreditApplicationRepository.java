package com.example.credit.repository;

import com.example.credit.entity.CreditApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {
    List<CreditApplication> findByUserId(Long userId);
}
