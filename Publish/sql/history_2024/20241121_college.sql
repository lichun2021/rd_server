alter table `college_member` add column `scoreInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `college_member` add column `shopInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `college_member` add column `vitInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `college_member` add column `missionInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `college_member` add column `giftInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;

alter table `college_info` add column `collegeName` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `college_info` add column `expTotal` int(11) NOT NULL DEFAULT '0';
alter table `college_info` add column `level` int(11) NOT NULL DEFAULT '0';
alter table `college_info` add column `exp` int(11) NOT NULL DEFAULT '0';
alter table `college_info` add column `vitality` double DEFAULT '0',
alter table `college_info` add column `joinFree` int(11) NOT NULL DEFAULT '0';
alter table `college_info` add column `reNameCount` int(11) NOT NULL DEFAULT '0';
alter table `college_info` add column `statisticsData` text COLLATE utf8mb4_unicode_ci NOT NULL;

