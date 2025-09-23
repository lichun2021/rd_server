-- MySQL dump 10.13  Distrib 5.7.44, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: game_10001
-- ------------------------------------------------------
-- Server version	5.7.44-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accumulate_online`
--

DROP TABLE IF EXISTS `accumulate_online`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accumulate_online` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayCount` int(11) NOT NULL,
  `receivedId` int(11) NOT NULL,
  `receivedTime` bigint(20) NOT NULL,
  `onlineTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityId` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `newlyTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_Lucky_star`
--

DROP TABLE IF EXISTS `activity_Lucky_star`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_Lucky_star` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `leftGiftCnt` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `lotCnt` int(11) NOT NULL DEFAULT '0',
  `lastBuyGiftId` text COLLATE utf8mb4_unicode_ci,
  `todayRecieveBag` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_accumulate_consume`
--

DROP TABLE IF EXISTS `activity_accumulate_consume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_accumulate_consume` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_accumulate_recharge`
--

DROP TABLE IF EXISTS `activity_accumulate_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_accumulate_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_accumulate_recharge_two`
--

DROP TABLE IF EXISTS `activity_accumulate_recharge_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_accumulate_recharge_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_after_competition`
--

DROP TABLE IF EXISTS `activity_after_competition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_after_competition` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `homageTime` bigint(20) NOT NULL DEFAULT '0',
  `giftInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_airdrop_supply`
--

DROP TABLE IF EXISTS `activity_airdrop_supply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_airdrop_supply` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxNum` int(11) NOT NULL,
  `isBuy` tinyint(1) NOT NULL DEFAULT '0',
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_alliance_carnival`
--

DROP TABLE IF EXISTS `activity_alliance_carnival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_alliance_carnival` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `receiveTimes` int(11) NOT NULL DEFAULT '0',
  `initGuildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `initGuildName` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `initCityLevel` int(11) NOT NULL DEFAULT '0',
  `buyTimes` int(11) NOT NULL DEFAULT '0',
  `buyTime` bigint(20) DEFAULT '0',
  `dayBuyNumber` int(11) DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiveMissionTime` bigint(20) NOT NULL,
  `finishTimes` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `sendAdvLevel` int(11) NOT NULL DEFAULT '0',
  `exchangeNumber` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `payGiftTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_alliance_celebrate`
--

DROP TABLE IF EXISTS `activity_alliance_celebrate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_alliance_celebrate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `donate` int(11) NOT NULL,
  `rewardInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_alliance_share_glory`
--

DROP TABLE IF EXISTS `activity_alliance_share_glory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_alliance_share_glory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `termId` int(11) NOT NULL DEFAULT '0',
  `rewardInfoA` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardActivityA` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardInfoB` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardActivityB` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardEnergyLevelA` int(11) NOT NULL DEFAULT '0',
  `rewardEnergyLevelB` int(11) NOT NULL DEFAULT '0',
  `donateCountA` int(11) NOT NULL DEFAULT '0',
  `donateCountB` int(11) NOT NULL DEFAULT '0',
  `guildid` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `rewardEnergyA` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardEnergyB` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_alliance_wish`
--

DROP TABLE IF EXISTS `activity_alliance_wish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_alliance_wish` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `numbers` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `signDays` int(11) NOT NULL DEFAULT '0',
  `supplySignDays` int(11) NOT NULL DEFAULT '0',
  `lastSignTime` bigint(20) NOT NULL DEFAULT '0',
  `sendGuildCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `wishMembers` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `wishCount` int(11) NOT NULL DEFAULT '0',
  `luxuryWishCount` int(11) NOT NULL DEFAULT '0',
  `achiveWish` bigint(20) NOT NULL DEFAULT '0',
  `buyGift` int(11) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `careIgnore` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_ally_beat_back`
--

DROP TABLE IF EXISTS `activity_ally_beat_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_ally_beat_back` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `receivedTime` int(11) NOT NULL DEFAULT '0',
  `wolrdCollectTimes` int(11) DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeTimes` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_anniversary_gift`
--

DROP TABLE IF EXISTS `activity_anniversary_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_anniversary_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItemsDaily` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_appointget`
--

DROP TABLE IF EXISTS `activity_appointget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_appointget` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainCnt` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_armament_exchange`
--

DROP TABLE IF EXISTS `activity_armament_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_armament_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `exchange` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `isOpen` int(11) DEFAULT '0',
  `isFirst` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_armies_mass`
--

DROP TABLE IF EXISTS `activity_armies_mass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_armies_mass` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `stage` int(11) NOT NULL,
  `share` int(11) NOT NULL,
  `sculptureOpenCount` int(11) NOT NULL,
  `sculptures` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `freeAwards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyGifts` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_back_gift`
--

DROP TABLE IF EXISTS `activity_back_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_back_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `lotteryCount` int(11) NOT NULL,
  `lotteryTotalCount` int(11) NOT NULL,
  `lotteryTime` bigint(20) NOT NULL,
  `refreshCount` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `awards` text CHARACTER SET utf8mb4 NOT NULL,
  `awardIndex` int(11) NOT NULL,
  `lossDays` int(11) NOT NULL,
  `lossVip` int(11) NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_back_privilege`
--

DROP TABLE IF EXISTS `activity_back_privilege`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_back_privilege` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `reward` int(11) NOT NULL DEFAULT '0',
  `buffStartTime` bigint(20) NOT NULL DEFAULT '0',
  `backType` int(11) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_back_soldier_exchange`
--

DROP TABLE IF EXISTS `activity_back_soldier_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_back_soldier_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `backTime` bigint(20) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `logoutTime` bigint(20) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeType` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `histor` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_back_to_new_fly`
--

DROP TABLE IF EXISTS `activity_back_to_new_fly`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_back_to_new_fly` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `baseLevel` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_back_to_new_fly_old`
--

DROP TABLE IF EXISTS `activity_back_to_new_fly_old`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_back_to_new_fly_old` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_banner_kill`
--

DROP TABLE IF EXISTS `activity_banner_kill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_banner_kill` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `killEnemyScore` bigint(20) NOT NULL DEFAULT '0',
  `killTargetInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_battlefield_treasure`
--

DROP TABLE IF EXISTS `activity_battlefield_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_battlefield_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cellId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyTime` bigint(20) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `receiveAwardDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `passedCells` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fixedRollTimes` int(11) NOT NULL,
  `randomRollTimes` int(11) NOT NULL,
  `poolAwards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyOrdinary` int(11) DEFAULT '0',
  `buyControl` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `yijianpaotu` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_beauty_contest`
--

DROP TABLE IF EXISTS `activity_beauty_contest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_beauty_contest` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_beauty_final`
--

DROP TABLE IF EXISTS `activity_beauty_final`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_beauty_final` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_best_prize`
--

DROP TABLE IF EXISTS `activity_best_prize`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_best_prize` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `drawConsume` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `bigPoolDrawInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_black_tech`
--

DROP TABLE IF EXISTS `activity_black_tech`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_black_tech` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `poolId` int(11) NOT NULL,
  `drawTimes` int(11) NOT NULL,
  `buffId` int(11) NOT NULL,
  `deadline` bigint(20) NOT NULL,
  `activeTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_blood_corps`
--

DROP TABLE IF EXISTS `activity_blood_corps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_blood_corps` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `totalScore` int(11) NOT NULL,
  `buildScore` int(11) NOT NULL,
  `techScore` int(11) NOT NULL,
  `armyScore` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_bounty_hunter`
--

DROP TABLE IF EXISTS `activity_bounty_hunter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_bounty_hunter` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `pool` int(11) NOT NULL DEFAULT '0',
  `hitType` int(11) NOT NULL DEFAULT '0',
  `bossHp` int(11) NOT NULL DEFAULT '0',
  `lefState` int(11) NOT NULL DEFAULT '0',
  `bossBHit` int(11) NOT NULL DEFAULT '0',
  `poolARount` int(11) NOT NULL DEFAULT '0',
  `bossBNotRun` int(11) NOT NULL DEFAULT '0',
  `bossBNotDie` int(11) NOT NULL DEFAULT '0',
  `costMutil` int(11) NOT NULL DEFAULT '0',
  `rewardMutil` int(11) NOT NULL DEFAULT '0',
  `mutilCount` int(11) NOT NULL DEFAULT '0',
  `freeItemDay` int(11) NOT NULL DEFAULT '0',
  `batter` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_broken_exchange`
--

DROP TABLE IF EXISTS `activity_broken_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_broken_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `giftCostDiamond` int(11) NOT NULL,
  `exchangeNum` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastOperTime` bigint(20) NOT NULL DEFAULT '0',
  `wolrdCollectTimes` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_broken_exchange_three`
--

DROP TABLE IF EXISTS `activity_broken_exchange_three`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_broken_exchange_three` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `giftCostDiamond` int(11) NOT NULL,
  `exchangeNum` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastOperTime` bigint(20) NOT NULL DEFAULT '0',
  `wolrdCollectTimes` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_broken_exchange_two`
--

DROP TABLE IF EXISTS `activity_broken_exchange_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_broken_exchange_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `giftCostDiamond` int(11) NOT NULL,
  `exchangeNum` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastOperTime` bigint(20) NOT NULL DEFAULT '0',
  `wolrdCollectTimes` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_build_level`
--

DROP TABLE IF EXISTS `activity_build_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_build_level` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `activityItems` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_cake_share`
--

DROP TABLE IF EXISTS `activity_cake_share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_cake_share` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cakeGifts` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `name_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_card`
--

DROP TABLE IF EXISTS `activity_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_card` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cardItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastRefreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `exchangeRefreshTime` bigint(20) NOT NULL DEFAULT '0',
  `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `customItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `customLatest` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_celebration_course`
--

DROP TABLE IF EXISTS `activity_celebration_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_celebration_course` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `signNumber` int(11) NOT NULL DEFAULT '0',
  `signTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shareIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shareReward` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shareTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_celebration_food`
--

DROP TABLE IF EXISTS `activity_celebration_food`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_celebration_food` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `foodLevel` int(11) NOT NULL,
  `buyAdvance` tinyint(1) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `wolrdCollectTimes` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `beatYuriTotalTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `wishTotalTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `buySuper` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `name_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_celebration_fund`
--

DROP TABLE IF EXISTS `activity_celebration_fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_celebration_fund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `fundLevel` int(11) NOT NULL DEFAULT '0',
  `levelScore` int(11) NOT NULL DEFAULT '0',
  `buyOver` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_celebration_shop`
--

DROP TABLE IF EXISTS `activity_celebration_shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_celebration_shop` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exchange` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `_idx` (`playerId`,`termId`,`invalid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_change_server`
--

DROP TABLE IF EXISTS `activity_change_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_change_server` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `costDiamon` bigint(20) NOT NULL DEFAULT '0',
  `costGold` bigint(20) NOT NULL DEFAULT '0',
  `consumeVit` bigint(20) NOT NULL DEFAULT '0',
  `consumeSpeedTool` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_chemistry`
--

DROP TABLE IF EXISTS `activity_chemistry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_chemistry` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `backType` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_christmas_recharge`
--

DROP TABLE IF EXISTS `activity_christmas_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_christmas_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `rechargeDiamond` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_christmas_war`
--

DROP TABLE IF EXISTS `activity_christmas_war`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_christmas_war` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receivedIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_chrono_gift`
--

DROP TABLE IF EXISTS `activity_chrono_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_chrono_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyNum` int(11) NOT NULL,
  `chronoDoors` text CHARACTER SET utf8mb4 NOT NULL,
  `achieves` text CHARACTER SET utf8mb4 NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_cmmoand_academy`
--

DROP TABLE IF EXISTS `activity_cmmoand_academy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_cmmoand_academy` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `stage` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `giftList` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rankIndex` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stageParam` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_cmmoand_academy_simplify`
--

DROP TABLE IF EXISTS `activity_cmmoand_academy_simplify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_cmmoand_academy_simplify` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `stage` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `giftList` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rankIndex` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stageParam` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_cny_exam`
--

DROP TABLE IF EXISTS `activity_cny_exam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_cny_exam` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `buyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `takeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` int(11) NOT NULL DEFAULT '0',
  `chooseItems1` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `chooseItems2` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_common_exchange`
--

DROP TABLE IF EXISTS `activity_common_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_common_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_common_exchange_two`
--

DROP TABLE IF EXISTS `activity_common_exchange_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_common_exchange_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_continuous_recharge`
--

DROP TABLE IF EXISTS `activity_continuous_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_continuous_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `historyRecharge` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `currentRecharge` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_core_explore`
--

DROP TABLE IF EXISTS `activity_core_explore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_core_explore` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `currLine` int(11) NOT NULL DEFAULT '0',
  `zoneArea` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `areaBox` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `areaStone` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `techInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `freePick` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `pickBuyTimes` int(11) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `autoPick` int(11) NOT NULL DEFAULT '0',
  `autoPickRewards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `autoPickConsumes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `specialItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `oreItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_coreplate`
--

DROP TABLE IF EXISTS `activity_coreplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_coreplate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `cityLevel` int(11) NOT NULL DEFAULT '0',
  `coreplateTimes` int(11) NOT NULL DEFAULT '0',
  `boxAchieveTimes` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_custom_gift`
--

DROP TABLE IF EXISTS `activity_custom_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_custom_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `purchaseItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `count` int(11) NOT NULL DEFAULT '0',
  `freeGet` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_daily_recharge`
--

DROP TABLE IF EXISTS `activity_daily_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_daily_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_daily_recharge_new`
--

DROP TABLE IF EXISTS `activity_daily_recharge_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_daily_recharge_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `giftItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_daily_sign`
--

DROP TABLE IF EXISTS `activity_daily_sign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_daily_sign` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `termRewards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `signDays` int(11) NOT NULL DEFAULT '0',
  `signToday` int(11) NOT NULL DEFAULT '0',
  `resignDays` int(11) NOT NULL DEFAULT '0',
  `cfgPoolId` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_daiy_buy_gift`
--

DROP TABLE IF EXISTS `activity_daiy_buy_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_daiy_buy_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `itemRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_deep_treasure`
--

DROP TABLE IF EXISTS `activity_deep_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_deep_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `nineBoxStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `nextFree` bigint(20) NOT NULL DEFAULT '0',
  `purchaseItemTimes` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshtimes` bigint(20) NOT NULL DEFAULT '0',
  `lotteryCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `lotteryBuff` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_destiny_revolver`
--

DROP TABLE IF EXISTS `activity_destiny_revolver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_destiny_revolver` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `firstKick` tinyint(1) NOT NULL DEFAULT '0',
  `inTarot` tinyint(1) NOT NULL DEFAULT '0',
  `gridStr` text CHARACTER SET utf8mb4 NOT NULL,
  `nineEndTime` bigint(20) NOT NULL,
  `multiple` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text CHARACTER SET utf8mb4 NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_develop_fast`
--

DROP TABLE IF EXISTS `activity_develop_fast`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_develop_fast` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `buyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `scoreItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `taskItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_develop_fast_old`
--

DROP TABLE IF EXISTS `activity_develop_fast_old`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_develop_fast_old` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `buyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `scoreItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `taskItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_develop_spurt`
--

DROP TABLE IF EXISTS `activity_develop_spurt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_develop_spurt` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `loginTime` bigint(20) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `signInDays` int(11) NOT NULL,
  `signInTime` bigint(20) NOT NULL,
  `unlockTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_diff_info_save`
--

DROP TABLE IF EXISTS `activity_diff_info_save`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_diff_info_save` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `score` int(11) NOT NULL DEFAULT '0',
  `popCnt` int(11) NOT NULL DEFAULT '0',
  `isEnd` tinyint(1) NOT NULL DEFAULT '0',
  `clickTime` bigint(20) NOT NULL DEFAULT '0',
  `dotCnt` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_diff_new_server_tech`
--

DROP TABLE IF EXISTS `activity_diff_new_server_tech`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_diff_new_server_tech` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `buffGet` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardGet` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_direct_gift`
--

DROP TABLE IF EXISTS `activity_direct_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_direct_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyGiftTimes` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_divide_gold`
--

DROP TABLE IF EXISTS `activity_divide_gold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_divide_gold` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `giveNum` int(11) NOT NULL,
  `winRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `askForTime` bigint(20) NOT NULL,
  `compoundRedNum` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastRefreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dome_exchange`
--

DROP TABLE IF EXISTS `activity_dome_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dome_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dome_exchange_two`
--

DROP TABLE IF EXISTS `activity_dome_exchange_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dome_exchange_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_double_gift`
--

DROP TABLE IF EXISTS `activity_double_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_double_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `doubleGiftItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `accDay` int(11) NOT NULL,
  `latestPurchaseTime` bigint(20) NOT NULL,
  `freeTakenTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_double_recharge`
--

DROP TABLE IF EXISTS `activity_double_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_double_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyGoodsIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_benefit`
--

DROP TABLE IF EXISTS `activity_dragon_boat_benefit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_benefit` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_celebration`
--

DROP TABLE IF EXISTS `activity_dragon_boat_celebration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_celebration` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `wolrdCollectTimes` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `beatYuriTotalTimes` int(11) NOT NULL DEFAULT '0',
  `guildDonateTimes` int(11) NOT NULL,
  `guildDonateTotalTimes` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_exchange`
--

DROP TABLE IF EXISTS `activity_dragon_boat_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `careItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_gift`
--

DROP TABLE IF EXISTS `activity_dragon_boat_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginAward` int(11) NOT NULL,
  `boatGifts` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_lucky_bag`
--

DROP TABLE IF EXISTS `activity_dragon_boat_lucky_bag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_lucky_bag` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `openCount` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dragon_boat_recharge`
--

DROP TABLE IF EXISTS `activity_dragon_boat_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dragon_boat_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `rechargeDays` int(11) NOT NULL,
  `lastRechargeTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItemsDay` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_collection`
--

DROP TABLE IF EXISTS `activity_dress_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_collection` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dressTypes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_collection_two`
--

DROP TABLE IF EXISTS `activity_dress_collection_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_collection_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dressTypes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_drawing_search`
--

DROP TABLE IF EXISTS `activity_dress_drawing_search`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_drawing_search` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `lastOperTime` bigint(20) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `wolrdCollectTimes` int(11) DEFAULT '0',
  `totalDropNum` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_energy_gather`
--

DROP TABLE IF EXISTS `activity_dress_energy_gather`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_energy_gather` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDays` int(11) NOT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_energy_gather_two`
--

DROP TABLE IF EXISTS `activity_dress_energy_gather_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_energy_gather_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDays` int(11) NOT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_fire_reignite`
--

DROP TABLE IF EXISTS `activity_dress_fire_reignite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_fire_reignite` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `recBoxNum` int(11) NOT NULL,
  `exchangeNum` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_fire_reignite_two`
--

DROP TABLE IF EXISTS `activity_dress_fire_reignite_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_fire_reignite_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `recBoxInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeNum` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_gunpowder_rise`
--

DROP TABLE IF EXISTS `activity_dress_gunpowder_rise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_gunpowder_rise` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_gunpowder_rise_two`
--

DROP TABLE IF EXISTS `activity_dress_gunpowder_rise_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_gunpowder_rise_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dress_treasure`
--

DROP TABLE IF EXISTS `activity_dress_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dress_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `awards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `randomId` int(11) NOT NULL DEFAULT '0',
  `awardScoreFrom` int(11) NOT NULL DEFAULT '0',
  `awardScoreTo` int(11) NOT NULL DEFAULT '0',
  `resetCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_dyzz_achieve`
--

DROP TABLE IF EXISTS `activity_dyzz_achieve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_dyzz_achieve` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_energies`
--

DROP TABLE IF EXISTS `activity_energies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_energies` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `dailyScore` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_energy_invest`
--

DROP TABLE IF EXISTS `activity_energy_invest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_energy_invest` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `daliyTask` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_equip_achieve`
--

DROP TABLE IF EXISTS `activity_equip_achieve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_equip_achieve` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_equip_black_market`
--

DROP TABLE IF EXISTS `activity_equip_black_market`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_equip_black_market` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `refines` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyPackageIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastBuyPackage` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_equip_carftsman`
--

DROP TABLE IF EXISTS `activity_equip_carftsman`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_equip_carftsman` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `attrBox` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `page` int(11) NOT NULL,
  `gachaTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_equip_tech`
--

DROP TABLE IF EXISTS `activity_equip_tech`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_equip_tech` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_evolution`
--

DROP TABLE IF EXISTS `activity_evolution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_evolution` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `taskItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `finishedExchange` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_exchange_decorate`
--

DROP TABLE IF EXISTS `activity_exchange_decorate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_exchange_decorate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `level` int(11) DEFAULT NULL,
  `exp` int(11) DEFAULT NULL,
  `levelReward` text COLLATE utf8mb4_unicode_ci,
  `achieveDayItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveDayRefreshTime` bigint(20) DEFAULT NULL,
  `achieveWeekItems` text COLLATE utf8mb4_unicode_ci,
  `achieveWeekRefreshTime` int(11) DEFAULT NULL,
  `levelOpenExchange` text COLLATE utf8mb4_unicode_ci,
  `decorateExchange` text COLLATE utf8mb4_unicode_ci,
  `loginDays` int(11) DEFAULT NULL,
  `loginRefreshTime` bigint(20) DEFAULT NULL,
  `weekNum` int(11) DEFAULT NULL,
  `weekBuyExpNum` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_exclusive_memory`
--

DROP TABLE IF EXISTS `activity_exclusive_memory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_exclusive_memory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `openState` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_festival`
--

DROP TABLE IF EXISTS `activity_festival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_festival` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDays` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_festival_two`
--

DROP TABLE IF EXISTS `activity_festival_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_festival_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDays` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_fighter_puzzle`
--

DROP TABLE IF EXISTS `activity_fighter_puzzle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_fighter_puzzle` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDay` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_fighter_puzzle_serveropen`
--

DROP TABLE IF EXISTS `activity_fighter_puzzle_serveropen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_fighter_puzzle_serveropen` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDay` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_fire_work`
--

DROP TABLE IF EXISTS `activity_fire_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_fire_work` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buffInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayFree` tinyint(1) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_first_recharge`
--

DROP TABLE IF EXISTS `activity_first_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_first_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `hasReceiveReward` int(11) NOT NULL DEFAULT '0',
  `hasExtrAward` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_first_recharge_three`
--

DROP TABLE IF EXISTS `activity_first_recharge_three`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_first_recharge_three` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `activeTime` bigint(20) NOT NULL DEFAULT '0',
  `payCount` int(11) NOT NULL DEFAULT '0',
  `rewardState` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_flight_plan`
--

DROP TABLE IF EXISTS `activity_flight_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_flight_plan` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cellId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `goodsExchange` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_fully_armed`
--

DROP TABLE IF EXISTS `activity_fully_armed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_fully_armed` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `searchId` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_ghost_secret`
--

DROP TABLE IF EXISTS `activity_ghost_secret`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_ghost_secret` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `drewInfo` text COLLATE utf8mb4_unicode_ci,
  `drewNum` int(11) DEFAULT NULL,
  `specAwardGot` tinyint(1) DEFAULT NULL,
  `resetNum` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gift_send`
--

DROP TABLE IF EXISTS `activity_gift_send`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gift_send` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gift_zero`
--

DROP TABLE IF EXISTS `activity_gift_zero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gift_zero` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `purchaseItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gift_zero_new`
--

DROP TABLE IF EXISTS `activity_gift_zero_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gift_zero_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `purchaseItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `freeTakenTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_global_sign`
--

DROP TABLE IF EXISTS `activity_global_sign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_global_sign` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `bullectChatControl` int(11) DEFAULT '0',
  `bulletChatTime` bigint(20) DEFAULT '0',
  `signTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gold_baby`
--

DROP TABLE IF EXISTS `activity_gold_baby`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gold_baby` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `findTimes` int(11) NOT NULL DEFAULT '0',
  `pools` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastLoginTime` bigint(20) NOT NULL DEFAULT '0',
  `buyTimes` int(11) NOT NULL DEFAULT '0',
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gold_baby_new`
--

DROP TABLE IF EXISTS `activity_gold_baby_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gold_baby_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `findTimes` int(11) NOT NULL DEFAULT '0',
  `pools` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastLoginTime` bigint(20) NOT NULL DEFAULT '0',
  `buyTimes` int(11) NOT NULL DEFAULT '0',
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_grateful_benefits`
--

DROP TABLE IF EXISTS `activity_grateful_benefits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_grateful_benefits` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `punchCount` int(11) NOT NULL DEFAULT '0',
  `lastPunchTime` bigint(20) NOT NULL DEFAULT '0',
  `shareCount` int(11) NOT NULL DEFAULT '0',
  `shareRefreshTime` bigint(20) NOT NULL DEFAULT '0',
  `inviteCDTime` bigint(20) NOT NULL DEFAULT '0',
  `wishMembers` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `award` tinyint(1) NOT NULL DEFAULT '0',
  `first` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_gratitude_gift`
--

DROP TABLE IF EXISTS `activity_gratitude_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_gratitude_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `rewardsGet` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_great_gift`
--

DROP TABLE IF EXISTS `activity_great_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_great_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyBag` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `recieveChest` text COLLATE utf8mb4_unicode_ci,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `outBuyBag` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `finishTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_greetings`
--

DROP TABLE IF EXISTS `activity_greetings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_greetings` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_group_buy`
--

DROP TABLE IF EXISTS `activity_group_buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_group_buy` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyScore` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `hotSellFreeGot` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `topDiscountRewardGot` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `topDiscountGifts` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_group_purchase`
--

DROP TABLE IF EXISTS `activity_group_purchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_group_purchase` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `loginDay` int(11) NOT NULL DEFAULT '0',
  `dailyReward` tinyint(1) NOT NULL DEFAULT '0',
  `scoreState` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_grow_up_boost`
--

DROP TABLE IF EXISTS `activity_grow_up_boost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_grow_up_boost` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `useConfig` int(11) NOT NULL DEFAULT '0',
  `scoreItemDetailString` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `scoreString` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItemsDay` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItemsScore` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_growfund`
--

DROP TABLE IF EXISTS `activity_growfund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_growfund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `isBuy` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_growfund_new`
--

DROP TABLE IF EXISTS `activity_growfund_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_growfund_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `fundBuyTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_guild_back`
--

DROP TABLE IF EXISTS `activity_guild_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_guild_dragon_attack`
--

DROP TABLE IF EXISTS `activity_guild_dragon_attack`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_guild_dragon_attack` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `damageMax` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_happy_gift`
--

DROP TABLE IF EXISTS `activity_happy_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_happy_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_heal_exchange`
--

DROP TABLE IF EXISTS `activity_heal_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_heal_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `active` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_heaven_blessing`
--

DROP TABLE IF EXISTS `activity_heaven_blessing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_heaven_blessing` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `groupId` int(11) NOT NULL DEFAULT '0',
  `payCount` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `choose` int(11) NOT NULL DEFAULT '0',
  `customState` int(11) NOT NULL DEFAULT '0',
  `activeState` tinyint(1) NOT NULL DEFAULT '0',
  `activeTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hell_fire`
--

DROP TABLE IF EXISTS `activity_hell_fire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hell_fire` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cycleStartTime` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `initBuildingBattlePoint` int(11) NOT NULL,
  `initTechBattlePoint` int(11) NOT NULL,
  `otherSumScore` int(11) NOT NULL,
  `targetIds` text CHARACTER SET utf8mb4 NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hell_fire_three`
--

DROP TABLE IF EXISTS `activity_hell_fire_three`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hell_fire_three` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cycleStartTime` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `initBuildingBattlePoint` int(11) NOT NULL,
  `initTechBattlePoint` int(11) NOT NULL,
  `otherSumScore` int(11) NOT NULL,
  `targetIds` text CHARACTER SET utf8mb4 NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hell_fire_two`
--

DROP TABLE IF EXISTS `activity_hell_fire_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hell_fire_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cycleStartTime` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `initBuildingBattlePoint` int(11) NOT NULL,
  `initTechBattlePoint` int(11) NOT NULL,
  `otherSumScore` int(11) NOT NULL,
  `targetIds` text CHARACTER SET utf8mb4 NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_achieve`
--

DROP TABLE IF EXISTS `activity_hero_achieve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_achieve` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_back`
--

DROP TABLE IF EXISTS `activity_hero_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_back` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_back_exchange`
--

DROP TABLE IF EXISTS `activity_hero_back_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_back_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_love`
--

DROP TABLE IF EXISTS `activity_hero_love`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_love` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `score` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastLoginTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_skin`
--

DROP TABLE IF EXISTS `activity_hero_skin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_skin` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `pool` int(11) NOT NULL,
  `itemStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `multiple` int(11) NOT NULL,
  `refreshTimes` int(11) NOT NULL,
  `hasFinally` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_theme`
--

DROP TABLE IF EXISTS `activity_hero_theme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_theme` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_trial`
--

DROP TABLE IF EXISTS `activity_hero_trial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_trial` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `mission` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastRefreshTime` bigint(20) NOT NULL,
  `acceptTimes` int(11) NOT NULL,
  `refreshTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hero_wish`
--

DROP TABLE IF EXISTS `activity_hero_wish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hero_wish` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `chooseId` int(11) NOT NULL DEFAULT '0',
  `addCount` int(11) NOT NULL DEFAULT '0',
  `achieveCount` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hidden_treasure`
--

DROP TABLE IF EXISTS `activity_hidden_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hidden_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `nineBoxStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `nextFree` bigint(20) NOT NULL DEFAULT '0',
  `refreshtimes` bigint(20) NOT NULL DEFAULT '0',
  `purchaseItemTimes` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lottoryCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_homeland_puzzle`
--

DROP TABLE IF EXISTS `activity_homeland_puzzle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_homeland_puzzle` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `drawCount` int(11) NOT NULL DEFAULT '0',
  `pCombine` int(11) NOT NULL DEFAULT '0',
  `pGrandPrize` int(11) NOT NULL DEFAULT '0',
  `pItem` int(11) NOT NULL DEFAULT '0',
  `collectedCombinationItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `grandPrizeWon` int(11) NOT NULL DEFAULT '0',
  `freeTimes` int(11) NOT NULL DEFAULT '0',
  `exchangeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `recordItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hongfu_gift`
--

DROP TABLE IF EXISTS `activity_hongfu_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hongfu_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `hongFuInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_honor_repay`
--

DROP TABLE IF EXISTS `activity_honor_repay`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_honor_repay` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiveReward` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyTimes` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_honour_hero_befell`
--

DROP TABLE IF EXISTS `activity_honour_hero_befell`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_honour_hero_befell` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `freeLotteryCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `oneLotteryCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tenLotteryCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_honour_hero_return`
--

DROP TABLE IF EXISTS `activity_honour_hero_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_honour_hero_return` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `oneLotteryCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tenLotteryCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lotteryPage` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_honour_mobilize`
--

DROP TABLE IF EXISTS `activity_honour_mobilize`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_honour_mobilize` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `chooseId` int(11) NOT NULL DEFAULT '0',
  `freeCount` int(11) NOT NULL DEFAULT '0',
  `lotteryCount` int(11) NOT NULL DEFAULT '0',
  `lotteryTotalCount` int(11) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_hot_blood_war`
--

DROP TABLE IF EXISTS `activity_hot_blood_war`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_hot_blood_war` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `cureArmyInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `cureFirstType` int(11) NOT NULL DEFAULT '0',
  `cureArmyId` int(11) NOT NULL DEFAULT '0',
  `cureArmyStartTime` bigint(20) NOT NULL DEFAULT '0',
  `cureArmySpeedTime` bigint(20) NOT NULL DEFAULT '0',
  `cureArmyCalTime` bigint(20) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `selfHurtInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `enemyKillInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `enemyKillScore` bigint(20) NOT NULL DEFAULT '0',
  `selfHurtScore` bigint(20) NOT NULL DEFAULT '0',
  `finishCheck` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_inherit`
--

DROP TABLE IF EXISTS `activity_inherit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_inherit` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `totalVipExp` int(11) NOT NULL,
  `totalGold` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_inherit_new`
--

DROP TABLE IF EXISTS `activity_inherit_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_inherit_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `totalVipExp` int(11) NOT NULL,
  `totalGold` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `sourcePlayerId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_invest`
--

DROP TABLE IF EXISTS `activity_invest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_invest` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `investItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_jigsaw_connect`
--

DROP TABLE IF EXISTS `activity_jigsaw_connect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_jigsaw_connect` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_jijia_skin`
--

DROP TABLE IF EXISTS `activity_jijia_skin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_jijia_skin` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `pool` int(11) NOT NULL DEFAULT '0',
  `itemStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `multiple` int(11) NOT NULL DEFAULT '0',
  `refreshTimes` int(11) NOT NULL DEFAULT '0',
  `hasFinally` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_joy_buy`
--

DROP TABLE IF EXISTS `activity_joy_buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_joy_buy` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exchangeRefreshNum` int(11) NOT NULL,
  `exchangeNextTime` bigint(20) NOT NULL,
  `exchangeList` text COLLATE utf8mb4_unicode_ci,
  `exchangeNumber` int(11) DEFAULT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginRefreshTime` bigint(20) DEFAULT NULL,
  `loginDays` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_jxjigsaw_connect`
--

DROP TABLE IF EXISTS `activity_jxjigsaw_connect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_jxjigsaw_connect` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_login_day`
--

DROP TABLE IF EXISTS `activity_login_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_login_day` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_login_day_two`
--

DROP TABLE IF EXISTS `activity_login_day_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_login_day_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_login_gift`
--

DROP TABLE IF EXISTS `activity_login_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_login_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyAdvanceTime` bigint(20) NOT NULL DEFAULT '0',
  `receivedCommDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receivedAdvanceDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `advanceEndTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_login_sign`
--

DROP TABLE IF EXISTS `activity_login_sign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_login_sign` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `lastTookTime` bigint(20) NOT NULL,
  `tookItemId` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_loginfund`
--

DROP TABLE IF EXISTS `activity_loginfund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_loginfund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `isBuy` int(11) NOT NULL DEFAULT '0',
  `isNew` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_loginfund_two`
--

DROP TABLE IF EXISTS `activity_loginfund_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_loginfund_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `facLv` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lottery_draw`
--

DROP TABLE IF EXISTS `activity_lottery_draw`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lottery_draw` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `lastFreeDrawTime` bigint(20) NOT NULL,
  `lastCellId` int(11) NOT NULL,
  `tenDrawTimes` int(11) NOT NULL,
  `totalTimes` int(11) NOT NULL,
  `ensureTimes` int(11) NOT NULL,
  `multi` tinyint(1) NOT NULL DEFAULT '0',
  `multiLucky` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lottery_ticket`
--

DROP TABLE IF EXISTS `activity_lottery_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lottery_ticket` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `buyMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lover_meet`
--

DROP TABLE IF EXISTS `activity_lover_meet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lover_meet` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `questionStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `endingStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_luck_get_gold`
--

DROP TABLE IF EXISTS `activity_luck_get_gold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_luck_get_gold` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveChoose` int(11) NOT NULL DEFAULT '0',
  `poolChoose` int(11) NOT NULL DEFAULT '0',
  `freeCount` int(11) NOT NULL DEFAULT '0',
  `dailyDrawCount` int(11) NOT NULL DEFAULT '0',
  `totalDrawCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lucky_box`
--

DROP TABLE IF EXISTS `activity_lucky_box`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lucky_box` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `cellMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `mustMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyNeedCount` int(11) NOT NULL DEFAULT '0',
  `randomCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lucky_discount`
--

DROP TABLE IF EXISTS `activity_lucky_discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lucky_discount` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `deadline` bigint(20) NOT NULL,
  `buyRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `poolId` int(11) NOT NULL,
  `freeTimes` int(11) NOT NULL,
  `drawTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_lucky_welfare`
--

DROP TABLE IF EXISTS `activity_lucky_welfare`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_lucky_welfare` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_machine_awake`
--

DROP TABLE IF EXISTS `activity_machine_awake`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_machine_awake` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `damage` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_machine_awake_two`
--

DROP TABLE IF EXISTS `activity_machine_awake_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_machine_awake_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `damage` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_machine_lab`
--

DROP TABLE IF EXISTS `activity_machine_lab`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_machine_lab` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `playerServer` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerExp` int(11) NOT NULL DEFAULT '0',
  `stormingPointTotal` int(11) NOT NULL DEFAULT '0',
  `buyGift` int(11) NOT NULL DEFAULT '0',
  `serverRewardLevel` int(11) NOT NULL DEFAULT '0',
  `playerRewardLevel` int(11) NOT NULL DEFAULT '0',
  `playerAdvRewardLevel` int(11) NOT NULL DEFAULT '0',
  `dropMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `careIgnore` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplementTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_machine_sell`
--

DROP TABLE IF EXISTS `activity_machine_sell`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_machine_sell` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `lotteryTimes` int(11) NOT NULL DEFAULT '0',
  `singleTimes` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_material_transport`
--

DROP TABLE IF EXISTS `activity_material_transport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_material_transport` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `truckNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `truckRobNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainRobNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `specialTrainNumber` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_medal_action`
--

DROP TABLE IF EXISTS `activity_medal_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_medal_action` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyNum` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_medal_fund`
--

DROP TABLE IF EXISTS `activity_medal_fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_medal_fund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `daliyTask` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_medal_fund_two`
--

DROP TABLE IF EXISTS `activity_medal_fund_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_medal_fund_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dailyTask` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_merge_competition`
--

DROP TABLE IF EXISTS `activity_merge_competition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_mid_autumn`
--

DROP TABLE IF EXISTS `activity_mid_autumn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_mid_autumn` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `exchangeNum` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyGiftNum` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_military_prepare`
--

DROP TABLE IF EXISTS `activity_military_prepare`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_military_prepare` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `advanced` int(11) NOT NULL DEFAULT '0',
  `advancedBox` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_monster2`
--

DROP TABLE IF EXISTS `activity_monster2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_monster2` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_new_first_recharge`
--

DROP TABLE IF EXISTS `activity_new_first_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_new_first_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `isActive` tinyint(1) NOT NULL DEFAULT '0',
  `activeTime` bigint(20) NOT NULL DEFAULT '0',
  `payCount` int(11) NOT NULL DEFAULT '0',
  `rewardState` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `popLevel` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_new_start`
--

DROP TABLE IF EXISTS `activity_new_start`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_new_start` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `isActive` tinyint(1) NOT NULL DEFAULT '0',
  `isBind` tinyint(1) NOT NULL DEFAULT '0',
  `playerLevel` int(11) NOT NULL DEFAULT '0',
  `vipLevel` int(11) NOT NULL DEFAULT '0',
  `baseLevel` int(11) NOT NULL DEFAULT '0',
  `heroCount` int(11) NOT NULL DEFAULT '0',
  `equipTechLevel` int(11) NOT NULL DEFAULT '0',
  `jijiaLevel` int(11) NOT NULL DEFAULT '0',
  `name` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `icon` int(11) NOT NULL DEFAULT '0',
  `pfIcon` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `oldPlayerId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `oldServerId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cfgInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `awardInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_newbie_train`
--

DROP TABLE IF EXISTS `activity_newbie_train`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_newbie_train` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dailyLoginTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_newly_experience`
--

DROP TABLE IF EXISTS `activity_newly_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_newly_experience` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_newyear_lottery`
--

DROP TABLE IF EXISTS `activity_newyear_lottery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_newyear_lottery` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dayTime` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `payGiftInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_newyear_treasure`
--

DROP TABLE IF EXISTS `activity_newyear_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_newyear_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_onermb_purchase`
--

DROP TABLE IF EXISTS `activity_onermb_purchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_onermb_purchase` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_order`
--

DROP TABLE IF EXISTS `activity_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_order` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `weekCycle` int(11) NOT NULL DEFAULT '0',
  `authorityId` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `expBuyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `orderItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `historyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_order_equip`
--

DROP TABLE IF EXISTS `activity_order_equip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_order_equip` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `weekCycle` int(11) NOT NULL DEFAULT '0',
  `authorityId` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `orderItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `historyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `weekNumber` int(11) DEFAULT '0',
  `weekTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_order_new`
--

DROP TABLE IF EXISTS `activity_order_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_order_new` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `authorityId` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL,
  `rewardInfo` text COLLATE utf8mb4_unicode_ci,
  `expBuyInfo` text COLLATE utf8mb4_unicode_ci,
  `orderItems` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_order_two`
--

DROP TABLE IF EXISTS `activity_order_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_order_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `weekCycle` int(11) NOT NULL DEFAULT '0',
  `authorityId` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `orderItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `historyItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `weekNumber` int(11) DEFAULT '0',
  `weekTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `rewardNormalLevel` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardAdvanceLevel` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_ordnance_fortress`
--

DROP TABLE IF EXISTS `activity_ordnance_fortress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_ordnance_fortress` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `stageId` int(11) NOT NULL DEFAULT '0',
  `rewardShow` int(11) NOT NULL DEFAULT '0',
  `bigRewardId` int(11) NOT NULL DEFAULT '0',
  `tickets` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigAwardTimes` int(11) NOT NULL DEFAULT '0',
  `openCount` int(11) NOT NULL DEFAULT '0',
  `rewardChoose` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigRewardCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_overlord_blessing`
--

DROP TABLE IF EXISTS `activity_overlord_blessing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_overlord_blessing` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `hasShare` tinyint(1) NOT NULL DEFAULT '0',
  `receiveShare` tinyint(1) NOT NULL DEFAULT '0',
  `hasBless` tinyint(1) NOT NULL DEFAULT '0',
  `receiveBless` tinyint(1) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_pandora_box`
--

DROP TABLE IF EXISTS `activity_pandora_box`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_pandora_box` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `freeCount` int(11) DEFAULT '0',
  `lotteryCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `storeInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int(11) DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_pdd`
--

DROP TABLE IF EXISTS `activity_pdd`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_pdd` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `failNum` int(11) NOT NULL DEFAULT '0',
  `isFirst` tinyint(1) NOT NULL DEFAULT '0',
  `shareTime` bigint(20) NOT NULL DEFAULT '0',
  `shareCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_pioneer_gift`
--

DROP TABLE IF EXISTS `activity_pioneer_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_pioneer_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `purchaseItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `accDay` int(11) NOT NULL,
  `latestPurchaseTime` bigint(20) NOT NULL,
  `freeTakenTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plan`
--

DROP TABLE IF EXISTS `activity_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plan` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `score` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_planet_explore`
--

DROP TABLE IF EXISTS `activity_planet_explore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_planet_explore` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `score` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `collectInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `exploreTimes` int(11) NOT NULL DEFAULT '0',
  `collectCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plant_fortress`
--

DROP TABLE IF EXISTS `activity_plant_fortress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plant_fortress` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `stageId` int(11) NOT NULL DEFAULT '0',
  `rewardShow` int(11) NOT NULL DEFAULT '0',
  `bigRewardId` int(11) NOT NULL DEFAULT '0',
  `tickets` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigAwardTimes` int(11) NOT NULL DEFAULT '0',
  `openCount` int(11) NOT NULL DEFAULT '0',
  `rewardChoose` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigRewardCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyCount` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plant_secret`
--

DROP TABLE IF EXISTS `activity_plant_secret`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plant_secret` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `openBoxCount` int(11) NOT NULL DEFAULT '0',
  `openBoxTimes` int(11) NOT NULL DEFAULT '0',
  `buyItemCount` int(11) NOT NULL DEFAULT '0',
  `openedCards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `secret` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `lastShareTimeWorld` bigint(20) NOT NULL DEFAULT '0',
  `lastShareTimeGuild` bigint(20) NOT NULL DEFAULT '0',
  `worldshare` int(11) NOT NULL DEFAULT '0',
  `allianceshare` int(11) NOT NULL DEFAULT '0',
  `daytime` int(11) NOT NULL DEFAULT '0',
  `dailyopenbox` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plant_soldier_factory`
--

DROP TABLE IF EXISTS `activity_plant_soldier_factory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plant_soldier_factory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `awardInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigAwardInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `drawCount` int(11) NOT NULL DEFAULT '0',
  `drawTotalCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plant_weapon`
--

DROP TABLE IF EXISTS `activity_plant_weapon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plant_weapon` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `turnCount` int(11) NOT NULL DEFAULT '0',
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `continueDraws` int(11) NOT NULL DEFAULT '0',
  `continueGiveups` int(11) NOT NULL DEFAULT '0',
  `cooldownTime` bigint(20) NOT NULL DEFAULT '0',
  `inspireProgress` int(11) NOT NULL DEFAULT '0',
  `consumeItemCount` int(11) NOT NULL DEFAULT '0',
  `awardItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `disCount` int(11) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `choosePlantWeapon` int(11) NOT NULL DEFAULT '0',
  `dailyRecieveTime` bigint(20) NOT NULL DEFAULT '0',
  `touchCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_plant_weapon_back`
--

DROP TABLE IF EXISTS `activity_plant_weapon_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_plant_weapon_back` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `drawTimes` int(11) NOT NULL DEFAULT '0',
  `freeTimes` int(11) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `buff` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_player`
--

DROP TABLE IF EXISTS `activity_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_player` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityId` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `newlyTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_player_comeback`
--

DROP TABLE IF EXISTS `activity_player_comeback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_player_comeback` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `init` tinyint(1) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `accountLogoutTime` bigint(20) NOT NULL DEFAULT '0',
  `rewardInfos` text COLLATE utf8mb4_unicode_ci,
  `achieveInfos` text COLLATE utf8mb4_unicode_ci,
  `buyInfos` text COLLATE utf8mb4_unicode_ci,
  `exchangeInfos` text COLLATE utf8mb4_unicode_ci,
  `loginDay` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_playerteam_back`
--

DROP TABLE IF EXISTS `activity_playerteam_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_playerteam_back` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `refreshTime` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `starNum` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `rewardInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `teamMemberInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_point_sprint`
--

DROP TABLE IF EXISTS `activity_point_sprint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_point_sprint` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `round` int(11) NOT NULL DEFAULT '0',
  `awardRound` int(11) NOT NULL DEFAULT '0',
  `scoreInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `awardedInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_power_collect`
--

DROP TABLE IF EXISTS `activity_power_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_power_collect` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectCnt` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_power_send`
--

DROP TABLE IF EXISTS `activity_power_send`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_power_send` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `sendCount` int(11) NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_power_up`
--

DROP TABLE IF EXISTS `activity_power_up`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_power_up` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_powerfund`
--

DROP TABLE IF EXISTS `activity_powerfund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_powerfund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `isBuy` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_preferential_surprise`
--

DROP TABLE IF EXISTS `activity_preferential_surprise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_preferential_surprise` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_present_rebate`
--

DROP TABLE IF EXISTS `activity_present_rebate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_present_rebate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_prestressing_loss`
--

DROP TABLE IF EXISTS `activity_prestressing_loss`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_prestressing_loss` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `openTime` bigint(20) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  `openTerm` int(11) NOT NULL DEFAULT '0',
  `coolTimeVal` bigint(20) NOT NULL DEFAULT '0',
  `vacancyTimeVal` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_quest_treasure`
--

DROP TABLE IF EXISTS `activity_quest_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_quest_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `gameInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `gameRefreshCount` int(11) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxScore` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_question_share`
--

DROP TABLE IF EXISTS `activity_question_share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_question_share` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shareAmount` int(11) NOT NULL DEFAULT '0',
  `dailyRewarded` int(11) NOT NULL,
  `rewards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayQuestion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayAnswer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayShare` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_radiation_war`
--

DROP TABLE IF EXISTS `activity_radiation_war`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_radiation_war` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `killNum` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_radiation_war_two`
--

DROP TABLE IF EXISTS `activity_radiation_war_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_radiation_war_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `killNum` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `guildAchieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recall_friend`
--

DROP TABLE IF EXISTS `activity_recall_friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recall_friend` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `recallNum` int(11) NOT NULL,
  `lastResetTime` bigint(20) DEFAULT NULL,
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `recallPlayer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recharge_fund`
--

DROP TABLE IF EXISTS `activity_recharge_fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recharge_fund` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `rechargeNum` int(11) NOT NULL,
  `investInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `diyReward` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardedInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recharge_gift`
--

DROP TABLE IF EXISTS `activity_recharge_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recharge_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `rechargeTotal` int(11) DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recharge_qixi`
--

DROP TABLE IF EXISTS `activity_recharge_qixi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recharge_qixi` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recharge_welfare`
--

DROP TABLE IF EXISTS `activity_recharge_welfare`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recharge_welfare` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci,
  `freeTimes` int(11) NOT NULL,
  `itemset` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `totalCoupon` int(11) NOT NULL,
  `receiveCoupon` int(11) NOT NULL,
  `dailyScore` int(11) NOT NULL,
  `isFreeRec` tinyint(1) NOT NULL,
  `lotteryTimes` int(11) NOT NULL,
  `dailyLotteryTimes` int(11) NOT NULL,
  `receiveDiamond` int(11) NOT NULL,
  `totalDiamond` int(11) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_recovery_exchange`
--

DROP TABLE IF EXISTS `activity_recovery_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_recovery_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `exchangeTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `redTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `redHighTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `recycleItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_red_envelope`
--

DROP TABLE IF EXISTS `activity_red_envelope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_red_envelope` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `recieveInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_red_package`
--

DROP TABLE IF EXISTS `activity_red_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_red_package` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `recieveInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `name_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_red_recharge`
--

DROP TABLE IF EXISTS `activity_red_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_red_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `loginDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rechargeItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_redblue_ticket`
--

DROP TABLE IF EXISTS `activity_redblue_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_redblue_ticket` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `ticketsA` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `ticketsB` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `poolRefreshTimes` int(11) NOT NULL DEFAULT '0',
  `started` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_redkoi`
--

DROP TABLE IF EXISTS `activity_redkoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_redkoi` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `turnId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `freeTimes` int(11) NOT NULL,
  `curChoseAward` int(11) NOT NULL,
  `wishPoints` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_resource_defense`
--

DROP TABLE IF EXISTS `activity_resource_defense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_resource_defense` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `stationInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `unlcokSuper` int(11) NOT NULL,
  `receivedRewardId` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stealTimes` int(11) NOT NULL,
  `beStealTimes` int(11) NOT NULL,
  `buyExpInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyExpRefreshTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `canStealTimes` int(11) NOT NULL DEFAULT '0',
  `stealTimesTick` bigint(20) NOT NULL,
  `stealTimesZeroTick` bigint(20) NOT NULL,
  `agentSkill` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `activeSkill` int(11) NOT NULL,
  `skillRefreshTimes` int(11) NOT NULL,
  `freeRefreshTimes` int(11) NOT NULL,
  `agentRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `greatRobotInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stealRobotInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_return_army_exchange`
--

DROP TABLE IF EXISTS `activity_return_army_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_return_army_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `exchangeInfos` text CHARACTER SET utf8mb4 NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_return_gift`
--

DROP TABLE IF EXISTS `activity_return_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_return_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `backCount` int(11) NOT NULL,
  `buyInfos` text CHARACTER SET utf8mb4 NOT NULL,
  `backType` int(11) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_return_puzzle`
--

DROP TABLE IF EXISTS `activity_return_puzzle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_return_puzzle` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `curDay` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveBoxItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveShareItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `nextTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `loginDay` int(11) NOT NULL DEFAULT '0',
  `lossDays` int(11) NOT NULL DEFAULT '0',
  `lossVip` int(11) NOT NULL DEFAULT '0',
  `backType` int(11) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_return_upgrade`
--

DROP TABLE IF EXISTS `activity_return_upgrade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_return_upgrade` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `overTime` bigint(20) NOT NULL DEFAULT '0',
  `backCount` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `goldBuyCount` int(11) NOT NULL DEFAULT '0',
  `upgradeInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `baseBeforLevel` int(11) NOT NULL DEFAULT '0',
  `baseAfterLevel` int(11) NOT NULL DEFAULT '0',
  `roleBeforLevel` int(11) NOT NULL DEFAULT '0',
  `roleAfterLevel` int(11) NOT NULL DEFAULT '0',
  `techPower` bigint(20) NOT NULL DEFAULT '0',
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_reward_order`
--

DROP TABLE IF EXISTS `activity_reward_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_reward_order` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `orderInfo` text COLLATE utf8mb4_unicode_ci,
  `refreshCnt` int(11) NOT NULL DEFAULT '0',
  `firstRefresh` tinyint(1) NOT NULL DEFAULT '0',
  `nextFreshTime` bigint(20) NOT NULL DEFAULT '0',
  `finishCnt` int(11) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_rose_gift`
--

DROP TABLE IF EXISTS `activity_rose_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_rose_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `selfNum` int(11) NOT NULL DEFAULT '0',
  `isPayToday` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `drawInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_roulette`
--

DROP TABLE IF EXISTS `activity_roulette`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_roulette` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `freeTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `itemset` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxReward` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int(11) DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_samurai_blackened`
--

DROP TABLE IF EXISTS `activity_samurai_blackened`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_samurai_blackened` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `initDay` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_scene_share`
--

DROP TABLE IF EXISTS `activity_scene_share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_scene_share` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `scene` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_sea_treasure`
--

DROP TABLE IF EXISTS `activity_sea_treasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_sea_treasure` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dayMark` int(11) NOT NULL DEFAULT '0',
  `findTimes` int(11) NOT NULL DEFAULT '0',
  `toolBuyTimes` int(11) NOT NULL DEFAULT '0',
  `receiveTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxInfos` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiveRewards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiveAdvRewards` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT NULL,
  `invalid` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_season`
--

DROP TABLE IF EXISTS `activity_season`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_season` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `orderItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `orderLevel` int(11) NOT NULL DEFAULT '0',
  `orderExp` int(11) NOT NULL DEFAULT '0',
  `authorityId` int(11) NOT NULL DEFAULT '0',
  `orderRewardLevel` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `orderRewardAdLevel` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchange` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `clientLevel` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_season_guild_grade`
--

DROP TABLE IF EXISTS `activity_season_guild_grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_season_guild_grade` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `guildId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `isReward` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_season_puzzle`
--

DROP TABLE IF EXISTS `activity_season_puzzle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_season_puzzle` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `dayTime` bigint(20) NOT NULL DEFAULT '0',
  `itemSendCount` int(11) NOT NULL DEFAULT '0',
  `itemGetCount` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `callHelpInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemSetInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_share_prosperity`
--

DROP TABLE IF EXISTS `activity_share_prosperity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_share_prosperity` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `rebateCount` int(11) NOT NULL DEFAULT '0',
  `bindOldPlayer` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_shooting_practice`
--

DROP TABLE IF EXISTS `activity_shooting_practice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_shooting_practice` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyCount` int(11) NOT NULL DEFAULT '0',
  `buyCountDaily` int(11) NOT NULL DEFAULT '0',
  `freeCount` int(11) NOT NULL DEFAULT '0',
  `scoreMax` int(11) NOT NULL DEFAULT '0',
  `scoreTotal` int(11) NOT NULL DEFAULT '0',
  `lastOverTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItemsDay` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_skin_plan`
--

DROP TABLE IF EXISTS `activity_skin_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_skin_plan` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `score` int(11) NOT NULL DEFAULT '0',
  `recvTop` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_snowball`
--

DROP TABLE IF EXISTS `activity_snowball`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_snowball` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `turnId` int(11) NOT NULL,
  `kickScore` int(11) NOT NULL,
  `continueKickScore` int(11) NOT NULL,
  `assisScore` int(11) NOT NULL,
  `goalScore` int(11) NOT NULL,
  `goalAssisScore` int(11) NOT NULL,
  `receive` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_soldier_exchange`
--

DROP TABLE IF EXISTS `activity_soldier_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_soldier_exchange` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeType` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `histor` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_song_hua`
--

DROP TABLE IF EXISTS `activity_song_hua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_song_hua` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `songHua` int(11) NOT NULL,
  `shouHua` int(11) NOT NULL,
  `laPiao` bigint(20) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_space_guard`
--

DROP TABLE IF EXISTS `activity_space_guard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_space_guard` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `taskItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_spread`
--

DROP TABLE IF EXISTS `activity_spread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_spread` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shopItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `hiddenAchieveIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `friends` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewardedTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `canRewardTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayReward` int(10) unsigned NOT NULL,
  `isBindCode` int(11) NOT NULL,
  `bindCode` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_star_light_sign`
--

DROP TABLE IF EXISTS `activity_star_light_sign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_star_light_sign` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `signItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `signDays` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int(11) NOT NULL DEFAULT '0',
  `scoreBox` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `isMultiple` tinyint(1) NOT NULL DEFAULT '0',
  `isAdMultiple` tinyint(1) NOT NULL DEFAULT '0',
  `signRedeemCnt` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_start_invest`
--

DROP TABLE IF EXISTS `activity_start_invest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_start_invest` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `freeInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `daliyTask` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `cells` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rechargeCount` int(11) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `speedItemBuyCount` int(11) NOT NULL DEFAULT '0',
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_strongest_guild`
--

DROP TABLE IF EXISTS `activity_strongest_guild`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_strongest_guild` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `stageId` int(11) NOT NULL,
  `targetInfo` text COLLATE utf8mb4_unicode_ci,
  `score` bigint(20) NOT NULL,
  `killScore` bigint(20) NOT NULL,
  `hurtScore` bigint(20) NOT NULL,
  `buildBattlePoint` bigint(20) NOT NULL,
  `techBattlePoint` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_strongest_leader`
--

DROP TABLE IF EXISTS `activity_strongest_leader`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_strongest_leader` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `activityStage` int(11) DEFAULT NULL,
  `stageId` int(11) NOT NULL,
  `score` bigint(20) DEFAULT NULL,
  `initFightPoint` bigint(20) NOT NULL,
  `buildBattlePoint` bigint(20) NOT NULL,
  `techBattlePoint` bigint(20) NOT NULL,
  `targetIds` text COLLATE utf8mb4_unicode_ci,
  `achieveTargets` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `hurtScore` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_strongpoint`
--

DROP TABLE IF EXISTS `activity_strongpoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_strongpoint` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_submarine_war`
--

DROP TABLE IF EXISTS `activity_submarine_war`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_submarine_war` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `gameInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `gameScore` int(11) NOT NULL DEFAULT '0',
  `gameCount` int(11) NOT NULL DEFAULT '0',
  `buyGameCount` int(11) NOT NULL DEFAULT '0',
  `gameLevelMax` int(11) NOT NULL DEFAULT '0',
  `gameScoreMax` int(11) NOT NULL DEFAULT '0',
  `gameScoreMaxTime` bigint(20) NOT NULL DEFAULT '0',
  `skillItemBuyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `initTime` bigint(20) NOT NULL DEFAULT '0',
  `loginDays` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `tips` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `orderInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_super_discount`
--

DROP TABLE IF EXISTS `activity_super_discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_super_discount` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `poolId` int(11) NOT NULL,
  `deadline` bigint(20) NOT NULL,
  `freeTimes` int(11) NOT NULL,
  `drawTimes` int(11) NOT NULL,
  `drawAllTimes` int(11) NOT NULL,
  `buyRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_super_gold`
--

DROP TABLE IF EXISTS `activity_super_gold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_super_gold` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_super_gold_two`
--

DROP TABLE IF EXISTS `activity_super_gold_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_super_gold_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_supersoldier_invest`
--

DROP TABLE IF EXISTS `activity_supersoldier_invest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_supersoldier_invest` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `daliyTask` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_supply_crate`
--

DROP TABLE IF EXISTS `activity_supply_crate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_supply_crate` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `round` int(11) NOT NULL DEFAULT '0',
  `isCanOPen` tinyint(1) NOT NULL DEFAULT '0',
  `isCanNext` tinyint(1) NOT NULL DEFAULT '0',
  `mult` int(11) NOT NULL DEFAULT '0',
  `crateItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `boxProg` int(11) NOT NULL DEFAULT '0',
  `customIndex` int(11) NOT NULL DEFAULT '0',
  `openItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `isCanDouble` tinyint(1) NOT NULL DEFAULT '0',
  `guildBoxProg` int(11) NOT NULL DEFAULT '0',
  `boxCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_supply_station`
--

DROP TABLE IF EXISTS `activity_supply_station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_supply_station` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `buyInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_supply_station_two`
--

DROP TABLE IF EXISTS `activity_supply_station_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_supply_station_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `playerPoint` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_tiberium_guess`
--

DROP TABLE IF EXISTS `activity_tiberium_guess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_tiberium_guess` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_time_limit_buy`
--

DROP TABLE IF EXISTS `activity_time_limit_buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_time_limit_buy` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `buyStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `closeRemind` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `player_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_time_limit_drop`
--

DROP TABLE IF EXISTS `activity_time_limit_drop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_time_limit_drop` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `collectRemainTime` int(11) NOT NULL,
  `wolrdCollectRemainTime` int(11) NOT NULL,
  `beatYuriTimes` int(11) NOT NULL,
  `wishTimes` int(11) NOT NULL,
  `wolrdCollectTimes` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_time_limit_login`
--

DROP TABLE IF EXISTS `activity_time_limit_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_time_limit_login` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginData` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_travel_shop_assist`
--

DROP TABLE IF EXISTS `activity_travel_shop_assist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_travel_shop_assist` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_treasure_cavalry`
--

DROP TABLE IF EXISTS `activity_treasure_cavalry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_treasure_cavalry` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `pool` int(11) NOT NULL,
  `itemStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `multiple` int(11) NOT NULL,
  `refreshTimes` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_treasury`
--

DROP TABLE IF EXISTS `activity_treasury`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_treasury` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `storageInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receivedInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `costInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_playerId` (`playerId`) USING BTREE,
  KEY `playerid_idx` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_virtual_laboratory`
--

DROP TABLE IF EXISTS `activity_virtual_laboratory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_virtual_laboratory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `cardInfo` text COLLATE utf8mb4_unicode_ci,
  `openCardInfo` text COLLATE utf8mb4_unicode_ci,
  `achieveItems` text COLLATE utf8mb4_unicode_ci,
  `resetNum` int(11) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_war_flag_two`
--

DROP TABLE IF EXISTS `activity_war_flag_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_war_flag_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `playerPoint` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `exchangeMsg` text COLLATE utf8mb4_unicode_ci,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_warzone_weal`
--

DROP TABLE IF EXISTS `activity_warzone_weal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_warzone_weal` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `loginDays` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_yuri_achieve_two`
--

DROP TABLE IF EXISTS `activity_yuri_achieve_two`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_yuri_achieve_two` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agency`
--

DROP TABLE IF EXISTS `agency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agency` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `agencyEventStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `agencyEventPoolStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemEventGen` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `exp` int(11) NOT NULL DEFAULT '0',
  `currLevel` int(11) NOT NULL DEFAULT '0',
  `box` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `finishCount` int(11) NOT NULL DEFAULT '0',
  `hasKilled` int(11) NOT NULL DEFAULT '0',
  `killCount` int(11) NOT NULL DEFAULT '0',
  `nextRefreshTime` bigint(20) NOT NULL DEFAULT '0',
  `playerPos` int(11) NOT NULL DEFAULT '0',
  `boxExtLevel` int(11) NOT NULL DEFAULT '0',
  `specialId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `finishSpecialCount` int(11) DEFAULT '0',
  `finishSpecialDay` int(11) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `idx_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `armour`
--

DROP TABLE IF EXISTS `armour`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `armour` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `armourId` int(11) NOT NULL,
  `quality` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `suit` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `extraAttr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `skillAttr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `locked` tinyint(1) DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `endTime` bigint(20) NOT NULL DEFAULT '0',
  `isSuper` tinyint(1) DEFAULT '0',
  `star` int(11) DEFAULT '0',
  `starAttr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `starAttrConsume` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantum` int(11) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE,
  KEY `issuper_idx` (`isSuper`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `army`
--

DROP TABLE IF EXISTS `army`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `army` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `armyId` int(11) NOT NULL DEFAULT '0',
  `free` int(11) NOT NULL DEFAULT '0',
  `march` int(11) NOT NULL DEFAULT '0',
  `trainCount` int(11) NOT NULL DEFAULT '0',
  `trainFinishCount` int(11) NOT NULL DEFAULT '0',
  `woundedCount` int(11) NOT NULL DEFAULT '0',
  `cureCount` int(11) NOT NULL DEFAULT '0',
  `cureFinishCount` int(11) NOT NULL DEFAULT '0',
  `taralabsCount` int(11) NOT NULL DEFAULT '0',
  `advancePower` decimal(20,6) DEFAULT '0.000000',
  `trainLatest` tinyint(1) DEFAULT '0',
  `lastTrainTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `nationalHospitalDeadCount` int(11) NOT NULL DEFAULT '0',
  `nationalHospitalRecoveredCount` int(11) NOT NULL DEFAULT '0',
  `tszzDeadCount` int(11) NOT NULL DEFAULT '0',
  `tszzRecoveredCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `building`
--

DROP TABLE IF EXISTS `building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `building` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `buildingCfgId` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `status` int(11) NOT NULL DEFAULT '0',
  `hp` int(11) NOT NULL DEFAULT '0',
  `resUpdateTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `buildIndex` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '1',
  `lastResCollectTime` bigint(20) NOT NULL DEFAULT '0',
  `lastUpgradeTime` bigint(20) NOT NULL DEFAULT '0',
  `rescueCd` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE,
  KEY `type_index` (`type`) USING BTREE,
  KEY `buildingCfgId_idx` (`buildingCfgId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `college_info`
--

DROP TABLE IF EXISTS `college_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `college_info` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `coachId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `collegeName` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expTotal` int(11) DEFAULT '0',
  `level` int(11) DEFAULT '0',
  `exp` int(11) DEFAULT '0',
  `vitality` decimal(20,6) DEFAULT '0.000000',
  `joinFree` int(11) DEFAULT '0',
  `reNameCount` int(11) DEFAULT '0',
  `statisticsData` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_playerId` (`coachId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `college_member`
--

DROP TABLE IF EXISTS `college_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `college_member` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collegeId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `auth` int(11) NOT NULL,
  `crossResetTime` bigint(20) NOT NULL DEFAULT '0',
  `quitTime` bigint(20) NOT NULL DEFAULT '0',
  `joinTime` bigint(20) NOT NULL DEFAULT '0',
  `lastNotifyedTime` bigint(20) NOT NULL DEFAULT '0',
  `todayOnlineTime` bigint(20) NOT NULL DEFAULT '0',
  `onlineTookInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `scoreInfo` text COLLATE utf8mb4_unicode_ci,
  `shopInfo` text COLLATE utf8mb4_unicode_ci,
  `vitInfo` text COLLATE utf8mb4_unicode_ci,
  `missionInfo` text COLLATE utf8mb4_unicode_ci,
  `giftInfo` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `commander`
--

DROP TABLE IF EXISTS `commander`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commander` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `equipInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `starExplore` text COLLATE utf8mb4_unicode_ci,
  `starExploreCollect` text COLLATE utf8mb4_unicode_ci,
  `soulResetCd` bigint(20) DEFAULT '0',
  `superSoldierSkin` text COLLATE utf8mb4_unicode_ci,
  `shopData` text COLLATE utf8mb4_unicode_ci,
  `getDressTime` bigint(20) DEFAULT '0',
  `getDressCount` int(11) DEFAULT '0',
  `fgylData` text COLLATE utf8mb4_unicode_ci,
  `mtpremarch` int(11) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cross_tech`
--

DROP TABLE IF EXISTS `cross_tech`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cross_tech` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `techId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `researching` tinyint(1) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_data`
--

DROP TABLE IF EXISTS `custom_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_data` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` int(11) DEFAULT '0',
  `arg` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_data`
--

DROP TABLE IF EXISTS `daily_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `daily_data` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dailyFriendBoxTimes` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `travelShopRefreshTimes` int(11) NOT NULL DEFAULT '0',
  `guildPushTimes` int(11) NOT NULL DEFAULT '0',
  `deadGuildRefuseRecommendCnt` int(11) NOT NULL DEFAULT '0',
  `lastPushTime` bigint(20) NOT NULL DEFAULT '0',
  `travelGiftBuyTimes` int(11) NOT NULL DEFAULT '0',
  `vipTravelGiftBuyTimes` int(11) NOT NULL DEFAULT '0',
  `isMilitaryRankRecieve` tinyint(1) NOT NULL,
  `attackFoggyWinTimes` int(11) NOT NULL DEFAULT '0',
  `crRewardTimes` int(11) NOT NULL DEFAULT '0',
  `crHighestScore` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `resCollDropTimes` int(11) NOT NULL DEFAULT '0',
  `travelShopInfo` text CHARACTER SET utf8mb4,
  `guardGift` text COLLATE utf8mb4_unicode_ci,
  `ghostBox` int(11) NOT NULL DEFAULT '0',
  `armourStarAttrTimes` int(11) DEFAULT '0',
  `nationMissionDayBuyTimes` int(11) DEFAULT '0',
  `nationShipAssist` int(11) DEFAULT '0',
  `nationTechHelp` int(11) DEFAULT '0',
  `nationTechNotice` int(11) DEFAULT '0',
  `nationSkillDropTimes` int(11) DEFAULT '0',
  `joinAtkFoggyWinTimes` int(11) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dress`
--

DROP TABLE IF EXISTS `dress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dress` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dressInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `equip`
--

DROP TABLE IF EXISTS `equip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `equip` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cfgId` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `isNew` tinyint(1) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `equip_research`
--

DROP TABLE IF EXISTS `equip_research`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `equip_research` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `researchId` int(11) NOT NULL,
  `researchLevel` int(11) NOT NULL,
  `receiveBox` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gacha`
--

DROP TABLE IF EXISTS `gacha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gacha` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dayOfYear` int(11) NOT NULL,
  `count` int(11) NOT NULL,
  `gachaType` int(11) NOT NULL,
  `freeTimesUsed` int(11) NOT NULL,
  `firstGachaUsed` int(11) NOT NULL DEFAULT '0',
  `nextFree` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `dayCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gm_recharge`
--

DROP TABLE IF EXISTS `gm_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gm_recharge` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gmUser` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `goodsId` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rechargeGold` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_big_gift`
--

DROP TABLE IF EXISTS `guild_big_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_big_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bigGiftLevelExp` int(11) NOT NULL,
  `bigGiftId` int(11) NOT NULL,
  `bigGiftExp` int(11) NOT NULL,
  `giftSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `guildId_Index` (`guildId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_building`
--

DROP TABLE IF EXISTS `guild_building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_building` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `buildType` int(11) NOT NULL,
  `buildingId` int(11) NOT NULL,
  `buildingStat` int(11) NOT NULL,
  `pos` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '0,0',
  `buildTime` bigint(20) DEFAULT '0',
  `buildLife` decimal(20,6) DEFAULT '0.000000',
  `level` int(11) DEFAULT '1',
  `buildParam` text COLLATE utf8mb4_unicode_ci,
  `lastTakeBackTime` bigint(20) DEFAULT '0',
  `lastTickTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_counterattack`
--

DROP TABLE IF EXISTS `guild_counterattack`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_counterattack` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `atkerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lostPower` int(11) NOT NULL,
  `counterPower` int(11) NOT NULL,
  `attackerPointId` int(11) NOT NULL,
  `playerBountySer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `wipeoutSer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `rewards` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bitBackRewards` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `overTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `guildId_index` (`guildId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_fgyl`
--

DROP TABLE IF EXISTS `guild_fgyl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_fgyl` (
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `passLevel` int(11) NOT NULL DEFAULT '0',
  `useTime` int(11) NOT NULL DEFAULT '0',
  `passTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`guildId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_hospice`
--

DROP TABLE IF EXISTS `guild_hospice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_hospice` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attackerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `maxPower` bigint(20) NOT NULL DEFAULT '0',
  `lostPower` bigint(20) NOT NULL DEFAULT '0',
  `state` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `awards` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `helpQueue` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `helpers` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `matchStartTime` bigint(20) NOT NULL,
  `matchEndTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `overwhelming` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_info`
--

DROP TABLE IF EXISTS `guild_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_info` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `name` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `tag` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `flagId` int(11) NOT NULL DEFAULT '0',
  `langId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `level` int(11) DEFAULT '0',
  `leaderId` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `leaderName` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `leaderOfflineTime` bigint(20) DEFAULT '0',
  `coleaderId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coleaderName` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `isNeedPermition` tinyint(1) NOT NULL DEFAULT '0',
  `needBuildingLevel` int(11) DEFAULT '0',
  `needPower` int(11) DEFAULT '0',
  `needCommanderLevel` int(11) DEFAULT '0',
  `announcement` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notice` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `l1Name` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `l2Name` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `l3Name` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `l4Name` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `l5Name` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `needLang` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `score` bigint(20) DEFAULT '0',
  `lastDonateCheckTime` bigint(20) DEFAULT '0',
  `clearResNum` int(11) DEFAULT '0',
  `hasChangeTag` tinyint(1) NOT NULL DEFAULT '0',
  `guildBoundId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `authInfo` tinytext COLLATE utf8mb4_unicode_ci,
  `taskRefreshTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `chatRoomModel` int(11) DEFAULT '0',
  `xzqTickets` int(11) DEFAULT '0',
  `spaceMechaInfo` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `formationInfo` tinytext COLLATE utf8mb4_unicode_ci,
  `rewardFlag` tinytext COLLATE utf8mb4_unicode_ci,
  `leaderPlatform` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_manor`
--

DROP TABLE IF EXISTS `guild_manor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_manor` (
  `manorId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `manorIndex` int(11) NOT NULL,
  `manorName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `manorState` int(11) NOT NULL DEFAULT '0',
  `pos` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '0,0',
  `level` int(11) DEFAULT '1',
  `buildingLife` decimal(20,6) DEFAULT '0.000000',
  `completeTime` bigint(20) DEFAULT '0',
  `lastTakeBackTime` bigint(20) DEFAULT '0',
  `placeTime` bigint(20) NOT NULL DEFAULT '0',
  `lastTickTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`manorId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_member`
--

DROP TABLE IF EXISTS `guild_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_member` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `authority` int(11) NOT NULL DEFAULT '0',
  `officeId` int(11) NOT NULL DEFAULT '0',
  `playerName` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `power` bigint(20) DEFAULT '0',
  `killCount` bigint(20) DEFAULT '0',
  `quitGuildTime` bigint(20) NOT NULL DEFAULT '0',
  `joinGuildTime` bigint(20) NOT NULL DEFAULT '0',
  `normalDonateTimes` int(11) NOT NULL DEFAULT '0',
  `crystalDonateTimes` int(11) NOT NULL DEFAULT '0',
  `donateResetTimes` int(11) NOT NULL DEFAULT '0',
  `nextDonateAddTime` bigint(20) NOT NULL DEFAULT '0',
  `donateDayOfYear` int(11) NOT NULL DEFAULT '0',
  `lastRefrashBigGift` bigint(20) NOT NULL DEFAULT '0',
  `joinGuildTimes` int(11) NOT NULL DEFAULT '0',
  `manorUnlockTimes` int(11) NOT NULL DEFAULT '0',
  `logoutTime` bigint(20) NOT NULL DEFAULT '0',
  `rewaredTaskIds` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `taskResetTime` bigint(20) NOT NULL DEFAULT '0',
  `signTimes` int(11) NOT NULL DEFAULT '0',
  `lastSingTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `noArmyPower` bigint(20) DEFAULT '0',
  `dragonAwardTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_science`
--

DROP TABLE IF EXISTS `guild_science`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_science` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `scienceId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `star` int(11) NOT NULL DEFAULT '0',
  `donate` int(11) NOT NULL DEFAULT '0',
  `recommend` tinyint(1) DEFAULT '0',
  `finishTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `openLimitTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_smail_gift`
--

DROP TABLE IF EXISTS `guild_smail_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_smail_gift` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemId` int(11) NOT NULL,
  `awardGet` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state` int(11) NOT NULL,
  `giftCreateTime` bigint(20) NOT NULL,
  `giftOverTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hero`
--

DROP TABLE IF EXISTS `hero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hero` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heroId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `star` int(11) NOT NULL,
  `step` int(11) NOT NULL,
  `office` int(11) NOT NULL,
  `cityDefense` int(11) NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `skillSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `passiveSkillSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `attrSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `equipSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `talentOpen` int(11) NOT NULL DEFAULT '0',
  `talentSerialized` text COLLATE utf8mb4_unicode_ci,
  `skinSerialized` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `shareCount` int(11) DEFAULT '0',
  `skin` int(11) DEFAULT '0',
  `soulSerialized` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hero_archives`
--

DROP TABLE IF EXISTS `hero_archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hero_archives` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `archives` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemId` int(11) NOT NULL DEFAULT '0',
  `itemCount` int(11) NOT NULL DEFAULT '0',
  `isNew` tinyint(1) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_content`
--

DROP TABLE IF EXISTS `item_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_content` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longblob NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `laboratory`
--

DROP TABLE IF EXISTS `laboratory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `laboratory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pageIndex` int(11) NOT NULL,
  `powerCoreStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `powerBlockStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `pageUnlock` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifetime_card`
--

DROP TABLE IF EXISTS `lifetime_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifetime_card` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `commonUnlockTime` bigint(20) NOT NULL DEFAULT '0',
  `advancedEndTime` bigint(20) NOT NULL DEFAULT '0',
  `weekAwardTime` bigint(20) NOT NULL DEFAULT '0',
  `monthAwardTime` bigint(20) NOT NULL DEFAULT '0',
  `freeEndTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `ready` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manhattan`
--

DROP TABLE IF EXISTS `manhattan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `manhattan` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `swId` int(11) NOT NULL DEFAULT '0',
  `stage` int(11) DEFAULT '0',
  `posLevel` text COLLATE utf8mb4_unicode_ci,
  `deployed` int(11) DEFAULT '0',
  `cityShow` int(11) DEFAULT '0',
  `base` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mecha_core`
--

DROP TABLE IF EXISTS `mecha_core`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mecha_core` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `rankLevel` int(11) DEFAULT '0',
  `techInfo` text COLLATE utf8mb4_unicode_ci,
  `slotInfo` text COLLATE utf8mb4_unicode_ci,
  `suitInfo` text COLLATE utf8mb4_unicode_ci,
  `suitCount` int(11) DEFAULT '0',
  `workSuit` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `unlockedCityShow` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mecha_core_module`
--

DROP TABLE IF EXISTS `mecha_core_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mecha_core_module` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cfgId` int(11) NOT NULL DEFAULT '0',
  `quality` int(11) NOT NULL DEFAULT '0',
  `randomAttr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `locked` tinyint(1) DEFAULT '0',
  `loadSuitInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medal_factory`
--

DROP TABLE IF EXISTS `medal_factory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medal_factory` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `exp` int(11) NOT NULL,
  `collectStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stealStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stealTodayStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `canSteal` int(11) NOT NULL,
  `enemyStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshStr` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dailyReward` int(11) NOT NULL,
  `dailyRefresh` int(11) NOT NULL,
  `refreshCool` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` int(11) NOT NULL,
  `lastRefreshDay` int(11) NOT NULL,
  `leyuzhuren` int(11) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mission`
--

DROP TABLE IF EXISTS `mission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mission` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cfgId` int(11) NOT NULL DEFAULT '0',
  `num` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `unfinishPreMission` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `money_reissue`
--

DROP TABLE IF EXISTS `money_reissue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `money_reissue` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `count` int(11) NOT NULL,
  `source` int(11) NOT NULL,
  `reissueParam` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `monster`
--

DROP TABLE IF EXISTS `monster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `monster` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `maxLevel` int(11) NOT NULL DEFAULT '0',
  `currentLevelCount` int(11) NOT NULL DEFAULT '0',
  `newMonsterKileLvl` int(11) NOT NULL DEFAULT '0',
  `attackNewMonsterTimes` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `bosskillInfo` text COLLATE utf8mb4_unicode_ci,
  `bosskillRefreshDay` int(11) DEFAULT '0',
  `dropLimitInfo` text COLLATE utf8mb4_unicode_ci,
  `dropLimitRefreshDay` int(11) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nation_build_quest`
--

DROP TABLE IF EXISTS `nation_build_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation_build_quest` (
  `playerId` varchar(64) NOT NULL DEFAULT '',
  `nationQuestType` int(11) DEFAULT NULL,
  `refreshCount` int(11) DEFAULT NULL,
  `questTimes` int(11) DEFAULT NULL,
  `questInfo` longtext,
  `resetTime` bigint(20) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT NULL,
  `invalid` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nation_construction`
--

DROP TABLE IF EXISTS `nation_construction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation_construction` (
  `buildingId` int(11) NOT NULL,
  `level` int(11) DEFAULT NULL,
  `buildingStatus` int(11) DEFAULT NULL,
  `buildVal` int(11) DEFAULT NULL,
  `totalVal` int(11) DEFAULT NULL,
  `buildTime` bigint(20) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT NULL,
  `invalid` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`buildingId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nation_military`
--

DROP TABLE IF EXISTS `nation_military`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation_military` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `nationMilitaryExp` int(11) DEFAULT '0',
  `nationMilitaryResetTerm` int(11) DEFAULT '0',
  `nationMilitarLlevel` int(11) DEFAULT '0',
  `crossTermId` int(11) DEFAULT '0',
  `nationMilitaryBattleExp` int(11) DEFAULT '0',
  `nationMilitaryRewardDay` int(11) DEFAULT '0',
  `nationMilitaryReward` int(11) DEFAULT '0',
  `nationMilitaryRankTerm` int(11) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nation_mission`
--

DROP TABLE IF EXISTS `nation_mission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation_mission` (
  `playerId` varchar(64) NOT NULL DEFAULT '',
  `missionStr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` int(11) NOT NULL DEFAULT '0',
  `remainTimes` int(11) NOT NULL DEFAULT '0',
  `timeMark` bigint(20) NOT NULL DEFAULT '0',
  `constructionLevelMark` int(11) NOT NULL DEFAULT '0',
  `weekMark` int(11) NOT NULL DEFAULT '0',
  `tech` int(11) NOT NULL DEFAULT '0',
  `giveupTime` bigint(20) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT NULL,
  `invalid` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nation_ship_component`
--

DROP TABLE IF EXISTS `nation_ship_component`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation_ship_component` (
  `componentId` int(11) NOT NULL,
  `level` int(11) DEFAULT NULL,
  `upEndTime` bigint(20) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT NULL,
  `invalid` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`componentId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `obelisk`
--

DROP TABLE IF EXISTS `obelisk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `obelisk` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cfgId` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `contribution` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `officer`
--

DROP TABLE IF EXISTS `officer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `officer` (
  `officerId` int(11) NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `endTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`officerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pay_state`
--

DROP TABLE IF EXISTS `pay_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pay_state` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rechargeGold` int(11) NOT NULL DEFAULT '0',
  `rechargeAwardId` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lastRechargeTime` bigint(20) NOT NULL DEFAULT '0',
  `rechargeInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plant_factory`
--

DROP TABLE IF EXISTS `plant_factory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plant_factory` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `plantCfgId` int(11) NOT NULL,
  `factoryType` int(11) NOT NULL,
  `lastResStoreTime` bigint(20) NOT NULL,
  `resStore` decimal(20,6) DEFAULT '0.000000',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plant_science`
--

DROP TABLE IF EXISTS `plant_science`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plant_science` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `plantScienceSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plant_soldier_advance`
--

DROP TABLE IF EXISTS `plant_soldier_advance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plant_soldier_advance` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `advanceArmy` int(11) NOT NULL DEFAULT '0',
  `collectArmy` int(11) NOT NULL DEFAULT '0',
  `lastResStoreTime` bigint(20) NOT NULL DEFAULT '0',
  `resStore` decimal(20,6) DEFAULT '0.000000',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `advanceTotal` int(11) DEFAULT '0',
  `resTotal` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `advanceStart` bigint(20) NOT NULL DEFAULT '0',
  `advanceEnd` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plant_soldier_school`
--

DROP TABLE IF EXISTS `plant_soldier_school`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plant_soldier_school` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `instrumentSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `cracksSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `crystalSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `strengthenSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `militarySerialized` text COLLATE utf8mb4_unicode_ci,
  `militarySerialized3` text COLLATE utf8mb4_unicode_ci,
  `switchInfo` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plant_tech`
--

DROP TABLE IF EXISTS `plant_tech`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plant_tech` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cfgId` int(11) NOT NULL,
  `buildType` int(11) NOT NULL,
  `chipSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `openid` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `puid` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `serverId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vit` int(11) NOT NULL DEFAULT '0',
  `vitTime` bigint(20) NOT NULL DEFAULT '0',
  `icon` int(11) DEFAULT '0',
  `iconBuy` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `battlePoint` bigint(20) DEFAULT '0',
  `vipExp` int(11) NOT NULL DEFAULT '0',
  `vipLevel` int(11) NOT NULL DEFAULT '0',
  `vipFlag` int(11) NOT NULL DEFAULT '0',
  `vipFreePoint` int(11) NOT NULL DEFAULT '0',
  `militaryExp` int(11) NOT NULL,
  `talentType` int(11) NOT NULL DEFAULT '0',
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `channel` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `channelId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `country` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `deviceId` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `loginWay` int(11) NOT NULL DEFAULT '0',
  `lang` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `version` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `forbidenTime` bigint(20) NOT NULL DEFAULT '0',
  `silentTime` bigint(20) NOT NULL DEFAULT '0',
  `zeroEarningTime` bigint(20) NOT NULL DEFAULT '0',
  `resetTime` bigint(20) NOT NULL DEFAULT '0',
  `loginTime` bigint(20) NOT NULL DEFAULT '0',
  `lastLoginTime` bigint(20) NOT NULL,
  `logoutTime` bigint(20) NOT NULL DEFAULT '0',
  `loginMask` bigint(20) DEFAULT '0',
  `lastGmailCtime` bigint(20) NOT NULL DEFAULT '0',
  `factoryUpTime` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `spyMark` text COLLATE utf8mb4_unicode_ci,
  `superLab` int(11) NOT NULL DEFAULT '0',
  `laboratory` int(11) NOT NULL DEFAULT '1',
  `unlockEquipResearch` int(11) NOT NULL DEFAULT '0',
  `actResetTime` bigint(20) NOT NULL DEFAULT '0',
  `beInvited` tinyint(1) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `isActive` tinyint(1) DEFAULT '0',
  `oilConsumeTime` bigint(20) NOT NULL DEFAULT '0',
  `livelyMask` int(11) NOT NULL DEFAULT '0',
  `onlineTimeHistory` int(11) NOT NULL DEFAULT '0',
  `onlineTimeCurDay` int(11) NOT NULL DEFAULT '0',
  `pos` text COLLATE utf8mb4_unicode_ci,
  `armourSuit` int(11) NOT NULL DEFAULT '1',
  `armourSuitCount` int(11) NOT NULL DEFAULT '1',
  `maxBattlePoint` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `name_index` (`name`) USING BTREE,
  KEY `deviceId_index` (`deviceId`) USING BTREE,
  KEY `platform_index` (`platform`) USING BTREE,
  KEY `vipLevel_index` (`vipLevel`) USING BTREE,
  KEY `channel_index` (`channel`) USING BTREE,
  KEY `country_index` (`country`) USING BTREE,
  KEY `lang_index` (`lang`) USING BTREE,
  KEY `puid_index` (`puid`,`serverId`) USING BTREE,
  KEY `server_id_idx` (`serverId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_achieve`
--

DROP TABLE IF EXISTS `player_achieve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_achieve` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `missions` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_base`
--

DROP TABLE IF EXISTS `player_base`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_base` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `gold` int(11) NOT NULL DEFAULT '0',
  `coin` int(11) DEFAULT '0',
  `goldore` bigint(20) NOT NULL DEFAULT '0',
  `goldoreUnsafe` bigint(20) NOT NULL DEFAULT '0',
  `oil` bigint(20) NOT NULL DEFAULT '0',
  `oilUnsafe` bigint(20) NOT NULL DEFAULT '0',
  `steel` bigint(20) NOT NULL DEFAULT '0',
  `steelUnsafe` bigint(20) NOT NULL DEFAULT '0',
  `tombarthite` bigint(20) NOT NULL DEFAULT '0',
  `tombarthiteUnsafe` bigint(20) NOT NULL DEFAULT '0',
  `guildContribution` bigint(20) NOT NULL DEFAULT '0',
  `guildMilitaryScore` bigint(20) NOT NULL DEFAULT '0',
  `saveAmt` int(11) DEFAULT '0',
  `chargeAmt` int(11) NOT NULL DEFAULT '0',
  `diamonds` int(11) NOT NULL DEFAULT '0',
  `recharge` int(11) NOT NULL DEFAULT '0',
  `unlockedArea` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cityDefVal` int(11) NOT NULL DEFAULT '0',
  `onFireEndTime` bigint(20) NOT NULL DEFAULT '0',
  `cityDefRepairTime` bigint(20) NOT NULL DEFAULT '0',
  `cityDefConsumeTime` bigint(20) NOT NULL DEFAULT '0',
  `flag` int(11) NOT NULL DEFAULT '0',
  `warFeverEndTime` bigint(20) NOT NULL DEFAULT '0',
  `cyborgScore` bigint(20) NOT NULL DEFAULT '0',
  `dyzzScore` bigint(20) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `expDec` int(11) NOT NULL DEFAULT '0',
  `saveAmtTotal` int(11) DEFAULT '0',
  `rechargeTotal` int(11) DEFAULT '0',
  `levelUpTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `level_index` (`level`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_daily_gift_buy`
--

DROP TABLE IF EXISTS `player_daily_gift_buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_daily_gift_buy` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `termId` int(11) NOT NULL DEFAULT '0',
  `itemRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_ghost_tower`
--

DROP TABLE IF EXISTS `player_ghost_tower`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_ghost_tower` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stageId` int(11) NOT NULL,
  `productTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_gift`
--

DROP TABLE IF EXISTS `player_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_gift` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `giftGroupIds` text COLLATE utf8mb4_unicode_ci,
  `lastRefreshTime` bigint(20) NOT NULL,
  `resetTime` bigint(20) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT NULL,
  `giftAdvice` text COLLATE utf8mb4_unicode_ci,
  `poolResetTimes` text COLLATE utf8mb4_unicode_ci,
  `rootGroupIdRefRecords` text COLLATE utf8mb4_unicode_ci,
  `buyLevels` text COLLATE utf8mb4_unicode_ci,
  `buyNums` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `weekResetTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_homeland`
--

-- DROP TABLE IF EXISTS `player_homeland`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
-- CREATE TABLE `player_homeland` (
--   `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
--   `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
--   `theme_id` int(11) NOT NULL DEFAULT '0',
--   `prosperity` bigint(20) NOT NULL DEFAULT '0',
--   `createTime` bigint(20) NOT NULL DEFAULT '0',
--   `updateTime` bigint(20) NOT NULL DEFAULT '0',
--   `invalid` tinyint(1) DEFAULT '0',
--   `likes` int(11) DEFAULT '0',
--   `lastDailyLikeTime` bigint(20) DEFAULT '0',
--   `themes` text COLLATE utf8mb4_unicode_ci,
--   `activeProsperityAttr` text COLLATE utf8mb4_unicode_ci,
--   `building_data` text COLLATE utf8mb4_unicode_ci,
--   `warehouse_data` text COLLATE utf8mb4_unicode_ci,
--   `unlocked_grid_data` text COLLATE utf8mb4_unicode_ci,
--   `building_collect` text COLLATE utf8mb4_unicode_ci,
--   `dailyLikes` text COLLATE utf8mb4_unicode_ci,
--   `shareTime` bigint(20) DEFAULT '0',
--   `freeTimes` int(11) DEFAULT '0',
--   `nextFree` bigint(20) DEFAULT '0',
--   `dailyDrawTimes` int(11) DEFAULT '0',
--   PRIMARY KEY (`id`) USING BTREE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `player_homeland`;

CREATE TABLE IF NOT EXISTS `player_homeland` (
  `id`                   VARCHAR(64)  NOT NULL,
  `playerId`             VARCHAR(64)  NOT NULL,
  `theme_id`             INT          NOT NULL DEFAULT 0,
  `prosperity`           BIGINT       NOT NULL DEFAULT 0,
  `buildingData`         MEDIUMTEXT   NOT NULL,
  `warehouseData`        MEDIUMTEXT   NOT NULL,
  `buildingCollect`      MEDIUMTEXT   NOT NULL,
  `createTime`           BIGINT       NOT NULL DEFAULT 0,
  `updateTime`           BIGINT       NOT NULL DEFAULT 0,
  `invalid`              TINYINT(1)   NOT NULL DEFAULT 0,
  `likes`                INT          NOT NULL DEFAULT 0,
  `dailyLike`            MEDIUMTEXT   NOT NULL,
  `lastDailyLikeTime`    BIGINT       NOT NULL DEFAULT 0,
  `themes`               MEDIUMTEXT   NOT NULL,
  `activeProsperityAttr` MEDIUMTEXT   NOT NULL,
  `shareTime`            BIGINT       NOT NULL DEFAULT 0,
  `shopInfo`             MEDIUMTEXT   NULL,
  PRIMARY KEY (`id`),
  KEY `idx_playerId` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- Table structure for table `player_other`
--

DROP TABLE IF EXISTS `player_other`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_other` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `dressItemInfo` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `autoGuildCityMoveCnt` int(11) DEFAULT '0',
  `autoMarchParam` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_relation`
--

DROP TABLE IF EXISTS `player_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_relation` (
  `id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `targetPlayerId` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` int(11) NOT NULL DEFAULT '0',
  `love` int(11) NOT NULL,
  `remark` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `guard` tinyint(1) NOT NULL DEFAULT '0',
  `guardValue` int(11) NOT NULL DEFAULT '0',
  `operationTime` bigint(20) NOT NULL DEFAULT '0',
  `dressId` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_resource_gift`
--

DROP TABLE IF EXISTS `player_resource_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_resource_gift` (
  `playerId` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `boughtInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_war_college`
--

DROP TABLE IF EXISTS `player_war_college`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_war_college` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `instanceInfo` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `maxInstanceId` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `firstReward` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `helpRwardCount` int(11) NOT NULL DEFAULT '0',
  `helpRwardDay` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_yqzz`
--

DROP TABLE IF EXISTS `player_yqzz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_yqzz` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `achieveSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `leaveBattleTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `playerGuild` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plot_battle`
--

DROP TABLE IF EXISTS `plot_battle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plot_battle` (
  `playerId` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `levelId` int(11) NOT NULL DEFAULT '0',
  `status` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `president`
--

DROP TABLE IF EXISTS `president`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `president` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `turnCount` int(11) DEFAULT '0',
  `presidentId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `presidentGuildId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `countryModify` int(11) NOT NULL DEFAULT '0',
  `countryName` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `countryIcon` int(11) NOT NULL DEFAULT '0',
  `attackerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `attackerGuildId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `periodType` int(11) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `attackTime` bigint(20) NOT NULL DEFAULT '0',
  `peaceTime` bigint(20) NOT NULL DEFAULT '0',
  `tenureTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `push_gift`
--

DROP TABLE IF EXISTS `push_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `push_gift` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `giftIdTime` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `groupRefreshCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `resetTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) NOT NULL,
  `groupRefreshTime` text COLLATE utf8mb4_unicode_ci,
  `groupStatistics` text COLLATE utf8mb4_unicode_ci,
  `plantTechnologyTimes` int(11) DEFAULT '0',
  `plantSoldierCrackTimes` int(11) DEFAULT '0',
  `armourIntensifyTimes` text COLLATE utf8mb4_unicode_ci,
  `armourStarUpTimes` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `questionnaire`
--

DROP TABLE IF EXISTS `questionnaire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questionnaire` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pageSurveys` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mailSurveys` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `overdueSurveys` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `finishedSurveys` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lastCheckTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `queue`
--

DROP TABLE IF EXISTS `activity_homeland_round`;

CREATE TABLE `activity_homeland_round` (
  `id`              varchar(64)  NOT NULL,
  `playerId`        varchar(64)  NOT NULL,
  `termId`          int(11)      NOT NULL,
  `loginDays`       text         NOT NULL,
  `drawTimes`       int(11)      NOT NULL DEFAULT 0,
  `exchangeItems`   text         NOT NULL,
  `createTime`      bigint(20)   NOT NULL DEFAULT 0,
  `updateTime`      bigint(20)   NOT NULL DEFAULT 0,
  `invalid`         tinyint(1)   NOT NULL DEFAULT 0,
  `playerPoint`     text         NOT NULL,
  `currentFloor`    int(11)      NOT NULL DEFAULT 0,
  `achieveItems`    text         NOT NULL,
  `lastFloorChange` int(11)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;

-- DROP TABLE IF EXISTS `queue`;
-- /*!40101 SET @saved_cs_client     = @@character_set_client */;
-- /*!40101 SET character_set_client = utf8 */;
-- CREATE TABLE `queue` (
--   `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
--   `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
--   `queueType` int(11) NOT NULL DEFAULT '0',
--   `buildingType` int(11) NOT NULL DEFAULT '0',
--   `itemId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
--   `status` int(11) NOT NULL DEFAULT '0',
--   `helpTimes` int(11) NOT NULL DEFAULT '0',
--   `cancelBackRes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
--   `startTime` bigint(20) NOT NULL DEFAULT '0',
--   `endTime` bigint(20) NOT NULL DEFAULT '0',
--   `totalReduceTime` bigint(20) NOT NULL DEFAULT '0',
--   `totalQueueTime` bigint(20) NOT NULL DEFAULT '0',
--   `enableEndTime` bigint(20) NOT NULL DEFAULT '0',
--   `createTime` bigint(20) NOT NULL DEFAULT '0',
--   `updateTime` bigint(20) NOT NULL DEFAULT '0',
--   `invalid` tinyint(1) DEFAULT '0',
--   `reusage` int(11) NOT NULL DEFAULT '-1',
--   PRIMARY KEY (`id`) USING BTREE,
--   KEY `playerId_index` (`playerId`) USING BTREE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
-- /*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `queue`;

CREATE TABLE `queue` (
  `id`              varchar(64)  NOT NULL,
  `playerId`        varchar(64)  NOT NULL,
  `queueType`       int(11)      NOT NULL DEFAULT 0,
  `buildingType`    int(11)      NOT NULL DEFAULT 0,
  `itemId`          varchar(64)           DEFAULT NULL,
  `startTime`       bigint(20)   NOT NULL DEFAULT 0,
  `endTime`         bigint(20)   NOT NULL DEFAULT 0,
  `totalQueueTime`  bigint(20)   NOT NULL DEFAULT 0,
  `totalReduceTime` bigint(20)   NOT NULL DEFAULT 0,
  `status`          int(11)      NOT NULL DEFAULT 0,
  `helpTimes`       int(11)      NOT NULL DEFAULT 0,
  `cancelBackRes`   varchar(255)          DEFAULT NULL,
  `reusage`         int(11)      NOT NULL DEFAULT -1,
  `enableEndTime`   bigint(20)   NOT NULL DEFAULT 0,
  `createTime`      bigint(20)   NOT NULL DEFAULT 0,
  `updateTime`      bigint(20)   NOT NULL DEFAULT 0,
  `invalid`         tinyint(1)   NOT NULL DEFAULT 0,
  `multiply`        int(11)      NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
--
-- Table structure for table `recharge`
--

DROP TABLE IF EXISTS `recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `recharge` (
  `billno` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `type` int(11) NOT NULL,
  `diamonds` int(11) NOT NULL DEFAULT '0',
  `time` bigint(20) NOT NULL DEFAULT '0',
  `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `serverId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `puid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `goodsId` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `goodsPrice` int(11) NOT NULL DEFAULT '0',
  `payMoney` int(11) NOT NULL DEFAULT '0',
  `currency` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `awardItems` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`billno`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE,
  KEY `puid_index` (`puid`) USING BTREE,
  KEY `goodsId_index` (`goodsId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recharge_back`
--

DROP TABLE IF EXISTS `recharge_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `recharge_back` (
  `billno` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `type` int(11) DEFAULT '0',
  `diamonds` int(11) DEFAULT '0',
  `time` bigint(20) DEFAULT '0',
  `token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `serverId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `puid` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `goodsId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `goodsPrice` int(11) DEFAULT '0',
  `payMoney` int(11) DEFAULT '0',
  `currency` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `awardItems` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`billno`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recharge_daily`
--

DROP TABLE IF EXISTS `recharge_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `recharge_daily` (
  `billno` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `type` int(11) DEFAULT '0',
  `diamonds` int(11) DEFAULT '0',
  `time` bigint(20) DEFAULT '0',
  `token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `serverId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `puid` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `goodsId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `goodsPrice` int(11) DEFAULT '0',
  `payMoney` int(11) DEFAULT '0',
  `currency` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `awardItems` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`billno`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `server_identify`
--

DROP TABLE IF EXISTS `server_identify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `server_identify` (
  `serverIdentify` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `serverOpenTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`serverIdentify`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shop`
--

DROP TABLE IF EXISTS `shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shop` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `shopId` int(11) NOT NULL DEFAULT '0',
  `term` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `itemData` text COLLATE utf8mb4_unicode_ci,
  `extParam` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `statistics`
--

DROP TABLE IF EXISTS `statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `statistics` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `loginCnt` int(11) NOT NULL DEFAULT '0',
  `loginDay` int(11) NOT NULL DEFAULT '0',
  `warWinCnt` int(11) NOT NULL DEFAULT '0',
  `warLoseCnt` int(11) NOT NULL DEFAULT '0',
  `atkWinCnt` int(11) NOT NULL DEFAULT '0',
  `atkLoseCnt` int(11) NOT NULL DEFAULT '0',
  `atkInProtectCnt` int(11) NOT NULL DEFAULT '0',
  `defWinCnt` int(11) NOT NULL DEFAULT '0',
  `defLoseCnt` int(11) NOT NULL DEFAULT '0',
  `spyCnt` int(11) NOT NULL DEFAULT '0',
  `atkMonsterCnt` int(11) NOT NULL DEFAULT '0',
  `atkMonsterWinCnt` int(11) NOT NULL DEFAULT '0',
  `armyAddCnt` bigint(20) NOT NULL DEFAULT '0',
  `armyKillCnt` bigint(20) NOT NULL DEFAULT '0',
  `armyLoseCnt` bigint(20) NOT NULL DEFAULT '0',
  `armyCureCnt` bigint(20) NOT NULL DEFAULT '0',
  `joinGuildCnt` int(11) NOT NULL DEFAULT '0',
  `loseFightCnt` int(11) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `isBeating` int(11) DEFAULT '0',
  `cdkType` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cityMoveRecord` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `commonStatisData` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `status_data`
--

DROP TABLE IF EXISTS `status_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status_data` (
  `uuid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `statusId` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `targetId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `val` int(11) NOT NULL DEFAULT '0',
  `startTime` bigint(20) NOT NULL DEFAULT '0',
  `endTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`uuid`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `story_house`
--

DROP TABLE IF EXISTS `story_house`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `story_house` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `storeId` int(11) NOT NULL,
  `helpId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `queryHelp` int(11) NOT NULL,
  `openTime` bigint(20) NOT NULL,
  `collect` tinyint(1) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `story_house_base`
--

DROP TABLE IF EXISTS `story_house_base`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `story_house_base` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `exc` int(11) NOT NULL DEFAULT '0',
  `excCount` int(11) NOT NULL DEFAULT '0',
  `nextFreeExc` bigint(20) NOT NULL DEFAULT '0',
  `lastExcRecover` bigint(20) NOT NULL DEFAULT '0',
  `help` int(11) NOT NULL,
  `lastHelpRecover` bigint(20) NOT NULL,
  `lastRefrash` bigint(20) NOT NULL,
  `overDay` bigint(20) NOT NULL,
  `store` text COLLATE utf8mb4_unicode_ci,
  `refrashCount` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `story_house_help`
--

DROP TABLE IF EXISTS `story_house_help`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `story_house_help` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `targetId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `storehouseId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `storeId` int(11) NOT NULL,
  `openTime` bigint(20) NOT NULL,
  `collect` tinyint(1) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `story_mission`
--

DROP TABLE IF EXISTS `story_mission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `story_mission` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `chapterId` int(11) NOT NULL DEFAULT '0',
  `chapterState` int(11) NOT NULL DEFAULT '0',
  `missions` text COLLATE utf8mb4_unicode_ci,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `completeChapters` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `paralleledChapterMission` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `super_soldier`
--

DROP TABLE IF EXISTS `super_soldier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `super_soldier` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `soldierId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `star` int(11) NOT NULL,
  `step` int(11) NOT NULL,
  `office` int(11) NOT NULL,
  `cityDefense` int(11) NOT NULL DEFAULT '0',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `skillSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `passiveSkillSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  `anyWhereUnlock` int(11) DEFAULT '0',
  `shareCount` int(11) DEFAULT '0',
  `skin` int(11) DEFAULT '0',
  `energySerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `skinSerialized` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `talent`
--

DROP TABLE IF EXISTS `talent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `talent` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `talentId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `skillId` int(11) DEFAULT '0',
  `skillRefTime` bigint(20) NOT NULL DEFAULT '0',
  `skillState` bigint(20) DEFAULT '0',
  `castSkillTime` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tavern`
--

DROP TABLE IF EXISTS `tavern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tavern` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastRefreshTime` bigint(20) NOT NULL,
  `scoreAchieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveItems` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `achieveFinishCount` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `technology`
--

DROP TABLE IF EXISTS `technology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `technology` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `techId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `researching` tinyint(1) NOT NULL DEFAULT '0',
  `skillCd` bigint(20) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `war_flag`
--

DROP TABLE IF EXISTS `war_flag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `war_flag` (
  `flagId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ownerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `currentId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `placeTime` bigint(20) NOT NULL DEFAULT '0',
  `life` int(11) NOT NULL,
  `completeTime` bigint(20) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL,
  `speed` decimal(20,6) NOT NULL DEFAULT '0.000000',
  `pointId` int(11) NOT NULL,
  `lastBuildTick` bigint(20) NOT NULL DEFAULT '0',
  `lastResourceTick` bigint(20) NOT NULL DEFAULT '0',
  `ownIndex` int(11) NOT NULL DEFAULT '0',
  `occupyLife` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  `centerFlag` int(11) NOT NULL DEFAULT '0',
  `signUp` text CHARACTER SET utf8mb4 NOT NULL,
  `centerNextTickTime` bigint(20) NOT NULL DEFAULT '0',
  `centerActive` int(11) NOT NULL DEFAULT '0',
  `removeTime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`flagId`) USING BTREE,
  KEY `idx_flagId` (`flagId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wharf`
--

DROP TABLE IF EXISTS `wharf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wharf` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastRefreshTime` bigint(20) NOT NULL,
  `awardTime` int(11) NOT NULL,
  `awardId` int(11) NOT NULL,
  `awardPoolId` int(11) NOT NULL,
  `isTookAward` tinyint(1) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wishing_well`
--

DROP TABLE IF EXISTS `wishing_well`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wishing_well` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastWishTime` bigint(20) NOT NULL,
  `todayWishCounts` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `extraWishCount` int(11) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `world_march`
--

DROP TABLE IF EXISTS `world_march`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `world_march` (
  `marchId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerName` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origionId` int(11) NOT NULL,
  `terminalId` int(11) NOT NULL,
  `marchType` int(11) NOT NULL,
  `marchIntention` int(11) NOT NULL DEFAULT '0',
  `marchStatus` int(11) NOT NULL,
  `marchProcMask` int(11) DEFAULT '0',
  `startTime` bigint(20) NOT NULL,
  `endTime` bigint(20) NOT NULL,
  `reachTime` bigint(20) NOT NULL DEFAULT '0',
  `marchJourneyTime` bigint(20) NOT NULL DEFAULT '0',
  `targetId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `targetPointField` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `targetPointType` int(11) NOT NULL DEFAULT '0',
  `leaderPlayerId` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `armyStr` text COLLATE utf8mb4_unicode_ci,
  `assistantStr` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `heroIdStr` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `superSoldierId` int(11) NOT NULL DEFAULT '0',
  `awardStr` text CHARACTER SET utf8mb4,
  `awardExtraStr` text CHARACTER SET utf8mb4,
  `resStartTime` bigint(20) NOT NULL DEFAULT '0',
  `resEndTime` bigint(20) NOT NULL DEFAULT '0',
  `collectSpeed` decimal(20,6) DEFAULT '0.000000',
  `collectBaseSpeed` decimal(20,6) DEFAULT '0.000000',
  `massReadyTime` bigint(20) NOT NULL DEFAULT '0',
  `speedUpTimes` int(11) NOT NULL DEFAULT '0',
  `itemUseY` decimal(20,6) DEFAULT '0.000000',
  `itemUseX` decimal(20,6) DEFAULT '0.000000',
  `callBackX` decimal(20,6) DEFAULT '0.000000',
  `callBackY` decimal(20,6) DEFAULT '0.000000',
  `callBackTime` bigint(20) NOT NULL DEFAULT '0',
  `itemUseTime` bigint(20) NOT NULL DEFAULT '0',
  `attackTimes` int(11) NOT NULL DEFAULT '0',
  `buyItemTimes` int(11) NOT NULL DEFAULT '0',
  `manorMarchReachTime` bigint(20) DEFAULT NULL,
  `lastExploreTime` bigint(20) DEFAULT '0',
  `isOffensive` int(11) NOT NULL DEFAULT '0',
  `towerAttackInfo` text COLLATE utf8mb4_unicode_ci,
  `effect` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `vitCost` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `alarmPointId` int(11) NOT NULL DEFAULT '0',
  `extraSpyMarch` tinyint(1) DEFAULT '0',
  `armourSuit` int(11) NOT NULL DEFAULT '0',
  `talentType` int(11) NOT NULL DEFAULT '0',
  `superLab` int(11) NOT NULL DEFAULT '0',
  `emoticon` int(11) NOT NULL DEFAULT '0',
  `emoticonUseTime` bigint(20) NOT NULL DEFAULT '0',
  `formation` int(11) DEFAULT '0',
  `dressStr` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `extraInfo` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `mechacoreSuit` int(11) DEFAULT '0',
  `manhattanAtkSwId` int(11) DEFAULT '0',
  `manhattanDefSwId` int(11) DEFAULT '0',
  PRIMARY KEY (`marchId`) USING BTREE,
  KEY `index_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `world_point`
--

DROP TABLE IF EXISTS `world_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `world_point` (
  `id` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `areaId` int(11) NOT NULL,
  `zoneId` int(11) NOT NULL,
  `pointType` int(11) NOT NULL,
  `pointStatus` int(11) NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `playername` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cityLevel` int(11) NOT NULL DEFAULT '0',
  `playerIcon` int(11) NOT NULL DEFAULT '0',
  `lastActiveTime` bigint(20) NOT NULL DEFAULT '0',
  `resourceId` int(11) NOT NULL DEFAULT '0',
  `monsterId` int(11) NOT NULL DEFAULT '0',
  `remainBlood` int(11) DEFAULT '0',
  `remainResNum` bigint(20) NOT NULL DEFAULT '0',
  `marchId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `guildBuildId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `guildId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `buildingId` int(11) NOT NULL DEFAULT '0',
  `protectedEndTime` bigint(20) NOT NULL DEFAULT '0',
  `showEffect` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `commonHurtEndTime` bigint(20) NOT NULL DEFAULT '0',
  `ownerId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lifeStartTime` bigint(20) NOT NULL DEFAULT '0',
  `foggyInfo` text COLLATE utf8mb4_unicode_ci,
  `emoticon` int(11) NOT NULL DEFAULT '0',
  `emoticonUseTime` bigint(20) NOT NULL DEFAULT '0',
  `personalProtectInfo` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  `equipTechLevel` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `plantMilitaryLevel` int(11) DEFAULT '0',
  `atkManhattanSw` int(11) DEFAULT '0',
  `defManhattanSw` int(11) DEFAULT '0',
  `atkSwSkillId` int(11) DEFAULT '0',
  `defSwSkillId` int(11) DEFAULT '0',
  `plantMilitaryShow` int(11) DEFAULT '0',
  `mechaCoreShow` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xqhx`
--

DROP TABLE IF EXISTS `xqhx`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xqhx` (
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `season` int(11) DEFAULT '0',
  `usedPoint` int(11) DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xqhx_talent`
--

DROP TABLE IF EXISTS `xqhx_talent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xqhx_talent` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `talentId` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) DEFAULT '0',
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `yuri_strike`
--

DROP TABLE IF EXISTS `yuri_strike`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yuri_strike` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `playerId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cfgId` int(11) NOT NULL,
  `hasReward` int(11) NOT NULL,
  `areaIdLock` int(11) NOT NULL,
  `marchId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cleanQueueId` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `matchTime` bigint(20) NOT NULL,
  `createTime` bigint(20) NOT NULL,
  `updateTime` bigint(20) NOT NULL,
  `invalid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `id_Index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-06 11:47:53
