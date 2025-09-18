alter table `monster` add column `bosskillInfo` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `monster` add column `bosskillRefreshDay` int(11) NOT NULL DEFAULT '0';