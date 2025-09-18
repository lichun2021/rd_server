/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50621
Source Host           : localhost:3306
Source Database       : db_hjol_online

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2018-08-28 12:16:31
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `tb_hjol_onlinecnt`
-- ----------------------------
DROP TABLE IF EXISTS `tb_hjol_onlinecnt`;
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

-- ----------------------------
-- Records of tb_hjol_onlinecnt
-- ----------------------------
