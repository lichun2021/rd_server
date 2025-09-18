CREATE TABLE `mecha_core` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `rankLevel` int(11) NOT NULL DEFAULT '0',
  `techInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `slotInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `suitInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `suitCount` int(11) NOT NULL DEFAULT '0',
  `workSuit` int(11) NOT NULL DEFAULT '0',
  `unlockedCityShow` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;