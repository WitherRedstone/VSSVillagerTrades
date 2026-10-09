# VSS村民交易 (VSS Villager Trades)

[English](#english) | [中文](#中文)

---

# **English**

## Introduction

Add VSS-compatible transactions for villagers

## Features

- **VSS Currency System** — Trades are paid using VSS currency, with the player's balance displayed at the top of the trading screen and synced in real time.
- **Currency Item Conversion** — Configurable currency items can be mapped to VSS values. For example, 1 emerald = 5 VSS by default. Supports customizing multiple items.
- **All-Level Trades Unlocked** — Villagers offer trades from all 5 levels at once, eliminating the need to grind lower-level trades to unlock higher ones.
- **Daily Restock** — Villagers automatically restock sold-out trades at the start of each new game day.
- **Refresh Trades Button** — Click to refresh the villager's trade list.

## Configuration

The mod uses a common config file to define the mapping from items to VSS value:

- Format: `"item_id, price"` — e.g. `"minecraft:emerald, 5"` means 1 emerald = 5 VSS
- Default: `minecraft:emerald, 5`
- Multiple entries are supported

## Other

- MoreJS: When using custom villager trades, you can directly set the items you want to trade. For example, to trade emeralds for redstone, simply specify emerald — the currency price is determined by the conversion defined in the config. Similarly, if you want to trade diamonds for netherite, just define the diamond's VSS currency value in the config and it will be automatically converted.

---

# **中文**

## 简介

为村民添加vss兼容交易

## 功能特性

- **VSS 货币体系** — 交易使用 VSS 货币支付，界面顶部实时显示并同步玩家余额。
- **货币物品换算** — 可配置货币物品与 VSS 的换算关系，例如默认 1 绿宝石 = 5 VSS，支持自定义多种物品。
- **全等级交易解锁** — 村民一次性展示 1 至 5 级全部交易，无需反复刷低级交易来解锁高级交易。
- **每日自动补货** — 每个游戏日到来时，村民自动补货已售罄的交易。
- **刷新交易按钮** — 点击刷新村民交易列表。

## 配置

模组使用通用配置文件，用于定义物品到 VSS 的换算关系：

- 格式：`"物品ID, 价格"` — 例如 `"minecraft:emerald, 5"` 表示 1 绿宝石 = 5 VSS
- 默认值：`minecraft:emerald, 5`
- 支持配置多个条目

## 其他
- MoreJS：用自定义村民交易时，可直接设置你需要交易的物品，比如设置绿宝石交易红石，直接写绿宝石即可，货币价格根据配置文件的换算决定；再比如你需要用钻石换下界合金，则需要在配置里定义钻石对应的VSS货币价格即可自动转换。