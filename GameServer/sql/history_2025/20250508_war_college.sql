ALTER TABLE `player_war_college` ADD COLUMN `firstReward` text COLLATE utf8mb4_unicode_ci NOT NULL;
alter table `player_war_college` add column `helpRwardDay` int(11) NOT NULL DEFAULT '0';
alter table `player_war_college` add column `helpRwardCount` int(11) NOT NULL DEFAULT '0';

