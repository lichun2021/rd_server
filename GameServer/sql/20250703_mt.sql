alter table `commander` add column `mtpremarch` int(11) NOT NULL DEFAULT '0';

DROP TABLE IF EXISTS `activity_material_transport`;
CREATE TABLE `activity_material_transport` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `truckNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `truckRobNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainRobNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `specialTrainNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


