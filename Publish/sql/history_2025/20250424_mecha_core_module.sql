CREATE TABLE `mecha_core_module` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cfgId` int(11) NOT NULL DEFAULT '0',
  `quality` int(11) NOT NULL DEFAULT '0',
  `randomAttr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `locked` int(11) NOT NULL DEFAULT '0',
  `loadSuitInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;