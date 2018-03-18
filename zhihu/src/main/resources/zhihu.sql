/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : zhihu

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2018-03-17 16:24:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for zhihuuser
-- ----------------------------
DROP TABLE IF EXISTS `zhihuuser`;
CREATE TABLE `zhihuuser` (
  `userName` varchar(32) NOT NULL,
  `nickName` varchar(32) DEFAULT NULL,
  `sex` tinyint(4) DEFAULT NULL,
  `profile` varchar(128) DEFAULT NULL,
  `business` varchar(32) DEFAULT NULL,
  `company` varchar(32) DEFAULT NULL,
  `position` varchar(32) DEFAULT NULL,
  `education` varchar(32) DEFAULT NULL,
  `major` varchar(32) DEFAULT NULL,
  `answersNum` int(11) DEFAULT '0',
  `questionsNum` int(11) DEFAULT '0',
  `starsNum` int(11) DEFAULT '0',
  `thxNum` int(11) DEFAULT '0',
  `followingNum` int(11) DEFAULT '0',
  `followersNum` int(11) DEFAULT '0',
  PRIMARY KEY (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
