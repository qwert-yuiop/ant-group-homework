package com.ant.demo;

import com.ant.dto.AllocationSupplyResult;
import com.ant.utils.FundPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class FundPoolDemo1 {
    @Autowired
    private FundPoolUtils fundPoolUtils;

    /**
     * 从资金池中分配指定金额，使剩余余额方差最小
     * demo1的模拟场景通过指定输入获取
     *
     * @param pools            资金池当前余额列表
     * @param amountToWithdraw 需要支出的总金额
     * @return 每个资金池应支出的金额列表
     * @throws IllegalArgumentException 当输入参数不合法时抛出
     */
    public List<AllocationSupplyResult> allocateWithdrawals(List<BigDecimal> pools, BigDecimal amountToWithdraw) {
        // 输入验证
        fundPoolUtils.validateInput(pools, amountToWithdraw);

        int n = pools.size();
        // 计算总余额
        BigDecimal totalBalance = pools.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算剩余总金额
        BigDecimal remainingTotal = totalBalance.subtract(amountToWithdraw);

        // 计算目标余额（所有资金池最终应达到的理想余额）
        BigDecimal targetBalance = remainingTotal.divide(new BigDecimal(n), 10, RoundingMode.HALF_UP);

        // 计算每个资金池应支出的金额
        List<BigDecimal> withdrawals = new ArrayList<>(n);
        List<AllocationSupplyResult> allocationSupplyResults = new ArrayList<>(n);
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (BigDecimal pool : pools) {
            // 应支出金额 = 当前余额 - 目标余额
            BigDecimal withdrawal = pool.subtract(targetBalance).setScale(2, RoundingMode.HALF_UP);
            withdrawals.add(withdrawal);
            calculatedTotal = calculatedTotal.add(withdrawal);
        }

        // 处理由于四舍五入导致的总支出不匹配问题
        fundPoolUtils.adjustForRoundingErrors(withdrawals, calculatedTotal, amountToWithdraw);

        for (int i = 0; i < n; i++) {
            allocationSupplyResults.add(new AllocationSupplyResult(i, pools.get(i),
                    withdrawals.get(i).setScale(2, RoundingMode.HALF_UP),
                    pools.get(i).subtract(withdrawals.get(i).setScale(2, RoundingMode.HALF_UP))));
            log.info(allocationSupplyResults.get(i).toString());
        }
        return allocationSupplyResults;
    }
}
