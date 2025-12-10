package com.example.demo.repository;

import com.example.demo.domain.CouponMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponMasterRepository extends JpaRepository<CouponMaster, Long> {

    Optional<CouponMaster> findByCode(String code);
}