CREATE TABLE `activity_merge_competition` (
 `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
 `termId` int(11) NOT NULL DEFAULT '0',
 `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `awardIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
 `guildPowerTargetTime` bigint(20) NOT NULL DEFAULT '0',
 `awardIdRefreshTime` bigint(20) NOT NULL DEFAULT '0',
 `costVit` int(11) NOT NULL DEFAULT '0',
 `giftScore` int(11) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 `guildPowerTargetFinish` int(11) NOT NULL DEFAULT '0',
 PRIMARY KEY (`id`),
 KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
