package com.example.couponsystem.service;

import com.example.couponsystem.domain.Coupon;
import com.example.couponsystem.producer.CouponCreateProducer;
import com.example.couponsystem.repository.AppliedUserRepository;
import com.example.couponsystem.repository.CouponCountRepository;
import com.example.couponsystem.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;
    private final AppliedUserRepository appliedUserRepository;

    public void apply(Long userId) {
//        long count = couponRepository.count();
        Long res = appliedUserRepository.add(userId);
        if (res != 1) {
            return;
        }

        Long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

//        couponRepository.save(new Coupon(userId));
        couponCreateProducer.create(userId);
    }
}
