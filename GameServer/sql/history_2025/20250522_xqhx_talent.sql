CREATE TABLE `xqhx_talent` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `talentId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
