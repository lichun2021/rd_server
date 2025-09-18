CREATE TABLE `manhattan` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `swId` int(11) NOT NULL DEFAULT '0',
  `stage` int(11) NOT NULL DEFAULT '0',
  `posLevel` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `deployed` int(11) NOT NULL DEFAULT '0',
  `cityShow` int(11) NOT NULL DEFAULT '0',
  `base` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;