alter table `activity_core_explore` add column `autoPick` int(11) NOT NULL DEFAULT '0';
alter table `activity_core_explore` add column `autoPickRewards` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `activity_core_explore` add column `autoPickConsumes` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `activity_core_explore` add column `specialItems` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `activity_core_explore` add column `oreItems` text COLLATE utf8mb4_unicode_ci NOT NULL;