CREATE TABLE `activity_honour_mobilize` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `chooseId` int(11) NOT NULL DEFAULT '0',
 `freeCount` int(11) NOT NULL DEFAULT '0',
 `lotteryCount` int(11) NOT NULL DEFAULT '0',
 `lotteryTotalCount` int(11) NOT NULL DEFAULT '0',
 `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `initTime` bigint(20) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




  


