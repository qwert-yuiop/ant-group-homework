package com.ant.service;

import com.ant.entity.Budget;
import com.ant.mapper.BudgetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BudgetService {
    @Resource
    private BudgetMapper budgetMapper;

    // 预算记录固定ID为1
    private static final Long BUDGET_ID = 1L;

    /**
     * 预算扣减逻辑
     *
     * @param consumption 扣减金额（正数）
     * @return 扣减成功返回true，否则返回false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean budgetDeduct(BigDecimal consumption) {
        // 校验输入必须为正数
        if (consumption.compareTo(new BigDecimal("0.00")) <= 0) {
            return false;
        }

        // 执行扣减操作（数据库层面保证原子性）
        int affectedRows = budgetMapper.deductBudget(BUDGET_ID, consumption.setScale(2, RoundingMode.HALF_UP));

        // 影响行数为1表示扣减成功
        return affectedRows == 1;
    }

    /**
     * 获取当前预算金额（用于测试）
     */
    public BigDecimal getCurrentBudget() {
        Budget budget = budgetMapper.selectById(BUDGET_ID);
        return budget != null ? budget.getTotalAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("0.00");
    }

    /**
     * 重置预算（用于测试）
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetBudget(BigDecimal amount) {
        Budget budget = new Budget();
        budget.setId(BUDGET_ID);
        budget.setTotalAmount(amount.setScale(2, RoundingMode.HALF_UP));
        budgetMapper.updateById(budget);
    }
}