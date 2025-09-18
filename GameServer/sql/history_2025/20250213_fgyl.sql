CREATE TABLE `guild_fgyl` (
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `passLevel` int(11) NOT NULL DEFAULT '0',
  `useTime` int(11) NOT NULL DEFAULT '0',
  `passTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`guildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `commander` ADD COLUMN `fgylData` text COLLATE utf8mb4_unicode_ci NOT NULL;