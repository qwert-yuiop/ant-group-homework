package com.ant.test;

import com.ant.service.FundPoolService;
import com.ant.dto.AllocationSupplyResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

// 启动Spring上下文，扫描并加载所有Bean
@RunWith(SpringRunner.class)
@SpringBootTest
public class FundPoolTest {

    // 注入真实的业务服务（非模拟）
    @Autowired
    private FundPoolService fundPoolService;

    // 测试用例中常用的金额常量
    private static final BigDecimal BD_100 = new BigDecimal("100.00");
    private static final BigDecimal BD_200 = new BigDecimal("200.00");
    private static final BigDecimal BD_300 = new BigDecimal("300.00");
    private static final BigDecimal BD_0 = new BigDecimal("0.00");
    private static final BigDecimal BD_400 = new BigDecimal("400.00");
    private static final BigDecimal BD_500 = new BigDecimal("500.00");
    private static final BigDecimal BD_200_33 = new BigDecimal("200.33");
    private static final BigDecimal BD_300_67 = new BigDecimal("300.67");


    /**
     * 测试场景1：正常分配（无四舍五入误差）
     * 资金池：[100, 200, 300]，支出300
     * 总余额：600 → 剩余总金额：300 → 目标余额：100
     * 预期支出：0、100、200 → 总支出300
     */
    @Test
    public void testAllocateWithdrawals_NormalCase() {
        // 输入参数
        List<BigDecimal> pools = Arrays.asList(BD_100, BD_200, BD_300);
        BigDecimal amountToWithdraw = BD_300;

        // 执行方法
        List<AllocationSupplyResult> results = fundPoolService.allocateWithdrawals(pools, amountToWithdraw);

        // 断言结果数量
        assertEquals(3, results.size());

        // 断言每个资金池的支出和剩余余额
        // 第1个资金池：100 - 0 = 100（剩余）
        AllocationSupplyResult result0 = results.get(0);
        assertEquals(BD_100, result0.getPreAllocationShortfalls());
        assertEquals(BD_0, result0.getAllocationShortfalls());
        assertEquals(BD_100, result0.getAfterAllocationShortfalls());

        // 第2个资金池：200 - 100 = 100（剩余）
        AllocationSupplyResult result1 = results.get(1);
        assertEquals(BD_100, result1.getAllocationShortfalls());
        assertEquals(BD_100, result1.getAfterAllocationShortfalls());

        // 第3个资金池：300 - 200 = 100（剩余）
        AllocationSupplyResult result2 = results.get(2);
        assertEquals(BD_200, result2.getAllocationShortfalls());
        assertEquals(BD_100, result2.getAfterAllocationShortfalls());

        // 断言总支出等于目标金额
        BigDecimal totalWithdrawn = results.stream()
                .map(AllocationSupplyResult::getAllocationShortfalls)
                .reduce(BD_0, BigDecimal::add);
        assertEquals(amountToWithdraw, totalWithdrawn);
    }


    /**
     * 测试场景2：四舍五入误差调整
     * 资金池：[200.33, 300.67]，支出100 → 总余额501 → 剩余401 → 目标余额200.5
     * 初始计算支出：(200.33-200.5)=-0.17，(300.67-200.5)=100.17 → 总支出100.00（无误差）
     * 若因精度产生误差，工具类会自动调整
     */
    @Test
    public void testAllocateWithdrawals_RoundingAdjustment() {
        List<BigDecimal> pools = Arrays.asList(BD_200_33, BD_300_67);
        BigDecimal amountToWithdraw = new BigDecimal("100");

        List<AllocationSupplyResult> results = fundPoolService.allocateWithdrawals(pools, amountToWithdraw);

        // 断言总支出
        BigDecimal totalWithdrawn = results.stream()
                .map(AllocationSupplyResult::getAllocationShortfalls)
                .reduce(BD_0, BigDecimal::add);
        assertEquals(amountToWithdraw.setScale(2), totalWithdrawn.setScale(2));

        // 断言剩余余额相等（方差最小）
        BigDecimal remaining0 = results.get(0).getAfterAllocationShortfalls().setScale(2);
        BigDecimal remaining1 = results.get(1).getAfterAllocationShortfalls().setScale(2);
        assertEquals(remaining0, remaining1);
    }


    /**
     * 测试场景3：负债场景（总余额 < 支出金额）
     * 资金池：[100, 200]，支出400 → 总余额300 → 剩余-100 → 目标余额-50
     * 预期支出：150（100 - (-50)）、250（200 - (-50)）→ 总支出400
     */
    @Test
    public void testAllocateWithdrawals_NegativeBalance() {
        List<BigDecimal> pools = Arrays.asList(BD_100, BD_200);
        BigDecimal amountToWithdraw = BD_400;

        List<AllocationSupplyResult> results = fundPoolService.allocateWithdrawals(pools, amountToWithdraw);

        // 断言支出金额
        assertEquals(new BigDecimal("150.00"), results.get(0).getAllocationShortfalls());
        assertEquals(new BigDecimal("250.00"), results.get(1).getAllocationShortfalls());

        // 断言剩余余额（负债）
        assertEquals(new BigDecimal("-50.00"), results.get(0).getAfterAllocationShortfalls());
        assertEquals(new BigDecimal("-50.00"), results.get(1).getAfterAllocationShortfalls());
    }


    /**
     * 测试场景4：单资金池场景
     * 资金池：[500]，支出200 → 剩余300 → 支出金额必为200
     */
    @Test
    public void testAllocateWithdrawals_SinglePool() {
        List<BigDecimal> pools = Collections.singletonList(BD_500);
        BigDecimal amountToWithdraw = new BigDecimal("200");

        List<AllocationSupplyResult> results = fundPoolService.allocateWithdrawals(pools, amountToWithdraw);

        assertEquals(1, results.size());
        AllocationSupplyResult result = results.get(0);
        assertEquals(amountToWithdraw, result.getAllocationShortfalls());
        assertEquals(new BigDecimal("300.00"), result.getAfterAllocationShortfalls());
    }


    /**
     * 测试场景5：输入参数校验（工具类抛出异常）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAllocateWithdrawals_InvalidInput_EmptyPools() {
        // 空资金池列表（预期抛出异常）
        fundPoolService.allocateWithdrawals(Collections.emptyList(), BD_100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocateWithdrawals_InvalidInput_NegativeAmount() {
        // 负支出金额（预期抛出异常）
        List<BigDecimal> pools = Arrays.asList(BD_100);
        fundPoolService.allocateWithdrawals(pools, new BigDecimal("-50"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocateWithdrawals_InvalidInput_NullPool() {
        // 资金池包含null（预期抛出异常）
        List<BigDecimal> pools = Arrays.asList(BD_100, null);
        fundPoolService.allocateWithdrawals(pools, BD_100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocateWithdrawals_InvalidInput_NullAmount() {
        // 支出金额为null（预期抛出异常）
        List<BigDecimal> pools = Arrays.asList(BD_100);
        fundPoolService.allocateWithdrawals(pools, null);
    }
}