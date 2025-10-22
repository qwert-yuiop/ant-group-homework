package com.ant.test;

import com.ant.service.BudgetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BudgetTest {

    @Resource
    private BudgetService budgetService;

    // 初始预算
    private static final BigDecimal INITIAL_BUDGET = new BigDecimal("10000.00");
    // 并发线程数
    private static final int THREAD_COUNT = 20;
    // 每个线程扣减次数
    private static final int DEDUCT_PER_THREAD = 50;
    // 每次扣减金额
    private static final BigDecimal CONSUMPTION_PER_TIME = new BigDecimal("1.00");

    /**
     * 测试正常扣减
     */
    @Test
    public void testNormalDeduct() {
        // 重置预算
        budgetService.resetBudget(INITIAL_BUDGET);

        // 扣减测试
        boolean result = budgetService.budgetDeduct(new BigDecimal("1000.00"));
        assertTrue(result);
        assertEquals(new BigDecimal("9000.00"), budgetService.getCurrentBudget());
    }

    /**
     * 测试预算不足情况
     */
    @Test
    public void testInsufficientBudget() {
        budgetService.resetBudget(new BigDecimal("500.00"));

        // 扣减金额大于剩余预算
        boolean result = budgetService.budgetDeduct(new BigDecimal("1000.00"));
        assertFalse(result);
        assertEquals(new BigDecimal("500.00"), budgetService.getCurrentBudget()); // 预算应保持不变
    }

    /**
     * 测试并发扣减（核心测试）
     */
    @Test
    public void testConcurrentDeduct() throws InterruptedException {
        // 重置预算
        budgetService.resetBudget(INITIAL_BUDGET);

        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        // 计数器
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        // 成功扣减次数统计
        AtomicInteger successCount = new AtomicInteger(0);

        // 启动并发任务
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < DEDUCT_PER_THREAD; j++) {
                        if (budgetService.budgetDeduct(CONSUMPTION_PER_TIME)) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        executor.shutdown();

        // 验证结果
        BigDecimal finalBudget = budgetService.getCurrentBudget();
        BigDecimal totalDeducted = INITIAL_BUDGET.subtract(finalBudget).setScale(2, RoundingMode.HALF_UP);

        // 成功扣减次数应等于总扣减金额
        assertEquals(new BigDecimal(successCount.get()).setScale(2, RoundingMode.HALF_UP), totalDeducted);
        // 最终预算不应为负数
        assertTrue(finalBudget.compareTo(new BigDecimal("0.00")) >= 0);
        // 总扣减金额不应超过初始预算
        assertTrue(totalDeducted.compareTo(INITIAL_BUDGET) <= 0);

        System.out.println("初始预算: " + INITIAL_BUDGET);
        System.out.println("成功扣减次数: " + successCount.get());
        System.out.println("最终预算: " + finalBudget);
    }
}
