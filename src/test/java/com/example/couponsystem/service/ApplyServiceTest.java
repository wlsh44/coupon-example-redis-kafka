package com.example.couponsystem.service;

import com.example.couponsystem.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void clear() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("쿠폰 하나 응모")
    void test() throws Exception {
        //given


        //when
        applyService.apply(1L);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 명 쿠폰 응모")
    void applyConcurrent() throws Exception {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Thread.sleep(1000 * 10);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("한 명 여러 쿠폰 응모")
    void applyConcurrentOneUser() throws Exception {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Thread.sleep(1000 * 10);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(1);
    }
}