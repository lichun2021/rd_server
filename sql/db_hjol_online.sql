-- MySQL dump 10.13  Distrib 5.7.44, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: db_hjol_online
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
-- Table structure for table `activity_daiy_buy_gift`
--

DROP TABLE IF EXISTS `activity_daiy_buy_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_daiy_buy_gift` (
  `id` varchar(255) NOT NULL,
  `playerId` varchar(255) NOT NULL,
  `termId` int(11) NOT NULL DEFAULT '0',
  `itemRecord` text,
  `refreshTime` bigint(20) NOT NULL DEFAULT '0',
  `achieveItems` text,
  `createTime` bigint(20) NOT NULL DEFAULT '0',
  `updateTime` bigint(20) NOT NULL DEFAULT '0',
  `invalid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_playerId` (`playerId`),
  KEY `idx_termId` (`termId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每日购买礼包活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_hjol_onlinecnt`
--

DROP TABLE IF EXISTS `tb_hjol_onlinecnt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_hjol_onlinecnt` (
  `gameappid` varchar(32) NOT NULL DEFAULT '',
  `timekey` int(11) NOT NULL DEFAULT '0',
  `gsid` varchar(32) NOT NULL DEFAULT '',
  `zoneareaid` int(11) NOT NULL DEFAULT '0',
  `onlinecntios` int(11) NOT NULL DEFAULT '0',
  `onlinecntandroid` int(11) NOT NULL DEFAULT '0',
  `registernum` int(11) NOT NULL DEFAULT '0',
  `queuesize` int(11) NOT NULL DEFAULT '0',
  KEY `timekey` (`timekey`,`gameappid`,`gsid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-06 12:10:51
