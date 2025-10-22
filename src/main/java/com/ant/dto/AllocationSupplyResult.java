package com.ant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationSupplyResult {
    /**
     * 资金池ID
     */
    private Integer fundPoolId;
    /**
     * 支出前余额
     */
    private BigDecimal preAllocationShortfalls;
    /**
     * 本次支出金额
     */
    private BigDecimal allocationShortfalls;
    /**
     * 支出后余额
     */
    private BigDecimal afterAllocationShortfalls;

    @Override
    public String toString() {
        return "AllocationSupplyResult{" +
                "fundPoolId=" + fundPoolId +
                ", preAllocationShortfalls=" + preAllocationShortfalls +
                ", allocationShortfalls=" + allocationShortfalls +
                ", afterAllocationShortfalls=" + afterAllocationShortfalls +
                '}';
    }
}
