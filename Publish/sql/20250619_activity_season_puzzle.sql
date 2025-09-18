CREATE TABLE `activity_season_puzzle` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `dayTime` bigint(20) NOT NULL DEFAULT '0',
 `itemSendCount` int(11) NOT NULL DEFAULT '0',
 `itemGetCount` int(11) NOT NULL DEFAULT '0',
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `callHelpInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `itemSetInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;