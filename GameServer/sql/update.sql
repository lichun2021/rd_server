CREATE TABLE `recharge_back` (
  `billno` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `type` int(11) NOT NULL,
  `diamonds` int(11) NOT NULL DEFAULT '0',
  `time` bigint(20) NOT NULL DEFAULT '0',
  `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `serverId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `puid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `goodsId` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `goodsPrice` int(11) NOT NULL DEFAULT '0',
  `payMoney` int(11) NOT NULL DEFAULT '0',
  `currency` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `awardItems` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`billno`),
  KEY `playerId_index` (`playerId`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `goodsId_index` (`goodsId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


alter table `player` modify column `battlePoint` bigint(20) NOT NULL DEFAULT '0';
alter table `player` modify column `maxBattlePoint` bigint(20) NOT NULL DEFAULT '0';
alter table `guild_member` modify column `power` bigint(20) NOT NULL DEFAULT '0';


DROP TABLE IF EXISTS `activity_grow_up_boost`;
CREATE TABLE `activity_grow_up_boost` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `useConfig` int(11) NOT NULL DEFAULT '0',
 `scoreItemDetailString` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `scoreString` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `achieveItemsDay` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `achieveItemsScore` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `buyMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `activity_Lucky_star` ADD COLUMN `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `activity_Lucky_star` ADD COLUMN `dayTime` bigint(20) NOT NULL DEFAULT '0';


DROP TABLE IF EXISTS `activity_supply_crate`;
CREATE TABLE `activity_supply_crate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `round` int(11) NOT NULL DEFAULT '0',
  `isCanOPen` tinyint(1) NOT NULL DEFAULT '0',
  `isCanNext` tinyint(1) NOT NULL DEFAULT '0',
  `mult` int(11) NOT NULL DEFAULT '0',
  `crateItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxProg` int(11) NOT NULL DEFAULT '0',
  `customIndex` int(11) NOT NULL DEFAULT '0',
  `openItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `isCanDouble` tinyint(1) NOT NULL DEFAULT '0',
  `guildBoxProg` int(11) NOT NULL DEFAULT '0',
  `boxCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `activity_radiation_war_two` ADD COLUMN `guildAchieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `commander` ADD COLUMN `soulResetCd` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE `commander` ADD COLUMN `superSoldierSkin` text COLLATE utf8mb4_unicode_ci NOT NULL;

DROP TABLE IF EXISTS `activity_jijia_skin`;
CREATE TABLE `activity_jijia_skin` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `pool` int(11) NOT NULL DEFAULT '0',
  `itemStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `multiple` int(11) NOT NULL DEFAULT '0',
  `refreshTimes` int(11) NOT NULL DEFAULT '0',
  `hasFinally` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `activity_order_two` ADD COLUMN `rewardNormalLevel` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `activity_order_two` ADD COLUMN `rewardAdvanceLevel` text COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE `activity_card` ADD COLUMN `exchangeRefreshTime` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE `activity_card` ADD COLUMN `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `activity_card` ADD COLUMN `customItems` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `activity_card` ADD COLUMN `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL;


DROP TABLE IF EXISTS `activity_anniversary_gift`;
CREATE TABLE `activity_anniversary_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItemsDaily` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `activity_daiy_buy_gift`;
CREATE TABLE `activity_daiy_buy_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `itemRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



ALTER TABLE `commander` ADD COLUMN `shopData` text COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `commander` ADD COLUMN `getDressTime` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE `commander` ADD COLUMN `getDressCount` int(11) NOT NULL DEFAULT '0';


DROP TABLE IF EXISTS `activity_back_soldier_exchange`;
CREATE TABLE `activity_back_soldier_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `backTime` bigint(20) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `logoutTime` bigint(20) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeType` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `histor` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `activity_lottery_ticket`;
CREATE TABLE `activity_lottery_ticket` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `buyMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




DROP TABLE IF EXISTS `player_daily_gift_buy`;
CREATE TABLE `player_daily_gift_buy` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `itemRecord` text COLLATE utf8mb4_unicode_ci,
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `activity_shooting_practice`;
CREATE TABLE `activity_shooting_practice` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyCount` int(11) NOT NULL DEFAULT '0',
  `buyCountDaily` int(11) NOT NULL DEFAULT '0',
  `freeCount` int(11) NOT NULL DEFAULT '0',
  `scoreMax` int(11) NOT NULL DEFAULT '0',
  `scoreTotal` int(11) NOT NULL DEFAULT '0',
  `lastOverTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItemsDay` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
