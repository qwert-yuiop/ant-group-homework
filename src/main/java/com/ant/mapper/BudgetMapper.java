package com.ant.mapper;

import com.ant.entity.Budget;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
    /**
     * 扣减预算（使用悲观锁确保原子性）
     * @param id 预算记录ID
     * @param consumption 扣减金额
     * @return 影响行数（1表示成功，0表示失败）
     */
    int deductBudget(@Param("id") Long id, @Param("consumption") BigDecimal consumption);
}
