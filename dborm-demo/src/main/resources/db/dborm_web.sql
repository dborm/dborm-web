/*
 Navicat Premium Data Transfer

 Source Server         : dborm_test
 Source Server Type    : MySQL
 Source Server Version : 50616
 Source Host           : rds3tmsxzi96h6921824.mysql.rds.aliyuncs.com
 Source Database       : dborm_web

 Target Server Type    : MySQL
 Target Server Version : 50616
 File Encoding         : utf-8

 Date: 07/26/2016 10:19:51 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `user_info`
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '昵称',
  `age` int(11) DEFAULT '18' COMMENT '年龄，默认值为18',
  `create_by` varchar(64) NOT NULL DEFAULT '',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `deleted` int(11) DEFAULT '0' COMMENT '是否删除（软删除的时候使用，0：未删除  1：已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `user_info`
-- ----------------------------
BEGIN;
INSERT INTO `user_info` VALUES ('1', '122', '1', '26', '', '2016-06-14 15:51:37', 'sdfs23fsfsdfklsdflds', '2016-06-14 16:03:48', '0'), ('893875077c814bcd9032db84c4220fa3', '11', '11', '1122', 'sdfs23fsfsdfklsdflds', '2016-06-14 13:38:41', 'sdfs23fsfsdfklsdflds', '2016-06-14 16:00:28', '0'), ('94e82949dd6243c4a36a97ce181ac082', '122', '122222', '122', 'sdfs23fsfsdfklsdflds', '2016-06-14 15:58:17', 'sdfs23fsfsdfklsdflds', '2016-06-14 15:58:17', '0'), ('9a19697a9dc744b7bc999ebb7e61e1db', '122', '122', '122', 'sdfs23fsfsdfklsdflds', '2016-06-14 15:58:07', 'sdfs23fsfsdfklsdflds', '2016-06-14 15:58:07', '0'), ('9fdc88e4ebd3408a8501113d84754a01', '11', '11', '11', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:43:06', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:43:06', '0'), ('c761a45680244164b865cac0eac62856', '11', '11', '11', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:37:05', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:37:02', '0'), ('ee72ac2df4a949db9a9abb25769b52b6', '11', '11', '11', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:40:29', 'sdfs23fsfsdfklsdflds', '2016-06-14 11:40:29', '0'), ('f804a7e20eec48f992159f0baeebd22c', '11', '11', '11', 'sdfs23fsfsdfklsdflds', '2016-06-14 13:35:31', 'sdfs23fsfsdfklsdflds', '2016-06-14 13:35:31', '0'), ('f8f166d9c2994f229b49a8628c935dc8', 'sky', '123', '23', 'sdfs23fsfsdfklsdflds', '2016-06-14 16:17:01', 'sdfs23fsfsdfklsdflds', '2016-06-14 16:19:09', '0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
