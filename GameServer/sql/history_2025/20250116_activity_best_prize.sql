CREATE TABLE `activity_best_prize` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `dayTime` bigint(20) NOT NULL DEFAULT '0',
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `drawConsume` int(11) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;