# ant-group-homework
分布式环境下的预算扣减实现 &amp;&amp; 资金池分配问题 answer

题目名称： 分布式环境下的预算扣减实现
背景描述：
在一个高并发的分布式集群环境中，系统需要管理一笔全局共享的预算金额，该预算存储于数据库中。为了确保在多线程、多节点环境下预算扣减的准确性和一致性，你需要设计并实现一个线程安全的预算扣减方法。考虑可能出现的并发扣减请求，确保预算不会被超额扣除，并且能够高效处理大量扣减请求。
任务要求：
设计并实现一个名为BudgetService的Java类，包含以下核心方法：
public class BudgetService {
    /**
     * 实现预算扣减逻辑。
     * @param consumption 单次请求扣除的预算金额，为正整数。
     * @return 如果扣减成功返回true，否则返回false（例如预算不足时）。
     */
    public boolean budgetDeduct(int consumption) {
        // 初始化预算金额，此金额应改为从数据库或其他持久化存储中读取
        int totalBudget = ...
        // 请在此处实现扣减逻辑
    }
    
    // 其他辅助方法或字段声明，如有需要可自行添加
}




# 线下笔试题-资金池分配问题

## 一、背景说明

存在 $ n $ 个资金池 $ p = \{p_1, p_2, ..., p_n\} $，其中 $ p_n $ 表示第 $ n $ 个资金池的当前余额，各资金池初始余额不一定相同。

现需从这些资金池中支出总额为 $ f $ 的资金，$ f $ 可自由分割。

## 二、问题描述

设计一个过程、函数或服务，实现以下目标：

- 将支出金额 $ f $ 从各个资金池中合理分配；
- 分配完成后，所有资金池的余额尽可能接近（即方差最小）；
- 允许资金池在支出后处于负债状态（即余额可 < 0）；
- 需考虑：
  - 编码设计合理性
  - 容错处理机制
  - 单元测试完备性

## 三、输出格式定义

使用 `AllocationSupplyResult` 类表示每次分配的结果，包含以下字段：

| 字段名 | 含义 |
|--------|------|
| `fundPoolId` | 资金池标识 |
| `preAllocationShortfalls` | 支出前余额 |
| `allocationShortfalls` | 本次支出金额 |
| `afterAllocationShortfalls` | 支出后余额 |

> **注**：字段名中“Shortfalls”可能是命名误差，实际应为“Balance”或“Amount”，但此处保留原命名以符合上下文。

## 四、示例演示

### 示例 1：构造 20 个资金池，不进行支出（f = 0）

初始余额随机生成，支出为 0。

```text
AllocationSupplyResult [fundPoolId=0, preAllocationShortfalls=20, allocationShortfalls=0, afterAllocationShortfalls=20]
AllocationSupplyResult [fundPoolId=1, preAllocationShortfalls=47, allocationShortfalls=0, afterAllocationShortfalls=47]
AllocationSupplyResult [fundPoolId=2, preAllocationShortfalls=21, allocationShortfalls=0, afterAllocationShortfalls=21]
AllocationSupplyResult [fundPoolId=3, preAllocationShortfalls=41, allocationShortfalls=0, afterAllocationShortfalls=41]
AllocationSupplyResult [fundPoolId=4, preAllocationShortfalls=29, allocationShortfalls=0, afterAllocationShortfalls=29]
AllocationSupplyResult [fundPoolId=5, preAllocationShortfalls=9, allocationShortfalls=0, afterAllocationShortfalls=9]
AllocationSupplyResult [fundPoolId=6, preAllocationShortfalls=76, allocationShortfalls=0, afterAllocationShortfalls=76]
AllocationSupplyResult [fundPoolId=7, preAllocationShortfalls=97, allocationShortfalls=0, afterAllocationShortfalls=97]
AllocationSupplyResult [fundPoolId=8, preAllocationShortfalls=29, allocationShortfalls=0, afterAllocationShortfalls=29]
AllocationSupplyResult [fundPoolId=9, preAllocationShortfalls=66, allocationShortfalls=0, afterAllocationShortfalls=66]
AllocationSupplyResult [fundPoolId=10, preAllocationShortfalls=60, allocationShortfalls=0, afterAllocationShortfalls=60]
AllocationSupplyResult [fundPoolId=11, preAllocationShortfalls=92, allocationShortfalls=0, afterAllocationShortfalls=92]
AllocationSupplyResult [fundPoolId=12, preAllocationShortfalls=94, allocationShortfalls=0, afterAllocationShortfalls=94]
AllocationSupplyResult [fundPoolId=13, preAllocationShortfalls=27, allocationShortfalls=0, afterAllocationShortfalls=27]
AllocationSupplyResult [fundPoolId=14, preAllocationShortfalls=43, allocationShortfalls=0, afterAllocationShortfalls=43]
AllocationSupplyResult [fundPoolId=15, preAllocationShortfalls=75, allocationShortfalls=0, afterAllocationShortfalls=75]
AllocationSupplyResult [fundPoolId=16, preAllocationShortfalls=41, allocationShortfalls=0, afterAllocationShortfalls=41]
AllocationSupplyResult [fundPoolId=17, preAllocationShortfalls=74, allocationShortfalls=0, afterAllocationShortfalls=74]
AllocationSupplyResult [fundPoolId=18, preAllocationShortfalls=64, allocationShortfalls=0, afterAllocationShortfalls=64]
AllocationSupplyResult [fundPoolId=19, preAllocationShortfalls=61, allocationShortfalls=0, afterAllocationShortfalls=61]
```

✅ **观察**：所有 `allocationShortfalls` 均为 0，余额未变化。

---

### 示例 2：构造 20 个资金池，支出总额 f = 5000

分配后，各资金池余额趋于一致（最终均为 -196）。

```text
AllocationSupplyResult [fundPoolId=0, preAllocationShortfalls=20, allocationShortfalls=216, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=1, preAllocationShortfalls=47, allocationShortfalls=243, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=2, preAllocationShortfalls=21, allocationShortfalls=217, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=3, preAllocationShortfalls=41, allocationShortfalls=237, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=4, preAllocationShortfalls=29, allocationShortfalls=225, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=5, preAllocationShortfalls=9, allocationShortfalls=205, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=6, preAllocationShortfalls=76, allocationShortfalls=272, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=7, preAllocationShortfalls=97, allocationShortfalls=293, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=8, preAllocationShortfalls=29, allocationShortfalls=225, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=9, preAllocationShortfalls=66, allocationShortfalls=262, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=10, preAllocationShortfalls=60, allocationShortfalls=256, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=11, preAllocationShortfalls=92, allocationShortfalls=288, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=12, preAllocationShortfalls=94, allocationShortfalls=290, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=13, preAllocationShortfalls=27, allocationShortfalls=223, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=14, preAllocationShortfalls=43, allocationShortfalls=239, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=15, preAllocationShortfalls=75, allocationShortfalls=271, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=16, preAllocationShortfalls=41, allocationShortfalls=237, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=17, preAllocationShortfalls=74, allocationShortfalls=270, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=18, preAllocationShortfalls=64, allocationShortfalls=260, afterAllocationShortfalls=-196]
AllocationSupplyResult [fundPoolId=19, preAllocationShortfalls=61, allocationShortfalls=257, afterAllocationShortfalls=-196]
```

✅ **观察**：支出后所有资金池余额均为 `-196`，差额为 0，达到最优均衡。

---

### 示例 3：构造 10 个资金池，余额为 1 到 10，支出总额 f = 15

```text
AllocationSupplyResult [fundPoolId=1, preAllocationShortfalls=1, allocationShortfalls=0, afterAllocationShortfalls=1]
AllocationSupplyResult [fundPoolId=2, preAllocationShortfalls=2, allocationShortfalls=0, afterAllocationShortfalls=2]
AllocationSupplyResult [fundPoolId=3, preAllocationShortfalls=3, allocationShortfalls=0, afterAllocationShortfalls=3]
AllocationSupplyResult [fundPoolId=4, preAllocationShortfalls=4, allocationShortfalls=0, afterAllocationShortfalls=4]
AllocationSupplyResult [fundPoolId=5, preAllocationShortfalls=5, allocationShortfalls=0, afterAllocationShortfalls=5]
AllocationSupplyResult [fundPoolId=6, preAllocationShortfalls=6, allocationShortfalls=1, afterAllocationShortfalls=5]
AllocationSupplyResult [fundPoolId=7, preAllocationShortfalls=7, allocationShortfalls=2, afterAllocationShortfalls=5]
AllocationSupplyResult [fundPoolId=8, preAllocationShortfalls=8, allocationShortfalls=3, afterAllocationShortfalls=5]
AllocationSupplyResult [fundPoolId=9, preAllocationShortfalls=9, allocationShortfalls=4, afterAllocationShortfalls=5]
AllocationSupplyResult [fundPoolId=10, preAllocationShortfalls=10, allocationShortfalls=5, afterAllocationShortfalls=5]
```

✅ **观察**：
- 前 5 个资金池余额较小，未被扣除；
- 后 5 个资金池依次承担 `1+2+3+4+5 = 15` 的支出；
- 最终所有资金池余额最大为 `5`，最小为 `1`，尽可能拉平。

---
