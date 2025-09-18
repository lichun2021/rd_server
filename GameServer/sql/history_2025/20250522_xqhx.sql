CREATE TABLE `xqhx` (
 `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
 `season` int(11) NOT NULL DEFAULT '0',
 `usedPoint` int(11) NOT NULL DEFAULT '0',
 `createTime` bigint(20) NOT NULL DEFAULT '0',
 `updateTime` bigint(20) NOT NULL DEFAULT '0',
 `invalid` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;