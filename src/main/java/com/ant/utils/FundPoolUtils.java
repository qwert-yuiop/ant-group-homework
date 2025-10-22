package com.ant.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class FundPoolUtils {
    /**
     * 验证输入参数的合法性
     */
    public void validateInput(List<BigDecimal> pools, BigDecimal amountToWithdraw) {
        if (pools == null || pools.isEmpty()) {
            throw new IllegalArgumentException("资金池列表不能为空");
        }
        if (amountToWithdraw == null) {
            throw new IllegalArgumentException("支出金额不能为null");
        }
        if (amountToWithdraw.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("支出金额不能为负数");
        }
        for (int i = 0; i < pools.size(); i++) {
            if (pools.get(i) == null) {
                throw new IllegalArgumentException("资金池余额不能为null，索引: " + i);
            }
        }
    }

    /**
     * 调整由于四舍五入导致的总支出不匹配问题
     */
    public void adjustForRoundingErrors(List<BigDecimal> withdrawals,
                                         BigDecimal calculatedTotal,
                                         BigDecimal targetTotal) {
        BigDecimal difference = calculatedTotal.subtract(targetTotal).setScale(2, RoundingMode.HALF_UP);

        // 如果差值为0，无需调整
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 调整第一个资金池的支出金额来抵消差值
        BigDecimal first = withdrawals.get(0);
        withdrawals.set(0, first.subtract(difference).setScale(2, RoundingMode.HALF_UP));
    }
}
