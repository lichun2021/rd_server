CREATE TABLE `activity_plant_weapon_back` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `drawTimes` int(11) NOT NULL DEFAULT '0',
 `freeTimes` int(11) NOT NULL DEFAULT '0',
 `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `dayTime` bigint(20) NOT NULL DEFAULT '0',
 `buff` int(11) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;