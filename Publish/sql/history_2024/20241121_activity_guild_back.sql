DROP TABLE IF EXISTS `activity_guild_back`;
CREATE TABLE `activity_guild_back` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `getBox` int(11) NOT NULL DEFAULT '0',
  `useBox` int(11) NOT NULL DEFAULT '0',
  `dayBoxTime` bigint(20) NOT NULL DEFAULT '0',
  `dropCount` int(11) NOT NULL DEFAULT '0',
  `dayPoolCount` int(11) NOT NULL DEFAULT '0',
  `goldNum` int(11) NOT NULL DEFAULT '0',
  `vitNum` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;