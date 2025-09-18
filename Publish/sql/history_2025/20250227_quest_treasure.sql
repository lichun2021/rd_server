CREATE TABLE `activity_quest_treasure` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `gameInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `gameRefreshCount` int(11) NOT NULL DEFAULT '0',
 `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `boxScore` int(11) NOT NULL DEFAULT '0',
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `initTime` bigint(20) NOT NULL DEFAULT '0',
 `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;