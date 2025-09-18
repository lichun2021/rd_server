
alter table `monster` add column `dropLimitInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `monster` add column `dropLimitRefreshDay` int(11) NOT NULL DEFAULT '0';