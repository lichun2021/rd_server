CREATE DATABASE db_HJOL_online DEFAULT CHARACTER SET utf8;
use db_HJOL_online;
CREATE TABLE `tb_HJOL_onlinecnt` (
  `gameappid` varchar(32) NOT NULL DEFAULT '',
  `timekey` int(11) NOT NULL DEFAULT '0',
  `gsid` varchar(32) NOT NULL DEFAULT '',
  `zoneareaid` int(11) NOT NULL DEFAULT '0',
  `onlinecntios` int(11) NOT NULL DEFAULT '0',
  `onlinecntandroid` int(11) NOT NULL DEFAULT '0',
  KEY `timekey` (`timekey`,`gameappid`,`gsid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
